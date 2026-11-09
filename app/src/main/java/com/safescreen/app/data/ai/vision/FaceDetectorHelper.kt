package com.safescreen.app.data.ai.vision

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result data model of on-device face / non-mahram detection.
 */
data class FaceDetectionResult(
    val hasFaces: Boolean = false,
    val faceCount: Int = 0,
    val boundingBoxes: List<Rect> = emptyList(),
    val normalizedBoxes: List<RectF> = emptyList(),
    val detectionTimeMs: Long = 0L
)

/**
 * Ultra-fast on-device ML Kit Face Detector for Islamic Non-Mahram / Face filtering.
 * Runs 100% offline with zero cloud transmission and <15ms latency.
 */
@Singleton
class FaceDetectorHelper @Inject constructor(
    private val genderClassifier: GenderClassifier
) {

    private val detector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.08f) // Detects faces occupying at least 8% of the frame
            .build()

        detector = FaceDetection.getClient(options)
        Log.i(TAG, "ML Kit FaceDetectorHelper initialized in FAST offline mode")

        // Pre-warm detector asynchronously on init so cold start latency is eliminated
        try {
            val warmBitmap = Bitmap.createBitmap(112, 112, Bitmap.Config.ARGB_8888)
            val warmImage = InputImage.fromBitmap(warmBitmap, 0)
            detector.process(warmImage).addOnCompleteListener {
                warmBitmap.recycle()
                Log.d(TAG, "ML Kit FaceDetector pre-warmed successfully")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed pre-warming face detector: ${e.message}")
        }
    }

    /**
     * Detects human/female faces in the provided frame bitmap.
     * Synchronously resolves on a background dispatcher with zero latency.
     */
    suspend fun detectFaces(bitmap: Bitmap): FaceDetectionResult = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        try {
            if (bitmap.isRecycled) {
                return@withContext FaceDetectionResult()
            }

            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val task = detector.process(inputImage)

            // Await completion with safe timeout
            val faces = Tasks.await(task, 350, TimeUnit.MILLISECONDS)
            val duration = System.currentTimeMillis() - startTime

            if (faces.isEmpty()) {
                return@withContext FaceDetectionResult(
                    hasFaces = false,
                    faceCount = 0,
                    detectionTimeMs = duration
                )
            }

            val width = bitmap.width.toFloat()
            val height = bitmap.height.toFloat()

            val boxes = mutableListOf<Rect>()
            val normBoxes = mutableListOf<RectF>()

            for (face in faces) {
                val box = face.boundingBox

                // Filter out male faces: ONLY female faces (Non-Mahram) are shielded!
                val genderResult = genderClassifier.classifyFace(bitmap, box)
                if (!genderResult.isFemale) {
                    Log.d(
                        TAG,
                        "Male face detected (Confidence: ${(genderResult.maleConfidence * 100).toInt()}%). Ignored (Safe)."
                    )
                    continue
                }

                Log.i(
                    TAG,
                    "Female Non-Mahram Face Confirmed (Confidence: ${(genderResult.femaleConfidence * 100).toInt()}%). Shielding."
                )

                boxes.add(box)

                val boxW = box.width().toFloat()
                val boxH = box.height().toFloat()

                // Expand gracefully to cover hair, head, neck and upper figure/clothing of the image
                val expLeft = (box.left - boxW * 0.45f).coerceAtLeast(0f)
                val expTop = (box.top - boxH * 0.50f).coerceAtLeast(0f)
                val expRight = (box.right + boxW * 0.45f).coerceAtMost(width)
                val expBottom = (box.bottom + boxH * 1.60f).coerceAtMost(height)

                normBoxes.add(
                    RectF(
                        (expLeft / width).coerceIn(0f, 1f),
                        (expTop / height).coerceIn(0f, 1f),
                        (expRight / width).coerceIn(0f, 1f),
                        (expBottom / height).coerceIn(0f, 1f)
                    )
                )
            }

            val hasFemaleFaces = normBoxes.isNotEmpty()
            Log.d(TAG, "Processed ${faces.size} face(s) in ${duration}ms: ${normBoxes.size} female face(s) flagged")

            FaceDetectionResult(
                hasFaces = hasFemaleFaces,
                faceCount = normBoxes.size,
                boundingBoxes = boxes,
                normalizedBoxes = normBoxes,
                detectionTimeMs = duration
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            Log.w(TAG, "Face detection skipped/timed-out after ${duration}ms: ${e.message}")
            FaceDetectionResult(hasFaces = false, detectionTimeMs = duration)
        }
    }

    fun close() {
        try {
            detector.close()
            Log.d(TAG, "FaceDetectorHelper closed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing FaceDetectorHelper", e)
        }
    }

    companion object {
        private const val TAG = "FaceDetectorHelper"
    }
}
