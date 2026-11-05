package com.safescreen.app.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.safescreen.app.services.accessibility.ReelsAccessibilityObserver
import com.safescreen.app.services.tracker.ForegroundAppTracker
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
 * Also tracks the real-time active foreground application package name for AI Whitelist protection.
 */
@AndroidEntryPoint
class SafeScreenAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var reelsObserver: ReelsAccessibilityObserver

    @Inject
    lateinit var appTracker: ForegroundAppTracker

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityServiceEntryPoint {
        fun reelsObserver(): ReelsAccessibilityObserver
        fun appTracker(): ForegroundAppTracker
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

    private fun getTracker(): ForegroundAppTracker? {
        if (::appTracker.isInitialized) {
            return appTracker
        }
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            appTracker = entryPoint.appTracker()
            appTracker
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve ForegroundAppTracker via EntryPoint", e)
            null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. Extract and track active foreground application package name
        val packageName = event.packageName?.toString()
        if (!packageName.isNullOrBlank()) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                ForegroundAppTracker.currentForegroundPackage == null) {
                getTracker()?.updateForegroundApp(packageName)
                ForegroundAppTracker.update(packageName)
            }
        }

        // 2. Delegate to ReelsAccessibilityObserver for short-video inspection
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
            getTracker()?.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking on destroy", e)
        }
        Log.i(TAG, "SafeScreenAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "SafeScreenA11y"
        var isRunning: Boolean = false
            private set
    }
}
