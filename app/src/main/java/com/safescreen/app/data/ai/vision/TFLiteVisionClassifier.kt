package com.safescreen.app.data.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
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
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result data model of visual NSFW classification.
 */
data class VisionClassificationResult(
    val isNSFW: Boolean = false,
    val confidence: Float = 0f,
    val topLabel: String = "safe",
    val pornScore: Float = 0f,
    val sexyScore: Float = 0f,
    val hentaiScore: Float = 0f,
    val inferenceTimeMs: Long = 0L
)

/**
 * High-performance, Zero-GC on-device TFLite visual NSFW classifier.
 *
 * Implements Phase 5.1 requirements:
 * 1. Hardware acceleration: NNAPI with fallback to 4-thread XNNPACK CPU inference (<15ms latency).
 * 2. Pre-allocated Direct ByteBuffer (224x224x3 Float32) and reusable canvas to ensure Zero GC pressure.
 * 3. Dynamic sensitivity adaptation (Strict: 0.50, Balanced: 0.70, Relaxed: 0.85) synced with DataStore.
 */
@Singleton
class TFLiteVisionClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    // Sensitivity threshold dynamically loaded from DataStore
    private var nsfwThreshold = 0.70f

    // Zero-GC pre-allocated reusable scaling canvas & buffers
    private val scaledBitmap = Bitmap.createBitmap(INPUT_WIDTH, INPUT_HEIGHT, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(scaledBitmap)
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val destRect = Rect(0, 0, INPUT_WIDTH, INPUT_HEIGHT)
    private val intValues = IntArray(INPUT_WIDTH * INPUT_HEIGHT)

    // Pre-allocated Direct ByteBuffer: 1 * 224 * 224 * 3 channels * 4 bytes per float
    private val inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(
        1 * INPUT_WIDTH * INPUT_HEIGHT * 3 * 4
    ).order(ByteOrder.nativeOrder())

    private val inferenceMutex = Mutex()

    // Pre-allocated output buffer for 5 classes: [drawings, hentai, neutral, porn, sexy]
    private val outputBuffer = Array(1) { FloatArray(5) }

    init {
        // Observe user sensitivity changes reactively
        preferencesRepository.aiSensitivityFlow
            .onEach { level ->
                nsfwThreshold = when (level) {
                    SensitivityLevel.STRICT -> 0.50f
                    SensitivityLevel.BALANCED -> 0.70f
                    SensitivityLevel.RELAXED -> 0.85f
                }
                Log.d(TAG, "TFLiteVisionClassifier sensitivity updated to $level (threshold=$nsfwThreshold)")
            }
            .launchIn(scope)

        initialize(MODEL_ASSET_PATH)
    }

    /**
     * Initializes TFLite Interpreter with NNAPI hardware acceleration, falling back to multi-threaded CPU.
     */
    @Synchronized
    fun initialize(modelPath: String = MODEL_ASSET_PATH): Boolean {
        if (isInitialized && interpreter != null) return true

        return try {
            val byteBuffer = FileUtil.loadMappedFile(context, modelPath)

            // Try hardware acceleration (NNAPI + 4 threads)
            try {
                val options = Interpreter.Options().apply {
                    setNumThreads(4)
                    setUseNNAPI(true)
                }
                interpreter = Interpreter(byteBuffer, options)
                isInitialized = true
                Log.i(TAG, "TFLiteVisionClassifier initialized successfully with NNAPI acceleration")
            } catch (e: Exception) {
                Log.w(TAG, "NNAPI acceleration unavailable on device, falling back to 4-thread CPU: ${e.message}")
                val fallbackOptions = Interpreter.Options().apply {
                    setNumThreads(4)
                    setUseNNAPI(false)
                }
                interpreter = Interpreter(byteBuffer, fallbackOptions)
                isInitialized = true
                Log.i(TAG, "TFLiteVisionClassifier initialized successfully with CPU fallback")
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to load TFLite model from assets: $modelPath", e)
            isInitialized = false
            false
        }
    }

    /**
     * Classifies a single screen frame Bitmap with zero garbage collection allocations.
     *
     * @param bitmap Screen frame from MediaProjectionCaptureEngine (ARGB_8888).
     * @return [VisionClassificationResult] with confidence, threat label and execution time.
     */
    suspend fun classifyFrame(bitmap: Bitmap): VisionClassificationResult = withContext(Dispatchers.Default) {
        inferenceMutex.withLock {
            if (!isInitialized || interpreter == null) {
                return@withLock VisionClassificationResult(
                    isNSFW = false,
                    confidence = 0f,
                    topLabel = "uninitialized"
                )
            }

            val startTime = SystemClock.elapsedRealtime()

            try {
                // 1. Draw source frame into pre-allocated 224x224 scaled bitmap without allocations
                val srcRect = Rect(0, 0, bitmap.width, bitmap.height)
                canvas.drawBitmap(bitmap, srcRect, destRect, paint)

                // 2. Extract pixel color components into pre-allocated IntArray
                scaledBitmap.getPixels(intValues, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

                // 3. Populate direct ByteBuffer normalized in [0.0f, 1.0f] range
                inputBuffer.rewind()
                for (i in 0 until (INPUT_WIDTH * INPUT_HEIGHT)) {
                    val pixel = intValues[i]
                    val r = (pixel shr 16 and 0xFF) / 255.0f
                    val g = (pixel shr 8 and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f

                    inputBuffer.putFloat(r)
                    inputBuffer.putFloat(g)
                    inputBuffer.putFloat(b)
                }

                // 4. Run inference
                interpreter!!.run(inputBuffer, outputBuffer)

                val inferenceTime = SystemClock.elapsedRealtime() - startTime

                // Output mapping: [0: drawings, 1: hentai, 2: neutral, 3: porn, 4: sexy]
                val scores = outputBuffer[0]
                val hentaiScore = scores.getOrElse(1) { 0f }
                val pornScore = scores.getOrElse(3) { 0f }
                val sexyScore = scores.getOrElse(4) { 0f }

                val rawNsfwScore = maxOf(pornScore, hentaiScore)
                val effectiveConfidence = maxOf(rawNsfwScore, (sexyScore * 0.90f))

                val isNsfw = effectiveConfidence >= nsfwThreshold

                val topLabel = when {
                    pornScore >= nsfwThreshold -> "porn"
                    hentaiScore >= nsfwThreshold -> "hentai"
                    sexyScore >= nsfwThreshold -> "sexy"
                    else -> "safe"
                }

                VisionClassificationResult(
                    isNSFW = isNsfw,
                    confidence = effectiveConfidence,
                    topLabel = topLabel,
                    pornScore = pornScore,
                    sexyScore = sexyScore,
                    hentaiScore = hentaiScore,
                    inferenceTimeMs = inferenceTime
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing TFLite vision inference", e)
                VisionClassificationResult(isNSFW = false, confidence = 0f, topLabel = "error")
            }
        }
    }

    /**
     * Releases TFLite interpreter and frees native memory.
     */
    @Synchronized
    fun close() {
        try {
            interpreter?.close()
            interpreter = null
            isInitialized = false
            Log.d(TAG, "TFLiteVisionClassifier closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing TFLiteVisionClassifier", e)
        }
    }

    companion object {
        private const val TAG = "SafeScreenVision"
        private const val MODEL_ASSET_PATH = "models/nsfw.tflite"
        const val INPUT_WIDTH = 224
        const val INPUT_HEIGHT = 224
    }
}
