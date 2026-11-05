package com.safescreen.app.data.tflite

import android.content.Context
import android.graphics.Bitmap
import com.safescreen.app.data.ai.vision.TFLiteVisionClassifier
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Compatibility bridge delegating to the unified [TFLiteVisionClassifier].
 */
@Singleton
class TFLiteClassifier @Inject constructor(
    private val visionClassifier: TFLiteVisionClassifier
) {

    data class DetectionResult(
        val isNSFW: Boolean,
        val confidence: Float,
        val label: String
    )

    fun initialize(context: Context, modelPath: String = "models/nsfw.tflite") {
        visionClassifier.initialize(modelPath)
    }

    fun classifyFrame(bitmap: Bitmap): DetectionResult {
        val res = runBlocking { visionClassifier.classifyFrame(bitmap) }
        return DetectionResult(
            isNSFW = res.isNSFW,
            confidence = res.confidence,
            label = res.topLabel
        )
    }

    fun close() {
        visionClassifier.close()
    }
}
