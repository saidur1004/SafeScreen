package com.safescreen.app.presentation.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation ViewModel for Reels & Shorts Control Screen (Module 04 / Screen 07).
 * Connects directly to [UserPreferencesRepository] for persistent screen-time policies.
 */
@HiltViewModel
class ReelsControlViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _managedApps = MutableStateFlow(defaultManagedApps)
    private val _availableApps = MutableStateFlow(defaultAvailableApps)
    private val _selectedStrategy = MutableStateFlow(InterventionStrategy.HARD_LOCKOUT)
    private val _showAddAppSheet = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ReelsUiState> = combine(
        preferencesRepository.isReelsBlockerEnabledFlow,
        preferencesRepository.reelsDailyLimitMinutesFlow,
        _managedApps,
        _availableApps,
        _selectedStrategy
    ) { blockerEnabled, dailyLimit, managed, available, strategy ->
        ReelsUiState(
            isStrictModeEnabled = blockerEnabled,
            managedApps = managed.map {
                // Keep the primary daily limit synchronized with user preferences
                if (it.id == "instagram" || it.id == "tiktok" || it.id == "youtube") {
                    it.copy(timeLimitMinutes = dailyLimit)
                } else {
                    it
                }
            },
            selectedStrategy = strategy,
            showAddAppSheet = _showAddAppSheet.value,
            availableAppsToAdd = available,
            searchQuery = _searchQuery.value
        )
    }.combine(_showAddAppSheet) { state, showSheet ->
        state.copy(showAddAppSheet = showSheet)
    }.combine(_searchQuery) { state, query ->
        state.copy(searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReelsUiState()
    )

    fun onEvent(event: ReelsUiEvent) {
        when (event) {
            is ReelsUiEvent.ToggleStrictMode -> {
                viewModelScope.launch {
                    val current = uiState.value.isStrictModeEnabled
                    preferencesRepository.setReelsBlockerEnabled(!current)
                }
            }
            is ReelsUiEvent.UpdateAppTimeLimit -> {
                _managedApps.update { list ->
                    list.map {
                        if (it.id == event.appId) it.copy(timeLimitMinutes = event.minutes) else it
                    }
                }
                viewModelScope.launch {
                    preferencesRepository.setReelsDailyLimitMinutes(event.minutes)
                }
            }
            is ReelsUiEvent.ToggleAppEnabled -> {
                _managedApps.update { list ->
                    list.map {
                        if (it.id == event.appId) it.copy(isEnabled = event.enabled) else it
                    }
                }
            }
            is ReelsUiEvent.SelectStrategy -> {
                _selectedStrategy.value = event.strategy
            }
            ReelsUiEvent.OpenAddAppSheet -> {
                _showAddAppSheet.value = true
            }
            ReelsUiEvent.DismissAddAppSheet -> {
                _showAddAppSheet.value = false
            }
            is ReelsUiEvent.AddApp -> {
                _managedApps.update { it + event.app }
                _availableApps.update { it.filter { item -> item.id != event.app.id } }
                _showAddAppSheet.value = false
            }
            is ReelsUiEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.query
            }
            else -> Unit
        }
    }
}

