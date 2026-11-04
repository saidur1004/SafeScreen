package com.safescreen.app.data.local

import com.safescreen.app.data.local.converters.RoomConverters
import com.safescreen.app.data.local.entity.ThreatAction
import com.safescreen.app.data.local.entity.ThreatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomConvertersTest {

    private val converters = RoomConverters()

    @Test
    fun testThreatTypeConversion() {
        assertEquals("VISUAL_NSFW", converters.fromThreatType(ThreatType.VISUAL_NSFW))
        assertEquals("AUDIO_EXPLICIT", converters.fromThreatType(ThreatType.AUDIO_EXPLICIT))
        assertEquals("REELS_LIMIT", converters.fromThreatType(ThreatType.REELS_LIMIT))
        assertNull(converters.fromThreatType(null))

        assertEquals(ThreatType.VISUAL_NSFW, converters.toThreatType("VISUAL_NSFW"))
        assertEquals(ThreatType.AUDIO_EXPLICIT, converters.toThreatType("AUDIO_EXPLICIT"))
        assertEquals(ThreatType.REELS_LIMIT, converters.toThreatType("REELS_LIMIT"))
        assertNull(converters.toThreatType(null))
        // Default fallback for unknown value
        assertEquals(ThreatType.VISUAL_NSFW, converters.toThreatType("UNKNOWN_TYPE"))
    }

    @Test
    fun testThreatActionConversion() {
        assertEquals("BLURRED", converters.fromThreatAction(ThreatAction.BLURRED))
        assertEquals("MUTED", converters.fromThreatAction(ThreatAction.MUTED))
        assertEquals("BLOCKED", converters.fromThreatAction(ThreatAction.BLOCKED))
        assertNull(converters.fromThreatAction(null))

        assertEquals(ThreatAction.BLURRED, converters.toThreatAction("BLURRED"))
        assertEquals(ThreatAction.MUTED, converters.toThreatAction("MUTED"))
        assertEquals(ThreatAction.BLOCKED, converters.toThreatAction("BLOCKED"))
        assertNull(converters.toThreatAction(null))
        // Default fallback for unknown value
        assertEquals(ThreatAction.BLURRED, converters.toThreatAction("UNKNOWN_ACTION"))
    }
}

