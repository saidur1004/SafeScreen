package com.safescreen.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val whitelistDao: WhitelistDao
) : ViewModel() {

    private val _showClearCacheDialog = MutableStateFlow(false)
    private val _isCacheCleared = MutableStateFlow(false)

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.userPreferencesFlow,
        whitelistDao.getExemptedCountFlow().catch { emit(5) },
        _showClearCacheDialog,
        _isCacheCleared
    ) { prefs, exemptedCount, showDialog, cacheCleared ->
        SettingsUiState(
            sensitivityLevel = prefs.aiSensitivity,
            performanceProfile = prefs.performanceProfile,
            isBiometricLockEnabled = prefs.isBiometricEnabled,
            isPreventUninstallEnabled = prefs.isPreventUninstallEnabled,
            whitelistCount = exemptedCount,
            showClearCacheDialog = showDialog,
            isCacheCleared = cacheCleared,
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
                    userPreferencesRepository.setBiometricEnabled(event.enabled)
                }
            }
            is SettingsUiEvent.SetPreventUninstall -> {
                viewModelScope.launch {
                    userPreferencesRepository.setPreventUninstallEnabled(event.enabled)
                }
            }
            is SettingsUiEvent.RequestClearCache -> {
                _showClearCacheDialog.value = true
            }
            is SettingsUiEvent.DismissClearCacheDialog -> {
                _showClearCacheDialog.value = false
            }
            is SettingsUiEvent.ConfirmClearCache -> {
                _showClearCacheDialog.value = false
                _isCacheCleared.value = true
            }
            else -> Unit
        }
    }
}

