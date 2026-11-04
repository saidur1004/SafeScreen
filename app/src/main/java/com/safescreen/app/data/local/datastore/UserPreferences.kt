package com.safescreen.app.data.local.datastore

import com.safescreen.app.presentation.screens.home.BlurStyle
import com.safescreen.app.presentation.screens.settings.PerformanceProfile
import com.safescreen.app.presentation.screens.settings.SensitivityLevel

/**
 * Immutable data snapshot representing the unified user preferences state.
 */
data class UserPreferences(
    val isShieldActive: Boolean = true,
    val isVisualShieldEnabled: Boolean = true,
    val isAudioMuteEnabled: Boolean = true,
    val isReelsBlockerEnabled: Boolean = false,
    val aiSensitivity: SensitivityLevel = SensitivityLevel.BALANCED,
    val performanceProfile: PerformanceProfile = PerformanceProfile.BALANCED,
    val blurStyle: BlurStyle = BlurStyle.FROSTED_GLASS,
    val blurIntensity: Float = 0.85f,
    val isPinLockEnabled: Boolean = false,
    val pinHash: String = "",
    val isBiometricEnabled: Boolean = true,
    val isPreventUninstallEnabled: Boolean = true,
    val reelsDailyLimitMinutes: Int = 30,
    val reelsMinutesUsedToday: Int = 18,
    val hasCompletedOnboarding: Boolean = false,
    val hasAcceptedBatteryOptimization: Boolean = false
)

