package com.safescreen.app.services.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.core.content.ContextCompat
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real-time Audio Capture Metrics for SafeScreen diagnostics.
 */
data class AudioCaptureMetrics(
    val isCapturing: Boolean = false,
    val captureMode: String = "IDLE", // "INTERNAL_PLAYBACK", "MIC_FALLBACK", "IDLE"
    val sampleRate: Int = 16000,
    val bufferSizeSamples: Int = 0,
    val currentRmsDb: Float = -100f,
    val framesCaptured: Long = 0L
)

/**
 * Pre-allocated Zero-GC Circular Ring Buffer for PCM 16-bit audio samples.
 * Prevents object allocation during continuous streaming.
 */
class AudioRingBuffer(val capacity: Int) {
    private val buffer = ShortArray(capacity)
    private var writeIndex = 0
    private var count = 0
    private val lock = Any()

    fun write(data: ShortArray, offset: Int, length: Int) {
        synchronized(lock) {
            for (i in 0 until length) {
                buffer[writeIndex] = data[offset + i]
                writeIndex = (writeIndex + 1) % capacity
                if (count < capacity) count++
            }
        }
    }

    /**
     * Copies the latest samples into the destination array without allocating new objects.
     */
    fun readLatest(destination: ShortArray): Int {
        synchronized(lock) {
            val toRead = minOf(destination.size, count)
            if (toRead == 0) return 0

            val start = (writeIndex - toRead + capacity) % capacity
            for (i in 0 until toRead) {
                destination[i] = buffer[(start + i) % capacity]
            }
            return toRead
        }
    }

    fun clear() {
        synchronized(lock) {
            writeIndex = 0
            count = 0
        }
    }
}

/**
 * Production Audio Playback Capture Engine for SafeScreen.
 * Captures internal system media audio (YouTube, Spotify, Instagram Reels, Chrome, etc.)
 * via AudioPlaybackCaptureConfiguration (Android 10+ / API 29+) with fallback to microphone.
 *
 * Critical AI Format: 16kHz, 16-bit PCM, Mono (standard input for YAMNet audio classifier).
 * Zero GC Pressure: Pre-allocated circular ring buffer and reusable chunk buffers.
 */
