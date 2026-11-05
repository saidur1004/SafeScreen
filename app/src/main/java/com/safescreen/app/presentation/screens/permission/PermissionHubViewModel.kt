package com.safescreen.app.presentation.screens.permission

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.services.permission.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation ViewModel for PermissionHubScreen.
 * Bridges Android OS system permissions directly into reactive Compose state.
 */
@HiltViewModel
class PermissionHubViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionHubUiState())
    val uiState: StateFlow<PermissionHubUiState> = _uiState.asStateFlow()

    fun refreshPermissions(context: Context) {
        val currentList = _uiState.value.permissions
        val updatedList = currentList.map { item ->
            val granted = permissionManager.isPermissionGranted(context, item.type)
            item.copy(isGranted = granted)
        }
        val batteryExempted = permissionManager.isBatteryOptimizationExempted(context)

        _uiState.update {
            it.copy(
                permissions = updatedList,
                isBatteryOptimizedExempted = batteryExempted
            )
        }
    }

    fun onScreenCaptureGranted(resultCode: Int, data: android.content.Intent?) {
        PermissionManager.setMediaProjectionResult(resultCode, data)
        _uiState.update { state ->
            val updated = state.permissions.map {
                if (it.type == PermissionType.SCREEN_CAPTURE) it.copy(isGranted = true) else it
            }
            state.copy(permissions = updated)
        }
    }

    fun openBatteryDialog() {
        _uiState.update { it.copy(showBatteryDialog = true) }
    }

    fun dismissBatteryDialog() {
        _uiState.update { it.copy(showBatteryDialog = false) }
    }

    fun onAllowUnrestrictedBattery(context: Context) {
        _uiState.update {
            it.copy(
                showBatteryDialog = false,
                isBatteryOptimizedExempted = true
            )
        }
        viewModelScope.launch {
            userPreferencesRepository.setBatteryOptimizationAccepted(true)
            // Fix permission bypass: Only mark onboarding complete if all permissions are actually granted
            if (_uiState.value.areAllGranted) {
                userPreferencesRepository.setCompletedOnboarding(true)
            }
        }
    }

    fun completeOnboardingIfAllGranted(): Boolean {
        val allGranted = _uiState.value.areAllGranted
        if (allGranted) {
            viewModelScope.launch {
                userPreferencesRepository.setCompletedOnboarding(true)
            }
        }
        return allGranted
    }
}

