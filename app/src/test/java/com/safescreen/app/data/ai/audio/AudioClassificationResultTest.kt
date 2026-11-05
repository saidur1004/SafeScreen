package com.safescreen.app.data.ai.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioClassificationResultTest {

    @Test
    fun testDefaultCleanAudioResult() {
        val result = AudioClassificationResult()
        assertFalse(result.isExplicit)
        assertEquals(0f, result.confidence, 0.001f)
        assertEquals("Clean", result.topCategory)
        assertEquals(0f, result.explicitScore, 0.001f)
        assertEquals(0L, result.inferenceTimeMs)
    }

    @Test
    fun testExplicitAudioDetectionResult() {
        val result = AudioClassificationResult(
            isExplicit = true,
            confidence = 0.82f,
            topCategory = "Groan",
            explicitScore = 0.82f,
            inferenceTimeMs = 18L
        )
        assertTrue(result.isExplicit)
        assertEquals(0.82f, result.confidence, 0.001f)
        assertEquals("Groan", result.topCategory)
        assertEquals(18L, result.inferenceTimeMs)
    }
}