@Singleton
class AudioPlaybackCaptureEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val isRecording = AtomicBoolean(false)
    private val isPausedByPreference = AtomicBoolean(false)

    // Pre-allocated Zero-GC Buffers
    // 3 seconds capacity at 16kHz mono = 48,000 shorts (~96 KB)
    private val ringBuffer = AudioRingBuffer(RING_BUFFER_CAPACITY)
    private val readChunkBuffer = ShortArray(READ_CHUNK_SIZE)
    // YAMNet standard window: 0.975s = 15,600 samples
    private val windowBuffer = ShortArray(YAMNET_WINDOW_SAMPLES)

    private val _metrics = MutableStateFlow(AudioCaptureMetrics())
    val metrics: StateFlow<AudioCaptureMetrics> = _metrics.asStateFlow()

    private val _audioFlow = MutableSharedFlow<ShortArray>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val audioFlow: SharedFlow<ShortArray> = _audioFlow.asSharedFlow()

    var onAudioWindowCapturedListener: ((ShortArray) -> Unit)? = null

    val isCapturing: Boolean
        get() = isRecording.get() && audioRecord != null

    init {
        // Observe audio mute preference from DataStore
        userPreferencesRepository.isAudioMuteEnabledFlow
            .onEach { isEnabled ->
                isPausedByPreference.set(!isEnabled)
                Log.d(TAG, "Audio mute preference updated: isEnabled=$isEnabled")
            }
            .launchIn(engineScope)
    }

    /**
     * Starts audio capture using AudioPlaybackCaptureConfiguration if MediaProjection is provided,
     * or gracefully falls back to MIC recording if permission is granted.
     */
    @Synchronized
    fun startCapture(mediaProjection: MediaProjection? = null): Boolean {
        if (isRecording.get()) {
            Log.d(TAG, "Audio capture is already active")
            return true
        }

        // Verify RECORD_AUDIO permission
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            Log.w(TAG, "Cannot start audio capture: RECORD_AUDIO permission not granted")
            return false
        }

        try {
            Log.d(TAG, "Initializing AudioRecord with 16kHz Mono 16-bit PCM...")

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()

            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSizeInBytes = maxOf(minBufferSize, SAMPLE_RATE * 2) // at least 1s buffer (32 KB)

            var captureMode = "IDLE"

            // 1. Attempt AudioPlaybackCapture (Internal system media audio on Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && mediaProjection != null) {
                try {
                    val playbackConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build()

                    audioRecord = AudioRecord.Builder()
                        .setAudioFormat(audioFormat)
                        .setBufferSizeInBytes(bufferSizeInBytes)
                        .setAudioPlaybackCaptureConfig(playbackConfig)
                        .build()

                    if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                        captureMode = "INTERNAL_PLAYBACK"
                        Log.i(TAG, "AudioPlaybackCapture initialized successfully (Internal media playback)")
                    } else {
                        Log.w(TAG, "AudioPlaybackCapture failed to initialize, falling back to MIC")
                        audioRecord?.release()
                        audioRecord = null
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "AudioPlaybackCapture configuration error: ${e.message}, falling back to MIC")
                    audioRecord?.release()
                    audioRecord = null
                }
            }

            // 2. Fallback to standard Microphone recording if playback capture unavailable
            if (audioRecord == null) {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes
                )
                captureMode = "MIC_FALLBACK"
                Log.i(TAG, "AudioRecord initialized in MIC_FALLBACK mode")
            }

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to reach STATE_INITIALIZED")
                stopCapture()
                return false
            }

            // Start recording
            audioRecord?.startRecording()
            isRecording.set(true)
            ringBuffer.clear()

            _metrics.update {
                it.copy(
                    isCapturing = true,
                    captureMode = captureMode,
                    sampleRate = SAMPLE_RATE,
                    bufferSizeSamples = bufferSizeInBytes / 2,
                    currentRmsDb = -100f,
                    framesCaptured = 0L
                )
            }

            // Start dedicated audio reading background thread
            recordingThread = Thread({
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                runAudioRecordingLoop()
            }, "SafeScreenAudioThread").apply {
                start()
            }

            Log.i(TAG, "AudioPlaybackCaptureEngine running: Mode=$captureMode, SampleRate=$SAMPLE_RATE Hz")
            return true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException starting AudioRecord: ${e.message}", e)
            stopCapture()
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioPlaybackCaptureEngine: ${e.message}", e)
            stopCapture()
            return false
        }
    }

    /**
     * Upgrades an ongoing capture to internal playback capture when MediaProjection becomes available.
     */
    @Synchronized
    fun upgradeToPlaybackCapture(mediaProjection: MediaProjection) {
        if (_metrics.value.captureMode == "INTERNAL_PLAYBACK") return
        Log.d(TAG, "Upgrading audio capture engine to INTERNAL_PLAYBACK mode...")
        stopCapture()
        startCapture(mediaProjection)
    }

    /**
     * Dedicated background audio processing loop.
     * Zero-GC: Reuses pre-allocated chunk and ring buffers without heap allocations.
     */
    private fun runAudioRecordingLoop() {
        var totalFrames = 0L
        var lastWindowEmitTime = 0L
        var lastLogTime = System.currentTimeMillis()

        while (isRecording.get()) {
            val record = audioRecord ?: break

            if (isPausedByPreference.get()) {
                try {
                    Thread.sleep(100)
                } catch (_: InterruptedException) {
                    break
                }
                continue
            }

            val readCount = record.read(readChunkBuffer, 0, readChunkBuffer.size)
            if (readCount > 0) {
                // 1. Write chunk to Circular RingBuffer (Zero GC)
                ringBuffer.write(readChunkBuffer, 0, readCount)
                totalFrames += readCount

                // 2. Compute RMS Energy (dB) using primitive math (Zero GC)
                var sum = 0.0
                for (i in 0 until readCount) {
                    val sample = readChunkBuffer[i].toDouble() / 32768.0
                    sum += sample * sample
                }
                val rms = kotlin.math.sqrt(sum / readCount)
                val currentDb = if (rms > 1e-5) {
                    (20.0 * kotlin.math.log10(rms)).toFloat().coerceIn(-100f, 0f)
                } else {
                    -100f
                }

                val now = System.currentTimeMillis()

                // 3. Emit AI inference window (YAMNet standard 0.975s window) every 500ms
                if (now - lastWindowEmitTime >= WINDOW_EMIT_INTERVAL_MS) {
                    lastWindowEmitTime = now
                    val samplesCopied = ringBuffer.readLatest(windowBuffer)
                    if (samplesCopied >= YAMNET_WINDOW_SAMPLES) {
                        _audioFlow.tryEmit(windowBuffer)
                        onAudioWindowCapturedListener?.invoke(windowBuffer)
                    }
                }

                // 4. Update metrics and log diagnostics every ~1.5 seconds
                if (now - lastLogTime >= 1500) {
                    lastLogTime = now
                    val mode = _metrics.value.captureMode
                    _metrics.update {
                        it.copy(
                            currentRmsDb = currentDb,
                            framesCaptured = totalFrames
                        )
                    }
                    Log.d(TAG, "Audio Active: Mode=$mode, RMS=${"%.1f".format(currentDb)} dB, TotalSamples=$totalFrames")
                }
            } else if (readCount < 0) {
                Log.w(TAG, "AudioRecord read error: $readCount")
                if (readCount == AudioRecord.ERROR_INVALID_OPERATION || readCount == AudioRecord.ERROR_BAD_VALUE) {
                    break
                }
            }
        }

        Log.d(TAG, "Audio recording loop terminated")
    }

    /**
     * Cleanly stops recording and releases native audio hardware resources.
     */
    @Synchronized
    fun stopCapture() {
        Log.d(TAG, "Stopping AudioPlaybackCaptureEngine...")

        try {
            isRecording.set(false)

            recordingThread?.interrupt()
            recordingThread = null

            audioRecord?.apply {
                if (state == AudioRecord.STATE_INITIALIZED) {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.w(TAG, "Error stopping AudioRecord: ${e.message}")
                    }
                }
                release()
            }
            audioRecord = null

            ringBuffer.clear()

            _metrics.update {
                it.copy(
                    isCapturing = false,
                    captureMode = "IDLE",
                    currentRmsDb = -100f
                )
            }

            Log.i(TAG, "AudioPlaybackCaptureEngine stopped and resources released cleanly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio capture: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "SafeScreenAudio"

        // 16kHz Mono 16-bit PCM for YAMNet AI
        const val SAMPLE_RATE = 16000
        const val READ_CHUNK_SIZE = 1024
        // 3 seconds ring buffer capacity (48,000 shorts)
        const val RING_BUFFER_CAPACITY = 16000 * 3
        // YAMNet inference window: 0.975 seconds = 15,600 samples
        const val YAMNET_WINDOW_SAMPLES = 15600
        // Window emission interval: 500ms
        const val WINDOW_EMIT_INTERVAL_MS = 500L
    }
}
