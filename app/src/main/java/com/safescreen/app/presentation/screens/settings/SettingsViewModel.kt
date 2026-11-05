package com.safescreen.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation ViewModel for the Settings Screen.
 * Binds AI sensitivity, FPS tuning profiles, and security preferences to Jetpack DataStore and Room DB.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val whitelistDao: WhitelistDao,
    private val threatLogDao: ThreatLogDao
) : ViewModel() {

    private val _showClearThreatHistoryDialog = MutableStateFlow(false)
    private val _showLicensesDialog = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        whitelistDao.getExemptedCountFlow().catch { emit(0) },
        _showClearThreatHistoryDialog,
        _showLicensesDialog
    ) { prefs, exemptedCount, showDialog, showLicenses ->
        SettingsUiState(
            sensitivityLevel = prefs.aiSensitivity,
            performanceProfile = prefs.performanceProfile,
            isBiometricLockEnabled = prefs.isPinLockEnabled,
            isPreventUninstallEnabled = prefs.isPreventUninstallEnabled,
            isPinEnrolled = prefs.isPinSet,
            whitelistCount = exemptedCount,
            showClearThreatHistoryDialog = showDialog,
            showLicensesDialog = showLicenses,
            appVersion = "v1.0.0 (On-Device Build)"
        )
    }.catch {
        emit(SettingsUiState())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SetSensitivity -> {
                viewModelScope.launch {
                    userPreferencesRepository.setAiSensitivity(event.level)
                }
            }
            is SettingsUiEvent.SetPerformanceProfile -> {
                viewModelScope.launch {
                    userPreferencesRepository.setPerformanceProfile(event.profile)
                }
            }
            is SettingsUiEvent.SetBiometricLock -> {
                viewModelScope.launch {
                    val prefs = userPreferencesRepository.userPreferencesFlow.first()
                    if (event.enabled && !prefs.isPinSet) {
                        // User cannot directly enable PIN lock if PIN is not enrolled yet.
                        return@launch
                    }
                    userPreferencesRepository.setPinLockEnabled(event.enabled)
                    userPreferencesRepository.setBiometricEnabled(event.enabled)
                }
            }
            is SettingsUiEvent.SetPreventUninstall -> {
                viewModelScope.launch {
                    userPreferencesRepository.setPreventUninstallEnabled(event.enabled)
                }
            }
            is SettingsUiEvent.RequestClearThreatHistory -> {
                _showClearThreatHistoryDialog.value = true
            }
            is SettingsUiEvent.DismissClearThreatHistoryDialog -> {
                _showClearThreatHistoryDialog.value = false
            }
            is SettingsUiEvent.ConfirmClearThreatHistory -> {
                _showClearThreatHistoryDialog.value = false
                viewModelScope.launch {
                    threatLogDao.clearAllThreatLogs()
                }
            }
            is SettingsUiEvent.OpenLicensesDialog,
            is SettingsUiEvent.OpenLicenses -> {
                _showLicensesDialog.value = true
            }
            is SettingsUiEvent.DismissLicensesDialog -> {
                _showLicensesDialog.value = false
            }
            else -> Unit
        }
    }
}

