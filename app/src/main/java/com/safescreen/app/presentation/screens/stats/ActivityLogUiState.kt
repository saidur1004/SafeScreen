package com.safescreen.app.presentation.screens.stats

import androidx.compose.runtime.Immutable

/**
 * Category of local protection events logged in SafeScreen.
 */
enum class LogCategory(val displayName: String) {
    ALL("All Events"),
    VISUAL("Visual Blur"),
    AUDIO("Audio Guard"),
    REELS("Reels Limit")
}

/**
 * Data model for an individual protection event item.
 */
@Immutable
data class ActivityLogItem(
    val id: String,
    val category: LogCategory,
    val title: String,
    val sourceApp: String,
    val timestamp: String,
    val badgeText: String? = null,
    val confidence: Float = 0.94f
)

/**
 * Group of activity logs partitioned by calendar day (e.g., Today, Yesterday).
 */
@Immutable
data class DateGroupedLogs(
    val dateId: String,
    val dateLabel: String,
    val isExpanded: Boolean = true,
    val items: List<ActivityLogItem> = emptyList()
)

/**
 * UI State for DetailedActivityLogScreen.
 */
@Immutable
data class ActivityLogUiState(
    val groupedLogs: List<DateGroupedLogs> = defaultSampleLogs(),
    val selectedFilter: LogCategory = LogCategory.ALL,
    val isFilterMenuVisible: Boolean = false,
    val isClearConfirmDialogVisible: Boolean = false,
    val isClearing: Boolean = false,
    val isEmpty: Boolean = false
) {
    /**
     * Filtered list of grouped logs respecting selected category.
     */
    val visibleGroups: List<DateGroupedLogs>
        get() {
            if (isEmpty) return emptyList()
            if (selectedFilter == LogCategory.ALL) return groupedLogs

            return groupedLogs.mapNotNull { group ->
                val filteredItems = group.items.filter { it.category == selectedFilter }
                if (filteredItems.isNotEmpty()) {
                    group.copy(items = filteredItems)
                } else null
            }
        }
}

/**
 * User interactions and events on DetailedActivityLogScreen.
 */
sealed interface ActivityLogUiEvent {
    data object NavigateBack : ActivityLogUiEvent
    data class ToggleGroupExpansion(val dateId: String) : ActivityLogUiEvent
    data class SelectFilter(val filter: LogCategory) : ActivityLogUiEvent
    data object ToggleFilterMenu : ActivityLogUiEvent
    data object RequestClearLogs : ActivityLogUiEvent
    data object ConfirmClearLogs : ActivityLogUiEvent
    data object DismissClearDialog : ActivityLogUiEvent
    data class LogItemClicked(val item: ActivityLogItem) : ActivityLogUiEvent
}

/**
 * Sample chronological log dataset matching visual reference:
 * - Event 1: Explicit Visual Content Blurred (Chrome • 03:42 PM, 94% NSFW score)
 * - Event 2: Background Music Silenced (Shorts Feed • 02:15 PM, 45 sec muted)
 * - Event 3: Reels Limit Reached — Overlay Shown (Instagram • 11:20 AM)
 * - Group 2: Yesterday • Oct 30, 2026
 */
fun defaultSampleLogs(): List<DateGroupedLogs> = listOf(
    DateGroupedLogs(
        dateId = "today",
        dateLabel = "Today • Oct 31, 2026",
        isExpanded = true,
        items = listOf(
            ActivityLogItem(
                id = "item_today_1",
                category = LogCategory.VISUAL,
                title = "Explicit Visual Content Blurred",
                sourceApp = "Browser (com.android.chrome) • 03:42 PM",
                timestamp = "03:42 PM",
                badgeText = "94% NSFW score",
                confidence = 0.94f
            ),
            ActivityLogItem(
                id = "item_today_2",
                category = LogCategory.AUDIO,
                title = "Background Music Silenced",
                sourceApp = "Shorts Feed • 02:15 PM",
                timestamp = "02:15 PM",
                badgeText = "45 sec muted",
                confidence = 0.91f
            ),
            ActivityLogItem(
                id = "item_today_3",
                category = LogCategory.REELS,
                title = "Reels Limit Reached — Overlay Shown",
                sourceApp = "Instagram • 11:20 AM",
                timestamp = "11:20 AM",
                badgeText = null,
                confidence = 1.0f
            )
        )
    ),
    DateGroupedLogs(
        dateId = "yesterday",
        dateLabel = "Yesterday • Oct 30, 2026",
        isExpanded = false,
        items = listOf(
            ActivityLogItem(
                id = "item_yesterday_1",
                category = LogCategory.VISUAL,
                title = "NSFW Image Intercepted",
                sourceApp = "Twitter / X • 08:14 PM",
                timestamp = "08:14 PM",
                badgeText = "88% NSFW score",
                confidence = 0.88f
            ),
            ActivityLogItem(
                id = "item_yesterday_2",
                category = LogCategory.AUDIO,
                title = "Suggestive Audio Muted",
                sourceApp = "TikTok • 05:40 PM",
                timestamp = "05:40 PM",
                badgeText = "12 sec muted",
                confidence = 0.85f
            )
        )
    )
)
