package com.safescreen.app.data.ai.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result data model for on-device Gender Classification.
 */
data class GenderResult(
    val isFemale: Boolean = false,
    val maleConfidence: Float = 0f,
    val femaleConfidence: Float = 0f,
    val inferenceTimeMs: Long = 0L
)

/**
 * Ultra-fast, 100% Offline Gender Classifier for Non-Mahram Detection.
 *
 * Runs a dedicated TFLite model trained on UTKFace dataset to accurately discern
 * Female vs. Male faces in <10ms per face.
 *
 * Architecture:
 * - Input: [1, 128, 128, 3] Normalized RGB float buffer [0..1]
 * - Output: [1, 2] -> [male_confidence, female_confidence]
 * - Zero-GC pre-allocated direct ByteBuffers
 * - Thread-safe inference via Coroutine Mutex
 */
@Singleton
class GenderClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "GenderClassifier"
        private const val MODEL_PATH = "models/gender.tflite"
        private const val INPUT_SIZE = 128
        private const val FEMALE_THRESHOLD = 0.58f // Strict threshold to prevent false positives on male figures
    }

    private var interpreter: Interpreter? = null
    private val mutex = Mutex()
    private var isInitialized = false

    // Pre-allocated Zero-GC buffers
    private val inputBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4).apply {
        order(ByteOrder.nativeOrder())
    }
    private val outputBuffer = Array(1) { FloatArray(2) }
    private val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
    private val scaledFaceBitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
    private val canvas = android.graphics.Canvas(scaledFaceBitmap)
    private val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
    private val destRect = Rect(0, 0, INPUT_SIZE, INPUT_SIZE)

    init {
        initialize()
    }

    private fun initialize() {
        try {
            val modelBuffer = FileUtil.loadMappedFile(context, MODEL_PATH)
            val options = Interpreter.Options().apply {
                numThreads = 2
                useNNAPI = false
            }
            interpreter = Interpreter(modelBuffer, options)
            isInitialized = true
            Log.i(TAG, "GenderClassifier initialized successfully (Offline TFLite UTKFace Model)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GenderClassifier", e)
            isInitialized = false
        }
    }

    /**
     * Classifies the gender of a face cropped from the full screen frame.
     *
     * @param fullBitmap The full screen frame bitmap.
     * @param faceBox The bounding box of the face in pixel coordinates.
     * @return GenderResult indicating whether the face is female (Non-Mahram).
     */
    suspend fun classifyFace(fullBitmap: Bitmap, faceBox: Rect): GenderResult = withContext(Dispatchers.Default) {
        if (!isInitialized || interpreter == null || fullBitmap.isRecycled) {
            return@withContext GenderResult()
        }

        mutex.withLock {
            val startTime = SystemClock.elapsedRealtime()

            try {
                // 1. Expand tight ML Kit box with 15% context padding matching UTKFace training format
                val boxWidth = (faceBox.right - faceBox.left).coerceAtLeast(1)
                val boxHeight = (faceBox.bottom - faceBox.top).coerceAtLeast(1)
                val padX = (boxWidth * 0.15f).toInt()
                val padY = (boxHeight * 0.15f).toInt()

                val left = (faceBox.left - padX).coerceIn(0, fullBitmap.width - 1)
                val top = (faceBox.top - padY).coerceIn(0, fullBitmap.height - 1)
                val right = (faceBox.right + padX).coerceIn(left + 1, fullBitmap.width)
                val bottom = (faceBox.bottom + padY).coerceIn(top + 1, fullBitmap.height)

                if ((right - left) < 12 || (bottom - top) < 12) {
                    return@withLock GenderResult()
                }

                // 2. Crop & scale into pre-allocated 128x128 bitmap (Zero-GC)
                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
                val srcRect = Rect(left, top, right, bottom)
                canvas.drawBitmap(fullBitmap, srcRect, destRect, paint)
                scaledFaceBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

                // 3. Populate direct ByteBuffer with normalized RGB float values [0..1]
                inputBuffer.rewind()
                for (i in 0 until (INPUT_SIZE * INPUT_SIZE)) {
                    val pixel = intValues[i]
                    val r = ((pixel shr 16) and 0xFF) / 255.0f
                    val g = ((pixel shr 8) and 0xFF) / 255.0f
                    val b = (pixel and 0xFF) / 255.0f
                    inputBuffer.putFloat(r)
                    inputBuffer.putFloat(g)
                    inputBuffer.putFloat(b)
                }

                // 4. Run inference
                outputBuffer[0][0] = 0f
                outputBuffer[0][1] = 0f
                interpreter!!.run(inputBuffer, outputBuffer)

                val maleScore = outputBuffer[0][0]
                val femaleScore = outputBuffer[0][1]
                val maxScore = Math.max(maleScore, femaleScore)
                val expM = Math.exp((maleScore - maxScore).toDouble())
                val expF = Math.exp((femaleScore - maxScore).toDouble())
                val probFemale = (expF / (expM + expF)).toFloat()
                val probMale = (expM / (expM + expF)).toFloat()
                val isFemale = femaleScore > maleScore && probFemale >= FEMALE_THRESHOLD
                val duration = SystemClock.elapsedRealtime() - startTime

                Log.d(
                    TAG,
                    "Face Gender Classified in ${duration}ms: isFemale=$isFemale (Female=${(probFemale * 100).toInt()}%, Male=${(probMale * 100).toInt()}%)"
                )

                GenderResult(
                    isFemale = isFemale,
                    maleConfidence = probMale,
                    femaleConfidence = probFemale,
                    inferenceTimeMs = duration
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error classifying face gender", e)
                GenderResult()
            }
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        if (!scaledFaceBitmap.isRecycled) {
            scaledFaceBitmap.recycle()
        }
        isInitialized = false
    }
}
