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
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

/**
 * Presentation ViewModel for Reels & Shorts Control Screen (Module 04 / Screen 07).
 * Connects directly to [UserPreferencesRepository] for persistent screen-time policies.
 * Persists custom added apps and intervention strategies across application restarts.
 */
@HiltViewModel
class ReelsControlViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _showAddAppSheet = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ReelsUiState> = combine(
        preferencesRepository.isReelsBlockerEnabledFlow,
        preferencesRepository.reelsDailyLimitMinutesFlow,
        preferencesRepository.reelsInterventionStrategyFlow,
        preferencesRepository.reelsManagedAppsJsonFlow
    ) { blockerEnabled, dailyLimit, strategyStr, managedAppsJson ->
        val strategy = runCatching {
            InterventionStrategy.valueOf(strategyStr)
        }.getOrDefault(InterventionStrategy.HARD_LOCKOUT)

        val restoredManaged = deserializeManagedApps(managedAppsJson) ?: defaultManagedApps

        val managedWithLimit = restoredManaged.map {
            if (it.id == "instagram" || it.id == "tiktok" || it.id == "youtube") {
                it.copy(timeLimitMinutes = dailyLimit)
            } else {
                it
            }
        }

        val available = defaultAvailableApps.filter { avail ->
            managedWithLimit.none { it.id == avail.id }
        }

        ReelsUiState(
            isStrictModeEnabled = blockerEnabled,
            managedApps = managedWithLimit,
            selectedStrategy = strategy,
            availableAppsToAdd = available
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
                val currentList = uiState.value.managedApps
                val updated = currentList.map {
                    if (it.id == event.appId) it.copy(timeLimitMinutes = event.minutes) else it
                }
                viewModelScope.launch {
                    preferencesRepository.setReelsManagedAppsJson(serializeManagedApps(updated))
                    preferencesRepository.setReelsDailyLimitMinutes(event.minutes)
                }
            }
            is ReelsUiEvent.ToggleAppEnabled -> {
                val currentList = uiState.value.managedApps
                val updated = currentList.map {
                    if (it.id == event.appId) it.copy(isEnabled = event.enabled) else it
                }
                viewModelScope.launch {
                    preferencesRepository.setReelsManagedAppsJson(serializeManagedApps(updated))
                }
            }
            is ReelsUiEvent.SelectStrategy -> {
                viewModelScope.launch {
                    preferencesRepository.setReelsInterventionStrategy(event.strategy.name)
                }
            }
            ReelsUiEvent.OpenAddAppSheet -> {
                _showAddAppSheet.value = true
            }
            ReelsUiEvent.DismissAddAppSheet -> {
                _showAddAppSheet.value = false
            }
            is ReelsUiEvent.AddApp -> {
                val currentList = uiState.value.managedApps
                val updated = currentList + event.app
                _showAddAppSheet.value = false
                viewModelScope.launch {
                    preferencesRepository.setReelsManagedAppsJson(serializeManagedApps(updated))
                }
            }
            is ReelsUiEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.query
            }
            else -> Unit
        }
    }

    private fun serializeManagedApps(apps: List<ManagedAppItem>): String {
        val array = JSONArray()
        for (app in apps) {
            val obj = JSONObject().apply {
                put("id", app.id)
                put("name", app.name)
                put("pkg", app.packageName)
                put("limit", app.timeLimitMinutes)
                put("enabled", app.isEnabled)
                put("brand", app.brandType.name)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeManagedApps(json: String?): List<ManagedAppItem>? {
        if (json.isNullOrBlank()) return null
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<ManagedAppItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ManagedAppItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        packageName = obj.getString("pkg"),
                        timeLimitMinutes = obj.optInt("limit", 60),
                        isEnabled = obj.optBoolean("enabled", true),
                        brandType = runCatching {
                            AppBrandType.valueOf(obj.optString("brand", "GENERIC"))
                        }.getOrDefault(AppBrandType.GENERIC)
                    )
                )
            }
            if (list.isEmpty()) null else list
        } catch (e: Exception) {
            null
        }
    }
}
