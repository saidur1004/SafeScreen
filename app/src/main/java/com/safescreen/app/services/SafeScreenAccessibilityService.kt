package com.safescreen.app.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility Service skeleton for SafeScreen app and short-video reel blocking.
 * Monitors window changes and content updates on-device with zero-cloud exposure.
 */
class SafeScreenAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handled in Phase 4.3 (Reels / App Blocker node observer)
    }

    override fun onInterrupt() {
        Log.d(TAG, "SafeScreenAccessibilityService interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.d(TAG, "SafeScreenAccessibilityService connected successfully")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d(TAG, "SafeScreenAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "SafeScreenA11y"
        var isRunning: Boolean = false
            private set
    }
}
