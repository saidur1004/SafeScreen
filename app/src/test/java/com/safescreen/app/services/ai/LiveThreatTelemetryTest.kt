package com.safescreen.app.services.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveThreatTelemetryTest {

    @Test
    fun testDefaultTelemetryState() {
        val telemetry = LiveThreatTelemetry()
        assertTrue(telemetry.isShieldActive)
        assertFalse(telemetry.isVisualThreat)
        assertEquals(0f, telemetry.visualConfidence, 0.001f)
        assertEquals("Clean", telemetry.visualLabel)
        assertFalse(telemetry.isAudioThreat)
        assertEquals(0f, telemetry.audioConfidence, 0.001f)
        assertEquals("Clean", telemetry.audioCategory)
        assertEquals(0, telemetry.totalThreatsIntercepted)
    }

    @Test
    fun testUpdatedThreatTelemetry() {
        val telemetry = LiveThreatTelemetry(
            isShieldActive = true,
            isVisualThreat = true,
            visualConfidence = 0.96f,
            visualLabel = "porn",
            isAudioThreat = false,
            audioConfidence = 0.02f,
            audioCategory = "Speech",
            totalThreatsIntercepted = 5
        )
        assertTrue(telemetry.isShieldActive)
        assertTrue(telemetry.isVisualThreat)
        assertEquals(0.96f, telemetry.visualConfidence, 0.001f)
        assertEquals("porn", telemetry.visualLabel)
        assertEquals(5, telemetry.totalThreatsIntercepted)
    }
}
