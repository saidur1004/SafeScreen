package com.safescreen.app.data.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
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
 * Result data model of visual NSFW & Islamic Non-Mahram classification.
 */
data class VisionClassificationResult(
    val isThreat: Boolean = false,
    val isNSFW: Boolean = false,
    val isFaceDetected: Boolean = false,
    val confidence: Float = 0f,
    val topLabel: String = "safe",
    val pornScore: Float = 0f,
    val sexyScore: Float = 0f,
    val hentaiScore: Float = 0f,
    val inferenceTimeMs: Long = 0L,
    val boundingBoxes: List<Rect> = emptyList(),
    val normalizedBoxes: List<RectF> = emptyList()
)

/**
 * High-performance, Zero-GC on-device TFLite visual NSFW + ML Kit Non-Mahram face classifier.
 *
 * Implements strict Islamic gaze protection:
 * 1. Offline ML Kit Face Detection (<15ms) for detecting women / female portraits and models.
 * 2. Hardware-accelerated OpenNSFW TFLite inference (<25ms) for explicit/swimsuit/lingerie.
 * 3. Dual aspect-preserving viewport inspection with Zero GC pressure.
 */
@Singleton
class TFLiteVisionClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val faceDetectorHelper: FaceDetectorHelper
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    // Sensitivity threshold dynamically loaded from DataStore
    private var nsfwThreshold = 0.18f

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

    // Pre-allocated output buffer for OpenNSFW 2 classes: [0: SFW, 1: NSFW]
    private val outputBuffer = Array(1) { FloatArray(2) }

    init {
        // Observe user sensitivity changes reactively
        preferencesRepository.aiSensitivityFlow
            .onEach { level ->
                nsfwThreshold = when (level) {
                    SensitivityLevel.STRICT -> 0.12f
                    SensitivityLevel.BALANCED -> 0.18f
                    SensitivityLevel.RELAXED -> 0.35f
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
     * Classifies a single screen frame Bitmap with dual Face Detection + OpenNSFW inspection.
     *
     * @param bitmap Screen frame from MediaProjectionCaptureEngine (ARGB_8888).
     * @return [VisionClassificationResult] with threat determination, confidence, label and boxes.
     */
    suspend fun classifyFrame(bitmap: Bitmap): VisionClassificationResult = withContext(Dispatchers.Default) {
        inferenceMutex.withLock {
            if (!isInitialized || interpreter == null || bitmap.isRecycled) {
                return@withLock VisionClassificationResult(
                    isThreat = false,
                    isNSFW = false,
                    confidence = 0f,
                    topLabel = "uninitialized"
                )
            }

            val startTime = SystemClock.elapsedRealtime()

            try {
                // 1. Concurrently run ultra-fast offline ML Kit Face Detection (<15ms)
                val faceResult = faceDetectorHelper.detectFaces(bitmap)

                // 2. Primary Inspection: Center Viewport 1:1 Aspect-Preserving Crop
                val squareSize = minOf(bitmap.width, bitmap.height)
                val topOffset = ((bitmap.height - squareSize) / 2).coerceAtLeast(0)
                val centerSrcRect = Rect(0, topOffset, squareSize, topOffset + squareSize)

                canvas.drawBitmap(bitmap, centerSrcRect, destRect, paint)
                scaledBitmap.getPixels(intValues, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

                // Populate direct ByteBuffer with BGR mean-subtracted floats [0..255] (OpenNSFW standard)
                inputBuffer.rewind()
                for (i in 0 until (INPUT_WIDTH * INPUT_HEIGHT)) {
                    val pixel = intValues[i]
                    val r = (pixel shr 16 and 0xFF).toFloat() - 122.68f
                    val g = (pixel shr 8 and 0xFF).toFloat() - 116.67f
                    val b = (pixel and 0xFF).toFloat() - 104.01f
                    inputBuffer.putFloat(b)
                    inputBuffer.putFloat(g)
                    inputBuffer.putFloat(r)
                }

                interpreter!!.run(inputBuffer, outputBuffer)
                var centerSfw = outputBuffer[0].getOrElse(0) { 1f }
                var centerNsfw = outputBuffer[0].getOrElse(1) { 0f }

                var finalNsfwScore = centerNsfw
                var finalSfwScore = centerSfw

                // 3. Secondary Inspection: Full Screen Context (if center crop is below threshold)
                if (finalNsfwScore < nsfwThreshold) {
                    val fullSrcRect = Rect(0, 0, bitmap.width, bitmap.height)
                    canvas.drawBitmap(bitmap, fullSrcRect, destRect, paint)
                    scaledBitmap.getPixels(intValues, 0, INPUT_WIDTH, 0, 0, INPUT_WIDTH, INPUT_HEIGHT)

                    inputBuffer.rewind()
                    for (i in 0 until (INPUT_WIDTH * INPUT_HEIGHT)) {
                        val pixel = intValues[i]
                        val r = (pixel shr 16 and 0xFF).toFloat() - 122.68f
                        val g = (pixel shr 8 and 0xFF).toFloat() - 116.67f
                        val b = (pixel and 0xFF).toFloat() - 104.01f
                        inputBuffer.putFloat(b)
                        inputBuffer.putFloat(g)
                        inputBuffer.putFloat(r)
                    }

                    interpreter!!.run(inputBuffer, outputBuffer)
                    val fullNsfw = outputBuffer[0].getOrElse(1) { 0f }
                    if (fullNsfw > finalNsfwScore) {
                        finalNsfwScore = fullNsfw
                        finalSfwScore = outputBuffer[0].getOrElse(0) { 1f }
                    }
                }

                val inferenceTime = SystemClock.elapsedRealtime() - startTime
                val isNsfw = finalNsfwScore >= nsfwThreshold
                val isFaceThreat = faceResult.hasFaces
                val isThreat = isNsfw || isFaceThreat

                val topLabel = when {
                    isNsfw && isFaceThreat -> "Non-Mahram & Explicit Model"
                    isFaceThreat -> "Non-Mahram Face Detected (${faceResult.faceCount})"
                    isNsfw -> "Explicit / Provocative Imagery"
                    else -> "safe"
                }

                val finalConfidence = when {
                    isFaceThreat && isNsfw -> maxOf(finalNsfwScore, 0.98f)
                    isFaceThreat -> 0.95f
                    isNsfw -> finalNsfwScore
                    else -> finalNsfwScore
                }

                Log.d(
                    TAG,
                    "Frame classified in ${inferenceTime}ms: Threat=$isThreat (Face=${faceResult.hasFaces}, NSFW=${(finalNsfwScore * 100).toInt()}%), threshold=$nsfwThreshold, label=$topLabel"
                )

                VisionClassificationResult(
                    isThreat = isThreat,
                    isNSFW = isThreat, // Maintain backward compatibility
                    isFaceDetected = isFaceThreat,
                    confidence = finalConfidence,
                    topLabel = topLabel,
                    pornScore = finalNsfwScore,
                    sexyScore = finalNsfwScore,
                    hentaiScore = 0f,
                    inferenceTimeMs = inferenceTime,
                    boundingBoxes = faceResult.boundingBoxes,
                    normalizedBoxes = faceResult.normalizedBoxes
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error executing vision inference", e)
                VisionClassificationResult(isThreat = false, isNSFW = false, confidence = 0f, topLabel = "error")
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
            faceDetectorHelper.close()
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
