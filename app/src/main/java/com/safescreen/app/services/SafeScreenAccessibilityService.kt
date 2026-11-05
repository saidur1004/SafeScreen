package com.safescreen.app.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.safescreen.app.services.accessibility.ReelsAccessibilityObserver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

/**
 * Accessibility Service for SafeScreen:
 * Performs continuous, on-device detection of short-video scrolling (Instagram Reels,
 * YouTube Shorts, TikTok, Facebook Reels) with zero-cloud exposure and zero UI lag.
 */
@AndroidEntryPoint
class SafeScreenAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var reelsObserver: ReelsAccessibilityObserver

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityServiceEntryPoint {
        fun reelsObserver(): ReelsAccessibilityObserver
    }

    private fun getObserver(): ReelsAccessibilityObserver? {
        if (::reelsObserver.isInitialized) {
            return reelsObserver
        }
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            reelsObserver = entryPoint.reelsObserver()
            reelsObserver
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve ReelsAccessibilityObserver via EntryPoint", e)
            null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val observer = getObserver() ?: return

        try {
            val rootNode = rootInActiveWindow
            observer.onAccessibilityEvent(event, rootNode)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "SafeScreenAccessibilityService interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "SafeScreenAccessibilityService connected successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            getObserver()?.stopTracking()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping reels tracking on destroy", e)
        }
        Log.i(TAG, "SafeScreenAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "SafeScreenA11y"
        var isRunning: Boolean = false
            private set
    }
}
