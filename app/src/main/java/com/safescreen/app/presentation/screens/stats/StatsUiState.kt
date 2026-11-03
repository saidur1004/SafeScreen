package com.safescreen.app.presentation.screens.stats

import androidx.compose.ui.graphics.Color
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple

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
    val visualPercent: Int = 55,
    val audioPercent: Int = 30,
    val reelsPercent: Int = 15,
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
 */
data class StatsUiState(
    val selectedTimeRange: StatsTimeRange = StatsTimeRange.SEVEN_DAYS,
    val weeklyActivities: List<DayActivityItem> = defaultWeeklyActivities,
    val selectedDayIndex: Int = 3, // Thursday selected by default
    val categoryBreakdown: CategoryBreakdownData = CategoryBreakdownData(),
    val topApps: List<InterceptedAppItem> = defaultTopApps,
    val totalBlockedToday: Int = 38,
    val totalMutedToday: Int = 14,
    val isPrivacyShieldVerified: Boolean = true
) {
    val selectedDay: DayActivityItem?
        get() = weeklyActivities.getOrNull(selectedDayIndex)
}

/**
 * Default sample data exactly matching 'Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png' (Left Screen).
 */
val defaultWeeklyActivities = listOf(
    DayActivityItem(day = "Mon", blockedFrames = 22, mutedAudio = 15),
    DayActivityItem(day = "Tue", blockedFrames = 15, mutedAudio = 9),
    DayActivityItem(day = "Wed", blockedFrames = 21, mutedAudio = 14),
    DayActivityItem(day = "Thu", blockedFrames = 42, mutedAudio = 18, isSelected = true),
    DayActivityItem(day = "Fri", blockedFrames = 19, mutedAudio = 13),
    DayActivityItem(day = "Sun", blockedFrames = 17, mutedAudio = 13)
)

val defaultTopApps = listOf(
    InterceptedAppItem(
        name = "Chrome",
        packageName = "com.android.chrome",
        eventCount = 54,
        percentage = 0.85f,
        barColor = NeonPink,
        iconType = AppIconType.CHROME
    ),
    InterceptedAppItem(
        name = "Instagram",
        packageName = "com.instagram.android",
        eventCount = 32,
        percentage = 0.50f,
        barColor = NeonCyan,
        iconType = AppIconType.INSTAGRAM
    ),
    InterceptedAppItem(
        name = "Twitter / X",
        packageName = "com.twitter.android",
        eventCount = 19,
        percentage = 0.30f,
        barColor = NeonPurple,
        iconType = AppIconType.TWITTER_X
    )
)

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
