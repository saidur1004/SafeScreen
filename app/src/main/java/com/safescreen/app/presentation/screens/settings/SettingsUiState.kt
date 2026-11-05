package com.safescreen.app.presentation.screens.settings

import androidx.compose.runtime.Immutable

/**
 * AI Sensitivity Level for on-device NSFW & Audio detection.
 */
enum class SensitivityLevel(val displayName: String) {
    STRICT("Strict"),
    BALANCED("Balanced"),
    RELAXED("Relaxed")
}

/**
 * On-device ML engine performance profiles specifying inspection frame rate.
 */
enum class PerformanceProfile(val fps: Int, val label: String) {
    BATTERY_SAVER(3, "Battery Saver\n(3 FPS)"),
    BALANCED(5, "Balanced\n(5 FPS)"),
    HIGH_PRECISION(8, "High Precision\n(8 FPS)")
}

/**
 * UI State for SettingsScreen.
 */
@Immutable
data class SettingsUiState(
    val sensitivityLevel: SensitivityLevel = SensitivityLevel.BALANCED,
    val performanceProfile: PerformanceProfile = PerformanceProfile.BALANCED,
    val isBiometricLockEnabled: Boolean = true,
    val isPreventUninstallEnabled: Boolean = true,
    val isPinEnrolled: Boolean = false,
    val whitelistCount: Int = 0,
    val showClearThreatHistoryDialog: Boolean = false,
    val showLicensesDialog: Boolean = false,
    val appVersion: String = "v1.0.0"
)

/**
 * UI Events emitted from SettingsScreen.
 */
sealed interface SettingsUiEvent {
    data class SetSensitivity(val level: SensitivityLevel) : SettingsUiEvent
    data class SetPerformanceProfile(val profile: PerformanceProfile) : SettingsUiEvent
    data class SetBiometricLock(val enabled: Boolean) : SettingsUiEvent
    data class SetPreventUninstall(val enabled: Boolean) : SettingsUiEvent
    data object RequestClearThreatHistory : SettingsUiEvent
    data object ConfirmClearThreatHistory : SettingsUiEvent
    data object DismissClearThreatHistoryDialog : SettingsUiEvent
    data object OpenLicensesDialog : SettingsUiEvent
    data object DismissLicensesDialog : SettingsUiEvent
    data object NavigateToWhitelist : SettingsUiEvent
    data object NavigateToSecurityLock : SettingsUiEvent
    data object NavigateBack : SettingsUiEvent
    data object OpenPrivacyPolicy : SettingsUiEvent
    data object OpenLicenses : SettingsUiEvent
}

