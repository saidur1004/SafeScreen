package com.safescreen.app.presentation.screens.settings

import androidx.compose.runtime.Immutable

/**
 * UI State for SecurityLockScreen.
 */
@Immutable
data class SecurityUiState(
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
    data object ForgotPin : SecurityUiEvent
    data object NavigateBack : SecurityUiEvent
}
