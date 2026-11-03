package com.safescreen.app.presentation.screens.focus

import androidx.compose.ui.graphics.Color
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple

/**
 * Visual graphic category for focus schedule cards.
 */
enum class FocusIconType {
    MOON_SLEEP,
    WORK_STUDY,
    CUSTOM_CLOCK
}

/**
 * Data model for individual recurring focus schedule item.
 */
data class FocusScheduleItem(
    val id: String,
    val title: String,
    val startTime: String,
    val endTime: String,
    val repeatSummary: String,
    val activeDays: Set<Int>, // 0=Mon, 1=Tue, 2=Wed, 3=Thu, 4=Fri, 5=Sat, 6=Sun
    val isEnabled: Boolean,
    val accentColor: Color,
    val badgeLabel: String = "Active",
    val badgeColor: Color = NeonGreen,
    val iconType: FocusIconType = FocusIconType.CUSTOM_CLOCK
)

/**
 * Default preset focus blocks matching 'Screen Workflow/04_Reels_And_Focus/':
 * - Night Purity & Sleep (10:00 PM – 06:00 AM, Daily, NeonCyan theme)
 * - Deep Work / Study (09:00 AM – 05:00 PM, Mon – Fri, NeonPurple theme)
 */
val defaultFocusSchedules = listOf(
    FocusScheduleItem(
        id = "night_sleep",
        title = "Night Purity & Sleep",
        startTime = "10:00 PM",
        endTime = "06:00 AM",
        repeatSummary = "Daily",
        activeDays = setOf(0, 1, 2, 3, 4, 5, 6),
        isEnabled = true,
        accentColor = NeonCyan,
        badgeLabel = "Active",
        badgeColor = NeonGreen,
        iconType = FocusIconType.MOON_SLEEP
    ),
    FocusScheduleItem(
        id = "deep_work",
        title = "Deep Work / Study",
        startTime = "09:00 AM",
        endTime = "05:00 PM",
        repeatSummary = "Mon – Fri",
        activeDays = setOf(0, 1, 2, 3, 4),
        isEnabled = true,
        accentColor = NeonPurple,
        badgeLabel = "Active",
        badgeColor = NeonPurple,
        iconType = FocusIconType.WORK_STUDY
    )
)

/**
 * Overall UiState for FocusScheduleScreen.
 */
data class FocusScheduleUiState(
    val schedules: List<FocusScheduleItem> = defaultFocusSchedules,
    val activeDayFilters: Set<Int> = setOf(0, 1, 2, 3, 4), // Mon-Fri active by default
    val showAddScheduleSheet: Boolean = false,
    val newScheduleTitle: String = "",
    val newStartTime: String = "08:00 PM",
    val newEndTime: String = "11:00 PM",
    val newActiveDays: Set<Int> = setOf(0, 1, 2, 3, 4),
    val isLoading: Boolean = false
)

/**
 * UI Events emitted from FocusScheduleScreen.
 */
sealed interface FocusScheduleUiEvent {
    data class ToggleSchedule(val id: String) : FocusScheduleUiEvent
    data class ToggleDayFilter(val dayIndex: Int) : FocusScheduleUiEvent
    data object OpenAddScheduleSheet : FocusScheduleUiEvent
    data object DismissAddScheduleSheet : FocusScheduleUiEvent
    data class UpdateNewTitle(val title: String) : FocusScheduleUiEvent
    data class UpdateNewStartTime(val time: String) : FocusScheduleUiEvent
    data class UpdateNewEndTime(val time: String) : FocusScheduleUiEvent
    data class ToggleNewScheduleDay(val dayIndex: Int) : FocusScheduleUiEvent
    data object SaveNewSchedule : FocusScheduleUiEvent
    data object NavigateBack : FocusScheduleUiEvent
}

