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
    val groupedLogs: List<DateGroupedLogs> = emptyList(),
    val selectedFilter: LogCategory = LogCategory.ALL,
    val isFilterMenuVisible: Boolean = false,
    val isClearConfirmDialogVisible: Boolean = false,
    val isClearing: Boolean = false,
    val isEmpty: Boolean = true
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
 * Initial empty chronological log dataset for clean production state.
 */
fun defaultSampleLogs(): List<DateGroupedLogs> = emptyList()

