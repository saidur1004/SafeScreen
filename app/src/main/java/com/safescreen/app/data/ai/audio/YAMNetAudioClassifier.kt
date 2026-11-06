package com.safescreen.app.data.ai.audio

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.presentation.screens.settings.SensitivityLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result data model of YAMNet audio classification.
 */
data class AudioClassificationResult(
    val isExplicit: Boolean = false,
    val confidence: Float = 0f,
    val topCategory: String = "Clean",
    val explicitScore: Float = 0f,
    val inferenceTimeMs: Long = 0L
)

/**
 * High-performance, Zero-GC on-device YAMNet Audio Classifier.
 *
 * Implements Phase 5.2 requirements:
 * 1. 16kHz Mono PCM explicit audio monitoring (0.975s window / 15,600 samples).
 * 2. Pre-allocated direct ByteBuffers (15600 * 4 bytes) and reusable tensors.
 * 3. AudioSet explicit sound signature detection (Groan, Grunt, Pant, Sigh, Gasp, Screaming).
 * 4. Dynamic sensitivity threshold adaptation synced with DataStore.
 */
@Singleton
class YAMNetAudioClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    private var explicitThreshold = 0.35f
    private val classLabels = mutableMapOf<Int, String>()

    // Explicit AudioSet sound signature class indices
    // 6: Shout, 11: Screaming, 12: Whispering, 23: Sigh, 33: Groan, 34: Grunt, 39: Gasp, 40: Pant
    private val adultMoanIndices = setOf(23, 33, 34, 39, 40)
    private val highIntensityVocalIndices = setOf(6, 11, 12)

    // Music & Singing AudioSet indices for muting music tracks
    private val musicAndSingingIndices = setOf(
        24, 29, 30, 107, 132, 133, 135, 136, 137, 138, 139, 140, 147, 148, 149,
        211, 212, 214, 222, 225, 228, 229, 232, 234, 235, 240, 241, 242, 243,
        244, 247, 248, 249, 251, 253, 254, 255, 256, 257, 259, 260, 261, 262,
        263, 264, 265, 267, 268, 269, 270, 271, 272, 273, 274, 275, 276
    )

    // Pre-allocated Zero-GC buffers
    private val inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(
        WINDOW_SAMPLES * 4
    ).order(ByteOrder.nativeOrder())

    private val outputScores = Array(1) { FloatArray(NUM_CLASSES) }
    private val outputsMap = mutableMapOf<Int, Any>()
    private val inputWrapper = arrayOf<Any>(inputBuffer)
    private val inferenceMutex = Mutex()

    init {
        // Observe user sensitivity changes reactively
        preferencesRepository.aiSensitivityFlow
            .onEach { level ->
                explicitThreshold = when (level) {
                    SensitivityLevel.STRICT -> 0.25f
                    SensitivityLevel.BALANCED -> 0.35f
                    SensitivityLevel.RELAXED -> 0.50f
                }
                Log.d(TAG, "YAMNetAudioClassifier sensitivity updated to $level (threshold=$explicitThreshold)")
            }
            .launchIn(scope)

        loadClassLabels()
        initialize(MODEL_ASSET_PATH)
    }

    private fun loadClassLabels() {
        try {
            context.assets.open(LABELS_ASSET_PATH).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var isFirstLine = true
                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (isFirstLine) {
                            isFirstLine = false
                            line = reader.readLine()
                            continue
                        }
                        val parts = line.split(",")
                        if (parts.size >= 3) {
                            val index = parts[0].trim().toIntOrNull()
                            val label = parts[2].replace("\"", "").trim()
                            if (index != null) {
                                classLabels[index] = label
                            }
                        }
                        line = reader.readLine()
                    }
                }
            }
            Log.d(TAG, "Loaded ${classLabels.size} YAMNet audio class labels from assets")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load YAMNet labels from $LABELS_ASSET_PATH", e)
        }
    }

    @Synchronized
    fun initialize(modelPath: String = MODEL_ASSET_PATH): Boolean {
        if (isInitialized && interpreter != null) return true

        return try {
            val byteBuffer = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options().apply {
                setNumThreads(2)
                setUseNNAPI(false) // YAMNet 1D audio runs fastest on 2-thread XNNPACK CPU
            }
            interpreter = Interpreter(byteBuffer, options)
            outputsMap[0] = outputScores
            isInitialized = true
            Log.i(TAG, "YAMNetAudioClassifier initialized successfully with 2-thread XNNPACK")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load YAMNet model from assets: $modelPath", e)
            isInitialized = false
            false
        }
    }

    /**
     * Classifies a 0.975-second audio chunk (15,600 samples of 16kHz PCM mono) with zero allocations.
     *
     * @param samples 16-bit Mono PCM audio samples.
     * @return [AudioClassificationResult] with explicit threat confidence and category.
     */
    suspend fun classifyAudio(samples: ShortArray): AudioClassificationResult = withContext(Dispatchers.Default) {
        inferenceMutex.withLock {
            if (!isInitialized || interpreter == null || samples.isEmpty()) {
                return@withLock AudioClassificationResult(
                    isExplicit = false,
                    confidence = 0f,
                    topCategory = "Uninitialized"
                )
            }

            val startTime = SystemClock.elapsedRealtime()

            try {
                // 1. Populate direct ByteBuffer with normalized float values in [-1.0f, 1.0f]
                inputBuffer.rewind()
                val sampleCount = minOf(samples.size, WINDOW_SAMPLES)
                for (i in 0 until sampleCount) {
                    val normalized = (samples[i] / 32768.0f).coerceIn(-1.0f, 1.0f)
                    inputBuffer.putFloat(normalized)
                }
                // Zero-pad if sample size is slightly under window size
                for (i in sampleCount until WINDOW_SAMPLES) {
                    inputBuffer.putFloat(0f)
                }

                // 2. Run inference
                interpreter!!.runForMultipleInputsOutputs(inputWrapper, outputsMap)

                val inferenceTime = SystemClock.elapsedRealtime() - startTime

                // 3. Evaluate scores
                val scores = outputScores[0]
                var highestScore = 0f
                var topCategoryIndex = 0

                var maxMoanScore = 0f
                var maxVocalScore = 0f
                var maxMusicScore = 0f

                for (i in 0 until minOf(scores.size, NUM_CLASSES)) {
                    val score = scores[i]
                    if (score > highestScore) {
                        highestScore = score
                        topCategoryIndex = i
                    }

                    if (adultMoanIndices.contains(i) && score > maxMoanScore) {
                        maxMoanScore = score
                    }
                    if (highIntensityVocalIndices.contains(i) && score > maxVocalScore) {
                        maxVocalScore = score
                    }
                    if (musicAndSingingIndices.contains(i) && score > maxMusicScore) {
                        maxMusicScore = score
                    }
                }

                // Effective threat score calculation (both explicit vocal/moan and background music/song)
                val explicitScore = maxOf(maxMoanScore, (maxVocalScore * 0.85f), maxMusicScore)
                val isExplicit = explicitScore >= explicitThreshold

                val threatCategoryName = when {
                    maxMusicScore >= explicitThreshold -> classLabels[topCategoryIndex]?.takeIf { musicAndSingingIndices.contains(topCategoryIndex) } ?: "Music Track"
                    maxMoanScore >= explicitThreshold -> "Explicit Audio"
                    maxVocalScore >= explicitThreshold -> "High Intensity Vocal"
                    else -> classLabels[topCategoryIndex] ?: "Clean"
                }

                if (isExplicit) {
                    Log.d(
                        TAG,
                        "Audio Threat Intercepted: $threatCategoryName (${(explicitScore * 100).toInt()}%), threshold=$explicitThreshold"
                    )
                }

                AudioClassificationResult(
                    isExplicit = isExplicit,
                    confidence = explicitScore,
                    topCategory = threatCategoryName,
                    explicitScore = explicitScore,
                    inferenceTimeMs = inferenceTime
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing YAMNet audio inference", e)
                AudioClassificationResult(isExplicit = false, confidence = 0f, topCategory = "Error")
            }
        }
    }

    @Synchronized
    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            isInitialized = false
            Log.d(TAG, "YAMNetAudioClassifier closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing YAMNetAudioClassifier", e)
        }
    }

    companion object {
        private const val TAG = "SafeScreenYAMNet"
        private const val MODEL_ASSET_PATH = "models/yamnet.tflite"
        private const val LABELS_ASSET_PATH = "models/yamnet_class_map.csv"
        const val WINDOW_SAMPLES = 15600
        const val NUM_CLASSES = 521
    }
}
