package com.safescreen.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.data.local.entity.ThreatType
import com.safescreen.app.domain.usecase.StartShieldUseCase
import com.safescreen.app.domain.usecase.StopShieldUseCase
import com.safescreen.app.services.permission.PermissionManager
import com.safescreen.app.services.ShieldProtectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Presentation ViewModel for the Home Dashboard.
 * Integrates directly with Jetpack DataStore and Room Database for reactive SSOT state.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val threatLogDao: ThreatLogDao,
    private val aiShieldCoordinator: com.safescreen.app.services.ai.AiShieldCoordinator,
    private val startShieldUseCase: StartShieldUseCase,
    private val stopShieldUseCase: StopShieldUseCase
) : ViewModel() {

    private val _showBlurPreviewSheet = MutableStateFlow(false)

    private val startOfDayMillis: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private val homeDataFlow = combine(
        userPreferencesRepository.userPreferencesFlow,
        threatLogDao.getTodayThreatCountByTypeFlow(ThreatType.VISUAL_NSFW, startOfDayMillis)
            .catch { emit(0) },
        threatLogDao.getTodayThreatCountByTypeFlow(ThreatType.AUDIO_EXPLICIT, startOfDayMillis)
            .catch { emit(0) },
        aiShieldCoordinator.telemetry,
        _showBlurPreviewSheet
    ) { prefs, visualCount, audioCount, telemetry, showSheet ->
        HomeUiState(
            isShieldActive = prefs.isShieldActive,
            isPinLockEnabled = prefs.isPinLockEnabled,
            blurredTodayCount = visualCount,
            mutedTodayCount = audioCount,
            reelsMinutesUsed = prefs.reelsMinutesUsedToday,
            reelsMinutesLimit = prefs.reelsDailyLimitMinutes,
            isVisualShieldEnabled = prefs.isVisualShieldEnabled,
            isAudioMuteEnabled = prefs.isAudioMuteEnabled,
            isReelsBlockerEnabled = prefs.isReelsBlockerEnabled,
            showBlurPreviewSheet = showSheet,
            currentBlurStyle = prefs.blurStyle,
            currentBlurIntensity = prefs.blurIntensity,
            isAiVisualThreat = telemetry.isVisualThreat,
            visualConfidence = telemetry.visualConfidence,
            visualLabel = telemetry.visualLabel,
            isAiAudioThreat = telemetry.isAudioThreat,
            audioConfidence = telemetry.audioConfidence,
            audioCategory = telemetry.audioCategory
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        homeDataFlow,
        ShieldProtectionService.isServiceRunning
    ) { homeState, serviceRunning ->
        homeState.copy(
            isShieldActive = homeState.isShieldActive &&
                PermissionManager.hasMediaProjectionConsent && serviceRunning
        )
    }.catch { error ->
        // Graceful fallback to default state on any unexpected flow exception
        emit(HomeUiState(isShieldActive = false))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isShieldActive = false)
    )

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.ToggleShield -> {
                viewModelScope.launch {
                    val current = uiState.value.isShieldActive
                    val newActive = !current
                    userPreferencesRepository.setShieldActive(newActive)
                    if (newActive) {
                        startShieldUseCase()
                    } else {
                        PermissionManager.clearMediaProjectionConsent()
                        stopShieldUseCase()
                    }
                }
            }
            is HomeUiEvent.ConfirmDisarmShield -> {
                viewModelScope.launch {
                    PermissionManager.clearMediaProjectionConsent()
                    userPreferencesRepository.setShieldActive(false)
                    stopShieldUseCase()
                }
            }
            is HomeUiEvent.SetVisualShield -> {
                viewModelScope.launch {
                    userPreferencesRepository.setVisualShieldEnabled(event.enabled)
                }
            }
            is HomeUiEvent.SetAudioMute -> {
                viewModelScope.launch {
                    userPreferencesRepository.setAudioMuteEnabled(event.enabled)
                }
            }
            is HomeUiEvent.SetReelsBlocker -> {
                viewModelScope.launch {
                    userPreferencesRepository.setReelsBlockerEnabled(event.enabled)
                }
            }
            is HomeUiEvent.OpenBlurStyleSheet, is HomeUiEvent.NavigateToBlurPreview -> {
                _showBlurPreviewSheet.value = true
            }
            is HomeUiEvent.DismissBlurStyleSheet -> {
                _showBlurPreviewSheet.value = false
            }
            is HomeUiEvent.ApplyBlurStyle -> {
                _showBlurPreviewSheet.value = false
                viewModelScope.launch {
                    userPreferencesRepository.setBlurStyle(event.style, event.intensity)
                }
            }
            // Navigation and Demo triggers are handled at composable level
            else -> Unit
        }
    }
}

