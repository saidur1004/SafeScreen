package com.safescreen.app.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.ThreatType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Presentation ViewModel for Detailed Activity Log Screen.
 * Fully dynamic: Maps Room DB ThreatLog entities into calendar-grouped timeline items.
 */
@HiltViewModel
class DetailedActivityLogViewModel @Inject constructor(
    private val threatLogDao: ThreatLogDao
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(LogCategory.ALL)
    private val _isFilterMenuVisible = MutableStateFlow(false)
    private val _isClearDialogVisible = MutableStateFlow(false)
    private val _expandedGroups = MutableStateFlow<Map<String, Boolean>>(mapOf("today" to true, "yesterday" to true))

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val uiState: StateFlow<ActivityLogUiState> = combine(
        threatLogDao.getAllThreatLogsFlow(),
        _selectedFilter,
        _isFilterMenuVisible,
        _isClearDialogVisible,
        _expandedGroups
    ) { logs, filter, isMenuVisible, isClearVisible, expandedMap ->
        val groupedList = groupLogsByDay(logs, expandedMap)
        val isEmpty = logs.isEmpty()

        ActivityLogUiState(
            groupedLogs = groupedList,
            selectedFilter = filter,
            isFilterMenuVisible = isMenuVisible,
            isClearConfirmDialogVisible = isClearVisible,
            isEmpty = isEmpty
        )
    }.catch { error ->
        emit(
            ActivityLogUiState(
                groupedLogs = emptyList(),
                isEmpty = true
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActivityLogUiState(groupedLogs = emptyList(), isEmpty = false)
    )

    fun onEvent(event: ActivityLogUiEvent) {
        when (event) {
            is ActivityLogUiEvent.SelectFilter -> {
                _selectedFilter.value = event.filter
                _isFilterMenuVisible.value = false
            }
            ActivityLogUiEvent.ToggleFilterMenu -> {
                _isFilterMenuVisible.value = !_isFilterMenuVisible.value
            }
            is ActivityLogUiEvent.ToggleGroupExpansion -> {
                val current = _expandedGroups.value.toMutableMap()
                val isExp = current[event.dateId] ?: true
                current[event.dateId] = !isExp
                _expandedGroups.value = current
            }
            ActivityLogUiEvent.RequestClearLogs -> {
                _isClearDialogVisible.value = true
            }
            ActivityLogUiEvent.DismissClearDialog -> {
                _isClearDialogVisible.value = false
            }
            ActivityLogUiEvent.ConfirmClearLogs -> {
                _isClearDialogVisible.value = false
                viewModelScope.launch {
                    threatLogDao.clearAllThreatLogs()
                }
            }
            is ActivityLogUiEvent.LogItemClicked -> Unit
            else -> Unit
        }
    }

    private fun groupLogsByDay(
        entities: List<ThreatLogEntity>,
        expandedMap: Map<String, Boolean>
    ): List<DateGroupedLogs> {
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = todayCalendar.timeInMillis
        val yesterdayStart = todayStart - 86400 * 1000L

        val todayLogs = mutableListOf<ActivityLogItem>()
        val yesterdayLogs = mutableListOf<ActivityLogItem>()
        val olderLogs = mutableListOf<ActivityLogItem>()

        entities.forEach { entity ->
            val item = entity.toUiItem()
            when {
                entity.timestamp >= todayStart -> todayLogs.add(item)
                entity.timestamp >= yesterdayStart -> yesterdayLogs.add(item)
                else -> olderLogs.add(item)
            }
        }

        val groups = mutableListOf<DateGroupedLogs>()
        if (todayLogs.isNotEmpty()) {
            groups.add(
                DateGroupedLogs(
                    dateId = "today",
                    dateLabel = "Today • ${dateFormat.format(Date(todayStart))}",
                    isExpanded = expandedMap["today"] ?: true,
                    items = todayLogs
                )
            )
        }
        if (yesterdayLogs.isNotEmpty()) {
            groups.add(
                DateGroupedLogs(
                    dateId = "yesterday",
                    dateLabel = "Yesterday • ${dateFormat.format(Date(yesterdayStart))}",
                    isExpanded = expandedMap["yesterday"] ?: true,
                    items = yesterdayLogs
                )
            )
        }
        if (olderLogs.isNotEmpty()) {
            groups.add(
                DateGroupedLogs(
                    dateId = "older",
                    dateLabel = "Older Activity",
                    isExpanded = expandedMap["older"] ?: false,
                    items = olderLogs
                )
            )
        }

        return groups
    }

    private fun ThreatLogEntity.toUiItem(): ActivityLogItem {
        val cat = when (threatType) {
            ThreatType.VISUAL_NSFW -> LogCategory.VISUAL
            ThreatType.AUDIO_EXPLICIT -> LogCategory.AUDIO
            ThreatType.REELS_LIMIT -> LogCategory.REELS
        }

        val titleText = when (threatType) {
            ThreatType.VISUAL_NSFW -> "Explicit Visual Content Blurred"
            ThreatType.AUDIO_EXPLICIT -> "Explicit Audio / Music Silenced"
            ThreatType.REELS_LIMIT -> "Reels Limit Reached — Intervention Shown"
        }

        return ActivityLogItem(
            id = id.toString(),
            category = cat,
            title = titleText,
            sourceApp = "$appName ($appPackageName)",
            timestamp = timeFormat.format(Date(timestamp)),
            badgeText = details ?: "${(severity * 100).toInt()}% Confidence",
            confidence = severity
        )
    }
}
