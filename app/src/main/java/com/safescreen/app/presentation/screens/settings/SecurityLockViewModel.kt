package com.safescreen.app.presentation.screens.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lifecycle-aware ViewModel for Security PIN & Biometric Screen.
 * Bound to DataStore [UserPreferencesRepository] for credential verification.
 * Implements mandatory PIN enrollment and failed-attempt lockout penalties.
 */
@HiltViewModel
class SecurityLockViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val mode: SecurityLockMode = runCatching {
        val arg = savedStateHandle.get<String>(Screen.SecurityLock.ARG_MODE)
        SecurityLockMode.valueOf(arg ?: "")
    }.getOrDefault(SecurityLockMode.APP_UNLOCK)

    private var firstEnteredEnrollPin: String = ""
    private var lockoutJob: Job? = null

    private val _inputState = MutableStateFlow(
        InternalSecurityState(
            enrollmentStage = PinEnrollmentStage.ENTER_NEW_PIN,
            pinInput = "",
            isError = false,
            errorMessage = null,
            isSuccess = false,
            failedAttempts = 0,
            isLockedOut = false,
            lockoutRemainingSeconds = 0,
            isBiometricHardwareAvailable = true
        )
    )

    val uiState: StateFlow<SecurityUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        _inputState
    ) { prefs, input ->
        SecurityUiState(
            mode = mode,
            enrollmentStage = input.enrollmentStage,
            pinInput = input.pinInput,
            maxPinLength = 4,
            isError = input.isError,
            errorMessage = input.errorMessage,
            isSuccess = input.isSuccess,
            failedAttempts = input.failedAttempts,
            isBiometricAvailable = prefs.isBiometricEnabled && input.isBiometricHardwareAvailable && mode != SecurityLockMode.ENROLL_PIN,
            isLockedOut = input.isLockedOut,
            lockoutRemainingSeconds = input.lockoutRemainingSeconds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SecurityUiState(mode = mode)
    )

    fun setBiometricHardwareAvailable(available: Boolean) {
        _inputState.update { it.copy(isBiometricHardwareAvailable = available) }
    }

    fun onEvent(event: SecurityUiEvent) {
        when (event) {
            is SecurityUiEvent.EnterDigit -> {
                val current = _inputState.value
                if (current.isLockedOut || current.isSuccess) return

                if (current.pinInput.length < 4) {
                    val newPin = current.pinInput + event.digit
                    _inputState.update { it.copy(pinInput = newPin, isError = false) }

                    if (newPin.length == 4) {
                        handleCompletePin(newPin)
                    }
                }
            }
            is SecurityUiEvent.DeleteDigit -> {
                val current = _inputState.value
                if (current.isLockedOut || current.isSuccess) return

                if (current.pinInput.isNotEmpty()) {
                    _inputState.update {
                        it.copy(
                            pinInput = it.pinInput.dropLast(1),
                            isError = false
                        )
                    }
                }
            }
            is SecurityUiEvent.ClearPin -> {
                _inputState.update { it.copy(pinInput = "", isError = false) }
            }
            is SecurityUiEvent.TriggerBiometric -> {
                // Handled in UI via BiometricPrompt launcher
            }
            is SecurityUiEvent.BiometricSuccess -> {
                val current = _inputState.value
                if (!current.isLockedOut) {
                    _inputState.update { it.copy(isSuccess = true, isError = false, failedAttempts = 0) }
                }
            }
            is SecurityUiEvent.BiometricFailed -> {
                _inputState.update {
                    it.copy(isError = true, errorMessage = event.errorMsg)
                }
                viewModelScope.launch {
                    delay(1200)
                    _inputState.update { it.copy(isError = false) }
                }
            }
            is SecurityUiEvent.ForgotPin -> {
                // Testing PIN leak removed. Handled securely.
            }
            is SecurityUiEvent.NavigateBack -> {
                // Handled in UI
            }
        }
    }

    private fun handleCompletePin(pin: String) {
        if (mode == SecurityLockMode.ENROLL_PIN) {
            handleEnrollmentPin(pin)
        } else {
            handleVerificationPin(pin)
        }
    }

    private fun handleEnrollmentPin(pin: String) {
        val current = _inputState.value
        when (current.enrollmentStage) {
            PinEnrollmentStage.ENTER_NEW_PIN -> {
                firstEnteredEnrollPin = pin
                viewModelScope.launch {
                    delay(200)
                    _inputState.update {
                        it.copy(
                            enrollmentStage = PinEnrollmentStage.CONFIRM_PIN,
                            pinInput = "",
                            isError = false
                        )
                    }
                }
            }
            PinEnrollmentStage.CONFIRM_PIN -> {
                if (pin == firstEnteredEnrollPin) {
                    viewModelScope.launch {
                        userPreferencesRepository.setPin(pin)
                        _inputState.update { it.copy(isSuccess = true, isError = false) }
                    }
                } else {
                    viewModelScope.launch {
                        _inputState.update {
                            it.copy(
                                isError = true,
                                errorMessage = "PINs do not match. Try again."
                            )
                        }
                        delay(800)
                        firstEnteredEnrollPin = ""
                        _inputState.update {
                            it.copy(
                                enrollmentStage = PinEnrollmentStage.ENTER_NEW_PIN,
                                pinInput = "",
                                isError = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleVerificationPin(pin: String) {
        viewModelScope.launch {
            val verified = userPreferencesRepository.verifyPin(pin)
            if (verified) {
                _inputState.update { it.copy(isSuccess = true, isError = false, failedAttempts = 0) }
            } else {
                val currentAttempts = _inputState.value.failedAttempts + 1
                if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
                    triggerLockout()
                } else {
                    val remaining = MAX_FAILED_ATTEMPTS - currentAttempts
                    _inputState.update {
                        it.copy(
                            isError = true,
                            errorMessage = "Incorrect PIN ($remaining attempts remaining)",
                            failedAttempts = currentAttempts
                        )
                    }
                    delay(650)
                    _inputState.update { it.copy(pinInput = "", isError = false) }
                }
            }
        }
    }

    private fun triggerLockout() {
        lockoutJob?.cancel()
        _inputState.update {
            it.copy(
                isLockedOut = true,
                lockoutRemainingSeconds = LOCKOUT_DURATION_SECONDS,
                isError = true,
                errorMessage = "Too many failed attempts. Locked for ${LOCKOUT_DURATION_SECONDS}s",
                pinInput = ""
            )
        }
        lockoutJob = viewModelScope.launch {
            for (sec in LOCKOUT_DURATION_SECONDS downTo 1) {
                _inputState.update {
                    it.copy(
                        lockoutRemainingSeconds = sec,
                        errorMessage = "Too many failed attempts. Try again in ${sec}s"
                    )
                }
                delay(1000)
            }
            _inputState.update {
                it.copy(
                    isLockedOut = false,
                    lockoutRemainingSeconds = 0,
                    failedAttempts = 0,
                    isError = false,
                    errorMessage = null
                )
            }
        }
    }

    private data class InternalSecurityState(
        val enrollmentStage: PinEnrollmentStage,
        val pinInput: String,
        val isError: Boolean,
        val errorMessage: String?,
        val isSuccess: Boolean,
        val failedAttempts: Int,
        val isLockedOut: Boolean,
        val lockoutRemainingSeconds: Int,
        val isBiometricHardwareAvailable: Boolean
    )

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_SECONDS = 30
    }
}

