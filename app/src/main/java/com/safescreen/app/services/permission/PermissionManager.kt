package com.safescreen.app.services.permission

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.safescreen.app.presentation.screens.permission.PermissionType
import com.safescreen.app.services.SafeScreenAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * System Permission Manager for SafeScreen.
 * Bridges real Android OS permissions with UI state and background services.
 */
@Singleton
class PermissionManager @Inject constructor() {

    fun isPermissionGranted(context: Context, type: PermissionType): Boolean {
        return when (type) {
            PermissionType.OVERLAY -> isOverlayPermissionGranted(context)
            PermissionType.NOTIFICATION -> isNotificationPermissionGranted(context)
            PermissionType.AUDIO_CAPTURE -> isAudioCapturePermissionGranted(context)
            PermissionType.ACCESSIBILITY -> isAccessibilityServiceEnabled(context)
            PermissionType.SCREEN_CAPTURE -> isScreenCaptureGranted()
        }
    }

    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isAudioCapturePermissionGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, SafeScreenAccessibilityService::class.java).flattenToString()
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName, ignoreCase = true) ||
                componentName.contains("SafeScreenAccessibilityService", ignoreCase = true)
            ) {
                return true
            }
        }
        return false
    }

    fun isBatteryOptimizationExempted(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun isScreenCaptureGranted(): Boolean {
        return hasMediaProjectionConsent
    }

    companion object {
        var hasMediaProjectionConsent: Boolean = false
        var mediaProjectionResultCode: Int = 0
        var mediaProjectionResultData: Intent? = null

        fun setMediaProjectionResult(resultCode: Int, data: Intent?) {
            mediaProjectionResultCode = resultCode
            mediaProjectionResultData = data
            hasMediaProjectionConsent = (resultCode == android.app.Activity.RESULT_OK && data != null)
        }

        fun clearMediaProjectionConsent() {
            hasMediaProjectionConsent = false
            mediaProjectionResultCode = 0
            mediaProjectionResultData = null
        }
    }
}

