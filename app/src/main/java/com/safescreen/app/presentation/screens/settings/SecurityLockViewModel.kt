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
import kotlinx.coroutines.flow.first
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
            isLockedOut = true,
            lockoutRemainingSeconds = 0,
            isBiometricHardwareAvailable = true,
            hasVerifiedCurrentPin = false,
            enableLockAfterEnrollment = true,
            isSecurityStateLoaded = false
        )
    )

    init {
        viewModelScope.launch {
            val lockout = userPreferencesRepository.getPinLockoutState()
            val now = System.currentTimeMillis()
            _inputState.update {
                it.copy(
                    failedAttempts = lockout.failedAttempts,
                    isLockedOut = lockout.lockoutUntilEpochMillis > now,
                    lockoutRemainingSeconds = secondsRemaining(lockout.lockoutUntilEpochMillis, now),
                    isSecurityStateLoaded = true
                )
            }
            if (lockout.lockoutUntilEpochMillis > now) {
                runLockoutCountdown(lockout.lockoutUntilEpochMillis)
            }
        }
    }

    val uiState: StateFlow<SecurityUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        _inputState
    ) { prefs, input ->
        SecurityUiState(
            mode = mode,
            enrollmentStage = if (mode == SecurityLockMode.ENROLL_PIN && prefs.isPinSet && !input.hasVerifiedCurrentPin) {
                PinEnrollmentStage.VERIFY_CURRENT_PIN
            } else {
                input.enrollmentStage
            },
            pinInput = input.pinInput,
            maxPinLength = 4,
            isError = input.isError,
            errorMessage = input.errorMessage,
            isSuccess = input.isSuccess,
            failedAttempts = input.failedAttempts,
            isBiometricAvailable = prefs.isBiometricEnabled && input.isBiometricHardwareAvailable &&
                (mode != SecurityLockMode.ENROLL_PIN || (prefs.isPinSet && !input.hasVerifiedCurrentPin)),
            isLockedOut = input.isLockedOut,
            lockoutRemainingSeconds = input.lockoutRemainingSeconds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SecurityUiState(
            mode = mode,
            isLockedOut = true,
            errorMessage = "Checking security status…"
        )
    )

    fun setBiometricHardwareAvailable(available: Boolean) {
        _inputState.update { it.copy(isBiometricHardwareAvailable = available) }
    }

    fun onEvent(event: SecurityUiEvent) {
        when (event) {
            is SecurityUiEvent.EnterDigit -> {
                val current = _inputState.value
                if (!current.isSecurityStateLoaded || current.isLockedOut || current.isSuccess) return

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
                if (!current.isSecurityStateLoaded || current.isLockedOut || current.isSuccess) return

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
                    viewModelScope.launch {
                        val isChangingExistingPin = mode == SecurityLockMode.ENROLL_PIN &&
                            !current.hasVerifiedCurrentPin && userPreferencesRepository.userPreferencesFlow.first().isPinSet
                        userPreferencesRepository.clearPinLockout()
                        if (isChangingExistingPin) {
                            val prefs = userPreferencesRepository.userPreferencesFlow.first()
                            _inputState.update {
                                it.copy(
                                    hasVerifiedCurrentPin = true,
                                    enableLockAfterEnrollment = prefs.isPinLockEnabled,
                                    enrollmentStage = PinEnrollmentStage.ENTER_NEW_PIN,
                                    pinInput = "",
                                    isError = false,
                                    failedAttempts = 0
                                )
                            }
                        } else {
                            _inputState.update { it.copy(isSuccess = true, isError = false, failedAttempts = 0) }
                        }
                    }
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
        if (!current.hasVerifiedCurrentPin) {
            viewModelScope.launch {
                val prefs = userPreferencesRepository.userPreferencesFlow.first()
                if (!prefs.isPinSet) {
                    _inputState.update { it.copy(hasVerifiedCurrentPin = true) }
                    handleEnrollmentPin(pin)
                } else if (userPreferencesRepository.verifyPin(pin, prefs)) {
                    userPreferencesRepository.clearPinLockout()
                    _inputState.update {
                        it.copy(
                            hasVerifiedCurrentPin = true,
                            enableLockAfterEnrollment = prefs.isPinLockEnabled,
                            enrollmentStage = PinEnrollmentStage.ENTER_NEW_PIN,
                            pinInput = "",
                            isError = false,
                            failedAttempts = 0
                        )
                    }
                } else {
                    recordFailedAttempt("Incorrect current PIN")
                }
            }
            return
        }
        when (current.enrollmentStage) {
            PinEnrollmentStage.VERIFY_CURRENT_PIN -> Unit
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
                        userPreferencesRepository.setPin(pin, enableLock = current.enableLockAfterEnrollment)
                        _inputState.update {
                            it.copy(
                                isSuccess = true,
                                isError = false,
                                failedAttempts = 0,
                                isLockedOut = false,
                                lockoutRemainingSeconds = 0
                            )
                        }
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
                userPreferencesRepository.clearPinLockout()
                _inputState.update { it.copy(isSuccess = true, isError = false, failedAttempts = 0) }
            } else {
                recordFailedAttempt("Incorrect PIN")
            }
        }
    }

    private fun recordFailedAttempt(message: String) {
        viewModelScope.launch {
            val status = userPreferencesRepository.recordFailedPinAttempt(
                maxAttempts = MAX_FAILED_ATTEMPTS,
                lockoutDurationMillis = LOCKOUT_DURATION_SECONDS * 1000L
            )
            val lockedOut = status.lockoutUntilEpochMillis > System.currentTimeMillis()
            _inputState.update {
                it.copy(
                    failedAttempts = status.failedAttempts,
                    isLockedOut = lockedOut,
                    lockoutRemainingSeconds = secondsRemaining(status.lockoutUntilEpochMillis),
                    isError = true,
                    errorMessage = if (lockedOut) {
                        "Too many failed attempts. Locked for ${LOCKOUT_DURATION_SECONDS}s"
                    } else {
                        "$message (${MAX_FAILED_ATTEMPTS - status.failedAttempts} attempts remaining)"
                    },
                    pinInput = ""
                )
            }
            if (lockedOut) {
                runLockoutCountdown(status.lockoutUntilEpochMillis)
            } else {
                delay(650)
                _inputState.update { it.copy(isError = false) }
            }
        }
    }

    private fun runLockoutCountdown(lockoutUntilEpochMillis: Long) {
        lockoutJob?.cancel()
        lockoutJob = viewModelScope.launch {
            while (true) {
                val remaining = secondsRemaining(lockoutUntilEpochMillis)
                if (remaining <= 0) break
                _inputState.update {
                    it.copy(
                        isLockedOut = true,
                        lockoutRemainingSeconds = remaining,
                        isError = true,
                        errorMessage = "Too many failed attempts. Try again in ${remaining}s"
                    )
                }
                delay(1000)
            }
            userPreferencesRepository.clearPinLockout()
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

    private fun secondsRemaining(until: Long, now: Long = System.currentTimeMillis()): Int =
        (((until - now).coerceAtLeast(0L) + 999L) / 1000L).toInt()

    private data class InternalSecurityState(
        val enrollmentStage: PinEnrollmentStage,
        val pinInput: String,
        val isError: Boolean,
        val errorMessage: String?,
        val isSuccess: Boolean,
        val failedAttempts: Int,
        val isLockedOut: Boolean,
        val lockoutRemainingSeconds: Int,
        val isBiometricHardwareAvailable: Boolean,
        val hasVerifiedCurrentPin: Boolean,
        val enableLockAfterEnrollment: Boolean,
        val isSecurityStateLoaded: Boolean
    )

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_SECONDS = 30
    }
}

