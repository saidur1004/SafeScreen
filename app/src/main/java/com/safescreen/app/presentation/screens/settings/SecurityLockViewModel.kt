package com.safescreen.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
 */
@HiltViewModel
class SecurityLockViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _inputState = MutableStateFlow(
        InternalSecurityState(
            pinInput = "",
            isError = false,
            errorMessage = null,
            isSuccess = false,
            failedAttempts = 0
        )
    )

    val uiState: StateFlow<SecurityUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        _inputState
    ) { prefs, input ->
        SecurityUiState(
            pinInput = input.pinInput,
            maxPinLength = 4,
            isError = input.isError,
            errorMessage = input.errorMessage,
            isSuccess = input.isSuccess,
            failedAttempts = input.failedAttempts,
            isBiometricAvailable = prefs.isBiometricEnabled,
            isLockedOut = false,
            lockoutRemainingSeconds = 0
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SecurityUiState()
    )

    fun onEvent(event: SecurityUiEvent) {
        when (event) {
            is SecurityUiEvent.EnterDigit -> {
                val current = _inputState.value
                if (current.pinInput.length < 4 && !current.isSuccess) {
                    val newPin = current.pinInput + event.digit
                    _inputState.update { it.copy(pinInput = newPin, isError = false) }

                    if (newPin.length == 4) {
                        viewModelScope.launch {
                            val verified = userPreferencesRepository.verifyPin(newPin)
                            if (verified) {
                                _inputState.update { it.copy(isSuccess = true, isError = false) }
                            } else {
                                _inputState.update {
                                    it.copy(
                                        isError = true,
                                        errorMessage = "Incorrect PIN. Try again.",
                                        failedAttempts = it.failedAttempts + 1
                                    )
                                }
                                delay(650)
                                _inputState.update { it.copy(pinInput = "", isError = false) }
                            }
                        }
                    }
                }
            }
            is SecurityUiEvent.DeleteDigit -> {
                val current = _inputState.value
                if (current.pinInput.isNotEmpty() && !current.isSuccess) {
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
                _inputState.update { it.copy(isSuccess = true, isError = false) }
            }
            is SecurityUiEvent.ForgotPin -> {
                // Handled in UI
            }
            is SecurityUiEvent.NavigateBack -> {
                // Handled in UI
            }
        }
    }

    private data class InternalSecurityState(
        val pinInput: String,
        val isError: Boolean,
        val errorMessage: String?,
        val isSuccess: Boolean,
        val failedAttempts: Int
    )
}
