package com.safescreen.app.data.ai.vision

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VisionClassificationResultTest {

    @Test
    fun testDefaultSafeResult() {
        val result = VisionClassificationResult()
        assertFalse(result.isNSFW)
        assertEquals(0f, result.confidence, 0.001f)
        assertEquals("safe", result.topLabel)
        assertEquals(0f, result.pornScore, 0.001f)
        assertEquals(0f, result.sexyScore, 0.001f)
        assertEquals(0f, result.hentaiScore, 0.001f)
        assertEquals(0L, result.inferenceTimeMs)
    }

    @Test
    fun testThreatDetectionResult() {
        val result = VisionClassificationResult(
            isNSFW = true,
            confidence = 0.94f,
            topLabel = "porn",
            pornScore = 0.94f,
            sexyScore = 0.05f,
            hentaiScore = 0.01f,
            inferenceTimeMs = 12L
        )
        assertTrue(result.isNSFW)
        assertEquals(0.94f, result.confidence, 0.001f)
        assertEquals("porn", result.topLabel)
        assertEquals(12L, result.inferenceTimeMs)
    }
}
