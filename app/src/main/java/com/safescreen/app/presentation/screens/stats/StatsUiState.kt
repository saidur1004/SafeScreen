package com.safescreen.app.presentation.screens.stats

import androidx.compose.ui.graphics.Color

/**
 * Filter time range selection for StatsScreen.
 */
enum class StatsTimeRange(val label: String) {
    TODAY("Today"),
    SEVEN_DAYS("7 Days"),
    THIRTY_DAYS("30 Days")
}

/**
 * Data model for daily activity dual-bar chart.
 */
data class DayActivityItem(
    val day: String,
    val blockedFrames: Int,
    val mutedAudio: Int,
    val isSelected: Boolean = false
)

/**
 * Data model for category breakdown metrics.
 */
data class CategoryBreakdownData(
    val visualPercent: Int = 0,
    val audioPercent: Int = 0,
    val reelsPercent: Int = 0,
    val visualLabel: String = "Explicit Visuals Blurred",
    val audioLabel: String = "Background Music Muted",
    val reelsLabel: String = "Reels Sessions Intercepted"
)

/**
 * App icon type indicator for top intercepted apps list.
 */
enum class AppIconType {
    CHROME,
    INSTAGRAM,
    TWITTER_X
}

/**
 * Data model for top intercepted app ranking row.
 */
data class InterceptedAppItem(
    val name: String,
    val packageName: String,
    val eventCount: Int,
    val percentage: Float, // 0.0f to 1.0f relative fill
    val barColor: Color,
    val iconType: AppIconType
)

/**
 * Overall UiState for Protection Stats Screen.
 * Defaults strictly to genuine 0 counts and empty collections for empty database states.
 */
data class StatsUiState(
    val selectedTimeRange: StatsTimeRange = StatsTimeRange.SEVEN_DAYS,
    val weeklyActivities: List<DayActivityItem> = emptyList(),
    val selectedDayIndex: Int = 6, // Current day (last day of 7-day window) selected by default
    val categoryBreakdown: CategoryBreakdownData = CategoryBreakdownData(),
    val topApps: List<InterceptedAppItem> = emptyList(),
    val totalBlockedToday: Int = 0,
    val totalMutedToday: Int = 0,
    val isPrivacyShieldVerified: Boolean = true
) {
    val selectedDay: DayActivityItem?
        get() = weeklyActivities.getOrNull(selectedDayIndex)
}

/**
 * UI Events emitted from StatsScreen.
 */
sealed interface StatsUiEvent {
    data class SelectTimeRange(val range: StatsTimeRange) : StatsUiEvent
    data class SelectDay(val index: Int) : StatsUiEvent
    data object ViewDetailedActivityLog : StatsUiEvent
    data object SeeAllApps : StatsUiEvent
    data object NavigateBack : StatsUiEvent
}
