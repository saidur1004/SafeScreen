package com.safescreen.app.services.tracker

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton repository tracking the real-time active foreground application package name.
 * Updated by [com.safescreen.app.services.SafeScreenAccessibilityService] via accessibility window events.
 * Consumed by [com.safescreen.app.services.ai.AiShieldCoordinator] to:
 * 1. Exempt whitelisted confidential/banking applications from AI scanning.
 * 2. Accurately tag intercepted threats in Room SQLite audit logs.
 */
@Singleton
class ForegroundAppTracker @Inject constructor() {

    private val _currentPackageName = MutableStateFlow<String?>(null)
    val currentPackageName: StateFlow<String?> = _currentPackageName.asStateFlow()

    val currentPackage: String?
        get() = _currentPackageName.value ?: currentForegroundPackage

    fun updateForegroundApp(packageName: String?) {
        if (packageName.isNullOrBlank()) return

        // Ignore temporary system overlays, keyboards, and SafeScreen's own floating overlays
        if (packageName == SYSTEM_UI_PACKAGE ||
            packageName == "com.safescreen.app" ||
            packageName.contains("inputmethod", ignoreCase = true)) {
            return
        }

        if (packageName != _currentPackageName.value) {
            Log.d(TAG, "Foreground app switched: $packageName")
            _currentPackageName.value = packageName
            currentForegroundPackage = packageName
        }
    }

    fun clear() {
        _currentPackageName.value = null
        currentForegroundPackage = null
    }

    companion object {
        private const val TAG = "ForegroundAppTracker"
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"

        @Volatile
        var currentForegroundPackage: String? = null
            private set

        fun update(packageName: String?) {
            if (packageName.isNullOrBlank()) return
            if (packageName == SYSTEM_UI_PACKAGE || packageName.contains("inputmethod", ignoreCase = true)) {
                return
            }
            currentForegroundPackage = packageName
        }
    }
}

