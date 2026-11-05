package com.safescreen.app.services.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReelsAccessibilityObserverTest {

    @Test
    fun testPlatformResolutionFromPackageName() {
        assertEquals(ReelPlatform.INSTAGRAM, ReelPlatform.fromPackageName("com.instagram.android"))
        assertEquals(ReelPlatform.YOUTUBE, ReelPlatform.fromPackageName("com.google.android.youtube"))
        assertEquals(ReelPlatform.TIKTOK, ReelPlatform.fromPackageName("com.zhiliaoapp.musically"))
        assertEquals(ReelPlatform.TIKTOK, ReelPlatform.fromPackageName("com.ss.android.ugc.trill"))
        assertEquals(ReelPlatform.FACEBOOK, ReelPlatform.fromPackageName("com.facebook.katana"))
        assertEquals(ReelPlatform.CHROME, ReelPlatform.fromPackageName("com.android.chrome"))
    }

    @Test
    fun testPlatformResolutionForUnknownPackagesReturnsNull() {
        assertNull(ReelPlatform.fromPackageName("com.example.unknownapp"))
        assertNull(ReelPlatform.fromPackageName("com.android.settings"))
        assertNull(ReelPlatform.fromPackageName("com.whatsapp"))
    }

    @Test
    fun testReelPlatformDisplayNames() {
        assertEquals("Instagram Reels", ReelPlatform.INSTAGRAM.displayName)
        assertEquals("YouTube Shorts", ReelPlatform.YOUTUBE.displayName)
        assertEquals("TikTok", ReelPlatform.TIKTOK.displayName)
        assertEquals("Facebook Reels", ReelPlatform.FACEBOOK.displayName)
        assertEquals("Web Shorts", ReelPlatform.CHROME.displayName)
    }

    @Test
    fun testTikTokCoverageIs100PercentShorts() {
        val tiktokPackages = ReelPlatform.TIKTOK.packageNames
        assertTrue(tiktokPackages.contains("com.zhiliaoapp.musically"))
        assertTrue(tiktokPackages.contains("com.ss.android.ugc.trill"))
    }
}

