package com.safescreen.app.presentation.screens.focus

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.entity.FocusScheduleEntity
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lifecycle-aware ViewModel for Focus Schedules screen.
 * Bound to Room Database [FocusScheduleDao] as SSOT.
 */
@HiltViewModel
class FocusScheduleViewModel @Inject constructor(
    private val focusScheduleDao: FocusScheduleDao
) : ViewModel() {

    private val _sheetState = MutableStateFlow(
        SheetInputState(
            showAddScheduleSheet = false,
            newScheduleTitle = "",
            newStartTime = "08:00 PM",
            newEndTime = "11:00 PM",
            newActiveDays = setOf(0, 1, 2, 3, 4),
            activeDayFilters = setOf(0, 1, 2, 3, 4)
        )
    )

    val uiState: StateFlow<FocusScheduleUiState> = combine(
        focusScheduleDao.getAllSchedulesFlow().catch { emit(emptyList()) },
        _sheetState
    ) { schedules, sheet ->
        val items = if (schedules.isEmpty()) {
            defaultFocusSchedules
        } else {
            schedules.map { it.toItem() }
        }
        FocusScheduleUiState(
            schedules = items,
            activeDayFilters = sheet.activeDayFilters,
            showAddScheduleSheet = sheet.showAddScheduleSheet,
            newScheduleTitle = sheet.newScheduleTitle,
            newStartTime = sheet.newStartTime,
            newEndTime = sheet.newEndTime,
            newActiveDays = sheet.newActiveDays,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusScheduleUiState(isLoading = true)
    )

    fun onEvent(event: FocusScheduleUiEvent) {
        when (event) {
            is FocusScheduleUiEvent.ToggleSchedule -> {
                val current = uiState.value.schedules.find { it.id == event.id }
                val newEnabled = !(current?.isEnabled ?: false)
                viewModelScope.launch(Dispatchers.IO) {
                    focusScheduleDao.updateScheduleEnabled(event.id, newEnabled)
                }
            }
            is FocusScheduleUiEvent.ToggleDayFilter -> {
                _sheetState.update { state ->
                    val updated = state.activeDayFilters.toMutableSet()
                    if (updated.contains(event.dayIndex)) {
                        updated.remove(event.dayIndex)
                    } else {
                        updated.add(event.dayIndex)
                    }
                    state.copy(activeDayFilters = updated)
                }
            }
            FocusScheduleUiEvent.OpenAddScheduleSheet -> {
                _sheetState.update { it.copy(showAddScheduleSheet = true) }
            }
            FocusScheduleUiEvent.DismissAddScheduleSheet -> {
                _sheetState.update { it.copy(showAddScheduleSheet = false) }
            }
            is FocusScheduleUiEvent.UpdateNewTitle -> {
                _sheetState.update { it.copy(newScheduleTitle = event.title) }
            }
            is FocusScheduleUiEvent.UpdateNewStartTime -> {
                _sheetState.update { it.copy(newStartTime = event.time) }
            }
            is FocusScheduleUiEvent.UpdateNewEndTime -> {
                _sheetState.update { it.copy(newEndTime = event.time) }
            }
            is FocusScheduleUiEvent.ToggleNewScheduleDay -> {
                _sheetState.update { state ->
                    val updated = state.newActiveDays.toMutableSet()
                    if (updated.contains(event.dayIndex)) {
                        updated.remove(event.dayIndex)
                    } else {
                        updated.add(event.dayIndex)
                    }
                    state.copy(newActiveDays = updated)
                }
            }
            FocusScheduleUiEvent.SaveNewSchedule -> {
                val sheet = _sheetState.value
                val title = sheet.newScheduleTitle.ifBlank { "Custom Focus Block" }
                val bitmask = sheet.newActiveDays.fold(0) { acc, day -> acc or (1 shl day) }
                val repeatSummary = when {
                    sheet.newActiveDays.size == 7 -> "Daily"
                    sheet.newActiveDays == setOf(0, 1, 2, 3, 4) -> "Mon – Fri"
                    sheet.newActiveDays == setOf(5, 6) -> "Sat – Sun"
                    else -> "Custom Days"
                }

                val newEntity = FocusScheduleEntity(
                    id = "schedule_${System.currentTimeMillis()}",
                    title = title,
                    startTimeMinutes = parseTimeToMinutes(sheet.newStartTime),
                    endTimeMinutes = parseTimeToMinutes(sheet.newEndTime),
                    startTimeDisplay = sheet.newStartTime,
                    endTimeDisplay = sheet.newEndTime,
                    repeatSummary = repeatSummary,
                    activeDaysBitmask = bitmask,
                    isEnabled = true,
                    iconType = "CUSTOM_CLOCK",
                    accentColorHex = 0xFF00E5FFL
                )

                viewModelScope.launch(Dispatchers.IO) {
                    focusScheduleDao.insertSchedule(newEntity)
                    _sheetState.update {
                        it.copy(
                            showAddScheduleSheet = false,
                            newScheduleTitle = "",
                            newStartTime = "08:00 PM",
                            newEndTime = "11:00 PM"
                        )
                    }
                }
            }
            FocusScheduleUiEvent.NavigateBack -> {
                // Handled in caller
            }
        }
    }

    private data class SheetInputState(
        val showAddScheduleSheet: Boolean,
        val newScheduleTitle: String,
        val newStartTime: String,
        val newEndTime: String,
        val newActiveDays: Set<Int>,
        val activeDayFilters: Set<Int>
    )

    private fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.trim().split(" ")
            val timeParts = parts[0].split(":")
            var hour = timeParts[0].toInt()
            val min = timeParts[1].toInt()
            val isPm = parts.getOrNull(1)?.equals("PM", ignoreCase = true) == true
            if (isPm && hour < 12) hour += 12
            if (!isPm && hour == 12) hour = 0
            hour * 60 + min
        } catch (e: Exception) {
            1200 // default 08:00 PM
        }
    }

    private fun FocusScheduleEntity.toItem(): FocusScheduleItem {
        val activeDays = (0..6).filter { day -> (activeDaysBitmask and (1 shl day)) != 0 }.toSet()
        val icon = when (iconType) {
            "MOON_SLEEP" -> FocusIconType.MOON_SLEEP
            "WORK_STUDY" -> FocusIconType.WORK_STUDY
            else -> FocusIconType.CUSTOM_CLOCK
        }
        val accent = Color(accentColorHex)
        return FocusScheduleItem(
            id = id,
            title = title,
            startTime = startTimeDisplay,
            endTime = endTimeDisplay,
            repeatSummary = repeatSummary,
            activeDays = activeDays,
            isEnabled = isEnabled,
            accentColor = accent,
            badgeLabel = if (isEnabled) "Active" else "Paused",
            badgeColor = if (isEnabled) NeonGreen else TextMuted,
            iconType = icon
        )
    }
}
