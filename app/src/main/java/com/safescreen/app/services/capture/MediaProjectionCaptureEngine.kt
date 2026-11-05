package com.safescreen.app.services.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.presentation.screens.settings.PerformanceProfile
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
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Capture metrics data model for telemetry and UI status.
 */
data class CaptureMetrics(
    val isCapturing: Boolean = false,
    val targetFps: Int = 5,
    val actualFps: Float = 0f,
    val framesCaptured: Long = 0L,
    val framesSkipped: Long = 0L,
    val width: Int = 0,
    val height: Int = 0
)

/**
 * High-performance, Zero-GC-pressure screen frame capture engine using Android [MediaProjection]
 * and [VirtualDisplay] with adaptive FPS decimation and static frame skip.
 */
@Singleton
class MediaProjectionCaptureEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository
) {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var mediaProjectionManager: MediaProjectionManager =
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var windowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    // Pre-allocated Zero-GC Double/Triple-Buffer Pool for thread-safe concurrent AI inference
    private val bitmapPool = arrayOfNulls<Bitmap>(BITMAP_POOL_SIZE)
    private var currentBufferIndex = 0
    private var cleanRowBuffer: ByteBuffer? = null
    private var captureWidth: Int = 0
    private var captureHeight: Int = 0

    @Volatile
    private var isScreenInteractive: Boolean = true

    // Adaptive FPS throttling
    private var targetFps: Int = 5
    private var targetIntervalMillis: Long = 200L // 1000 / 5
    private var lastFrameTimeMillis: Long = 0L

    // Static screen delta detection (8x8 pixel sample grid)
    private val sampleGrid = IntArray(64)
    private val prevSampleGrid = IntArray(64)
    private var staticConsecutiveFrames = 0

    // Telemetry & counters
    private var totalCapturedFrames = 0L
    private var totalSkippedFrames = 0L
    private var fpsCounter = 0
    private var fpsLastCalcTime = 0L
    private var currentCalculatedFps = 0f

    private val _metrics = MutableStateFlow(CaptureMetrics())
    val metrics: StateFlow<CaptureMetrics> = _metrics.asStateFlow()

    private val _frameFlow = MutableSharedFlow<Bitmap>(
        replay = 0,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    val frameFlow: SharedFlow<Bitmap> = _frameFlow.asSharedFlow()

    @Volatile
    var onProjectionStoppedListener: (() -> Unit)? = null

    val isCapturing: Boolean
        get() = virtualDisplay != null && mediaProjection != null

    val activeMediaProjection: MediaProjection?
        get() = mediaProjection

    init {
        // Observe performance profile from DataStore to adapt frame decimation
        userPreferencesRepository.performanceProfileFlow
            .onEach { profile ->
                updateFpsTarget(profile)
            }
            .launchIn(engineScope)
    }

    private fun updateFpsTarget(profile: PerformanceProfile) {
        targetFps = profile.fps
        targetIntervalMillis = (1000L / targetFps).coerceAtLeast(100L)
        _metrics.update { it.copy(targetFps = targetFps) }
        Log.d(TAG, "Adaptive FPS updated: targetFps=$targetFps, interval=${targetIntervalMillis}ms")
    }

    /**
     * Starts continuous background frame capture using provided MediaProjection consent token.
     */
    @Synchronized
    fun startCapture(resultCode: Int, resultData: Intent): Boolean {
        if (isCapturing) {
            Log.d(TAG, "MediaProjectionCaptureEngine is already capturing")
            return true
        }

        try {
            Log.d(TAG, "Starting MediaProjectionCaptureEngine...")

            // 1. Initialize dedicated background thread for image reader events
            handlerThread = HandlerThread("SafeScreenCaptureThread", Process.THREAD_PRIORITY_BACKGROUND).apply {
                start()
            }
            backgroundHandler = Handler(handlerThread!!.looper)

            // 2. Retrieve MediaProjection session
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData)
            if (mediaProjection == null) {
                Log.e(TAG, "Failed to obtain MediaProjection instance from resultCode=$resultCode")
                stopCapture()
                return false
            }

            // 3. Register mandatory callback (required in Android 14+)
            mediaProjection!!.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection session stopped by system or user")
                    stopCapture()
                    onProjectionStoppedListener?.invoke()
                }
            }, backgroundHandler)

            // 4. Calculate downsampled dimensions for AI processing while preserving aspect ratio
            calculateDimensions()

            // 5. Pre-allocate Zero-GC Triple-Buffer Pool
            for (i in 0 until BITMAP_POOL_SIZE) {
                bitmapPool[i] = Bitmap.createBitmap(captureWidth, captureHeight, Bitmap.Config.ARGB_8888)
            }
            currentBufferIndex = 0
            cleanRowBuffer = ByteBuffer.allocateDirect(captureWidth * captureHeight * 4)
            Log.d(TAG, "Zero-GC Triple-Buffer Pool pre-allocated: $BITMAP_POOL_SIZE x ${captureWidth}x${captureHeight} (${(BITMAP_POOL_SIZE * captureWidth * captureHeight * 4) / 1024} KB)")

            // 6. Create ImageReader with maxImages = 2
            imageReader = ImageReader.newInstance(
                captureWidth,
                captureHeight,
                PixelFormat.RGBA_8888,
                2
            )

            imageReader!!.setOnImageAvailableListener({ reader ->
                handleImageAvailable(reader)
            }, backgroundHandler)

            // 7. Create VirtualDisplay mirroring system screen
            val densityDpi = context.resources.displayMetrics.densityDpi
            virtualDisplay = mediaProjection!!.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                captureWidth,
                captureHeight,
                densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface,
                null,
                backgroundHandler
            )

            lastFrameTimeMillis = SystemClock.elapsedRealtime()
            fpsLastCalcTime = SystemClock.elapsedRealtime()
            totalCapturedFrames = 0L
            totalSkippedFrames = 0L

            _metrics.update {
                it.copy(
                    isCapturing = true,
                    targetFps = targetFps,
                    width = captureWidth,
                    height = captureHeight
                )
            }

            Log.i(TAG, "MediaProjectionCaptureEngine started successfully: ${captureWidth}x${captureHeight} @ ${targetFps} FPS")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting MediaProjection capture: ${e.message}", e)
            stopCapture()
            return false
        }
    }

    /**
     * Handles new frame event on background thread with strict FPS throttling and Zero-GC pixel transfer.
     */
    private fun handleImageAvailable(reader: ImageReader) {
        val now = SystemClock.elapsedRealtime()

        // Screen lock/off optimization: skip frames when display is not interactive
        if (!isScreenInteractive) {
            val droppedImage = reader.acquireLatestImage()
            droppedImage?.close()
            totalSkippedFrames++
            return
        }

        // Adaptive FPS Decimation: Drop frame immediately if arrived before target interval
        if (now - lastFrameTimeMillis < targetIntervalMillis) {
            val droppedImage = reader.acquireLatestImage()
            droppedImage?.close()
            totalSkippedFrames++
            return
        }

        val image = try {
            reader.acquireLatestImage() ?: return
        } catch (e: Exception) {
            Log.w(TAG, "Failed acquiring latest image: ${e.message}")
            return
        }

        try {
            lastFrameTimeMillis = now

            val plane = image.planes[0]
            val buffer = plane.buffer
            val pixelStride = plane.pixelStride
            val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * captureWidth

            // Obtain current ownership-safe buffer from pool
            val targetBitmap = bitmapPool[currentBufferIndex] ?: return

            // Zero-GC pixel transfer: Reuse pre-allocated buffer
            if (rowPadding == 0) {
                buffer.position(0)
                targetBitmap.copyPixelsFromBuffer(buffer)
            } else {
                val cleanBuffer = cleanRowBuffer ?: return
                cleanBuffer.rewind()
                for (y in 0 until captureHeight) {
                    buffer.position(y * rowStride)
                    val oldLimit = buffer.limit()
                    buffer.limit(y * rowStride + captureWidth * pixelStride)
                    cleanBuffer.put(buffer)
                    buffer.limit(oldLimit)
                }
                cleanBuffer.rewind()
                targetBitmap.copyPixelsFromBuffer(cleanBuffer)
            }

            // Delta Check: Skip identical static frames to preserve battery
            if (isFrameStatic(targetBitmap)) {
                totalSkippedFrames++
                return
            }

            totalCapturedFrames++
            fpsCounter++

            // Calculate actual FPS every 1000ms
            if (now - fpsLastCalcTime >= 1000L) {
                currentCalculatedFps = (fpsCounter * 1000f) / (now - fpsLastCalcTime)
                fpsCounter = 0
                fpsLastCalcTime = now

                _metrics.update {
                    it.copy(
                        actualFps = currentCalculatedFps,
                        framesCaptured = totalCapturedFrames,
                        framesSkipped = totalSkippedFrames
                    )
                }

                Log.d(TAG, "Capture Active: Frame #$totalCapturedFrames ($captureWidth x $captureHeight) @ %.1f FPS (Skipped: $totalSkippedFrames)".format(currentCalculatedFps))
            }

            // Give downstream a separately owned bitmap; the capture pool can now rotate safely.
            if (_frameFlow.subscriptionCount.value > 0) {
                val snapshot = targetBitmap.copy(Bitmap.Config.ARGB_8888, false)
                if (!_frameFlow.tryEmit(snapshot)) {
                    snapshot.recycle()
                }
            }
            currentBufferIndex = (currentBufferIndex + 1) % BITMAP_POOL_SIZE

        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame: ${e.message}", e)
        } finally {
            image.close()
        }
    }

    /**
     * Evaluates an 8x8 sparse sample grid (64 pixels) to detect if screen is static.
     * Skips frame processing after 4 consecutive unchanged samples.
     */
    private fun isFrameStatic(bitmap: Bitmap): Boolean {
        val stepX = (bitmap.width / 9).coerceAtLeast(1)
        val stepY = (bitmap.height / 9).coerceAtLeast(1)
        var hasChanged = false
        var idx = 0

        for (i in 1..8) {
            for (j in 1..8) {
                val x = (i * stepX).coerceAtMost(bitmap.width - 1)
                val y = (j * stepY).coerceAtMost(bitmap.height - 1)
                val pixel = bitmap.getPixel(x, y)
                sampleGrid[idx] = pixel
                if (pixel != prevSampleGrid[idx]) {
                    hasChanged = true
                }
                idx++
            }
        }

        System.arraycopy(sampleGrid, 0, prevSampleGrid, 0, 64)

        if (!hasChanged) {
            staticConsecutiveFrames++
            if (staticConsecutiveFrames > 4) {
                return true
            }
        } else {
            staticConsecutiveFrames = 0
        }
        return false
    }

    private fun calculateDimensions() {
        val screenWidth: Int
        val screenHeight: Int

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }

        // Downscale to target width 360px while maintaining exact screen aspect ratio
        val targetWidth = (screenWidth / 3).coerceAtLeast(360)
        val targetHeight = (screenHeight * targetWidth) / screenWidth

        // Make dimensions divisible by 2 for hardware buffer alignment
        captureWidth = (targetWidth / 2) * 2
        captureHeight = (targetHeight / 2) * 2
    }

    /**
     * Cleanly stops capture and frees all native resources and buffers.
     */
    @Synchronized
    fun stopCapture() {
        Log.d(TAG, "Stopping MediaProjectionCaptureEngine...")

        try {
            virtualDisplay?.release()
            virtualDisplay = null

            imageReader?.close()
            imageReader = null

            mediaProjection?.stop()
            mediaProjection = null
            com.safescreen.app.services.permission.PermissionManager.clearMediaProjectionConsent()

            handlerThread?.quitSafely()
            handlerThread = null
            backgroundHandler = null

            for (i in bitmapPool.indices) {
                bitmapPool[i]?.recycle()
                bitmapPool[i] = null
            }
            currentBufferIndex = 0
            cleanRowBuffer = null

            _metrics.update {
                it.copy(isCapturing = false, actualFps = 0f)
            }

            Log.i(TAG, "MediaProjectionCaptureEngine stopped and resources released cleanly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capture engine: ${e.message}", e)
        }
    }

    /**
     * Pauses or resumes frame decimation when the screen locks/unlocks.
     */
    fun setScreenInteractive(interactive: Boolean) {
        isScreenInteractive = interactive
        Log.d(TAG, "MediaProjectionCaptureEngine screen interactive: $interactive")
    }

    companion object {
        private const val TAG = "SafeScreenCapture"
        private const val VIRTUAL_DISPLAY_NAME = "SafeScreenVirtualDisplay"
        private const val BITMAP_POOL_SIZE = 3
    }
}
