package com.safescreen.app.presentation.screens.settings

import androidx.compose.runtime.Immutable

/**
 * Operating mode for SecurityLockScreen.
 */
enum class SecurityLockMode {
    APP_UNLOCK,
    SHIELD_DISARM,
    SETTINGS,
    ENROLL_PIN
}

/**
 * Sequential stages for mandatory PIN enrollment.
 */
enum class PinEnrollmentStage {
    VERIFY_CURRENT_PIN,
    ENTER_NEW_PIN,
    CONFIRM_PIN
}

/**
 * UI State for SecurityLockScreen.
 */
@Immutable
data class SecurityUiState(
    val mode: SecurityLockMode = SecurityLockMode.APP_UNLOCK,
    val enrollmentStage: PinEnrollmentStage = PinEnrollmentStage.ENTER_NEW_PIN,
    val pinInput: String = "",
    val maxPinLength: Int = 4,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val failedAttempts: Int = 0,
    val isBiometricAvailable: Boolean = true,
    val isLockedOut: Boolean = false,
    val lockoutRemainingSeconds: Int = 0
) {
    val isFilled: Boolean get() = pinInput.length >= maxPinLength
}

/**
 * UI Events emitted from SecurityLockScreen.
 */
sealed interface SecurityUiEvent {
    data class EnterDigit(val digit: String) : SecurityUiEvent
    data object DeleteDigit : SecurityUiEvent
    data object ClearPin : SecurityUiEvent
    data object TriggerBiometric : SecurityUiEvent
    data object BiometricSuccess : SecurityUiEvent
    data class BiometricFailed(val errorMsg: String) : SecurityUiEvent
    data object ForgotPin : SecurityUiEvent
    data object NavigateBack : SecurityUiEvent
}

