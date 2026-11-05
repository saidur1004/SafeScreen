package com.safescreen.app.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.ThreatType
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Presentation ViewModel for Protection Stats Analytics Dashboard.
 * Computes dynamic trends, category distribution, and top intercepted apps directly from Room DB.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val threatLogDao: ThreatLogDao
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(StatsTimeRange.SEVEN_DAYS)
    private val _selectedDayIndex = MutableStateFlow(6)

    private val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())

    val uiState: StateFlow<StatsUiState> = combine(
        threatLogDao.getAllThreatLogsFlow(),
        _selectedTimeRange,
        _selectedDayIndex
    ) { allLogs, range, dayIndex ->
        val now = System.currentTimeMillis()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Filter logs according to selected time range
        val filteredLogs = when (range) {
            StatsTimeRange.TODAY -> allLogs.filter { it.timestamp >= startOfToday }
            StatsTimeRange.SEVEN_DAYS -> allLogs.filter { it.timestamp >= (now - 7 * 86400 * 1000L) }
            StatsTimeRange.THIRTY_DAYS -> allLogs.filter { it.timestamp >= (now - 30 * 86400 * 1000L) }
        }

        val totalBlockedToday = allLogs.count { it.timestamp >= startOfToday && it.threatType == ThreatType.VISUAL_NSFW }
        val totalMutedToday = allLogs.count { it.timestamp >= startOfToday && it.threatType == ThreatType.AUDIO_EXPLICIT }

        // Category breakdown computation
        val visualCount = filteredLogs.count { it.threatType == ThreatType.VISUAL_NSFW }
        val audioCount = filteredLogs.count { it.threatType == ThreatType.AUDIO_EXPLICIT }
        val reelsCount = filteredLogs.count { it.threatType == ThreatType.REELS_LIMIT }
        val totalThreats = (visualCount + audioCount + reelsCount)

        val breakdown = if (totalThreats == 0) {
            CategoryBreakdownData(visualPercent = 0, audioPercent = 0, reelsPercent = 0)
        } else {
            CategoryBreakdownData(
                visualPercent = (visualCount * 100) / totalThreats,
                audioPercent = (audioCount * 100) / totalThreats,
                reelsPercent = (reelsCount * 100) / totalThreats
            )
        }

        // Top intercepted apps computation
        val topApps = computeTopApps(filteredLogs)

        // Weekly activity dual-bars computation
        val weeklyActivities = computeWeeklyActivities(allLogs, dayIndex)

        StatsUiState(
            selectedTimeRange = range,
            weeklyActivities = weeklyActivities,
            selectedDayIndex = dayIndex,
            categoryBreakdown = breakdown,
            topApps = topApps,
            totalBlockedToday = totalBlockedToday,
            totalMutedToday = totalMutedToday,
            isPrivacyShieldVerified = true
        )
    }.catch {
        emit(StatsUiState(weeklyActivities = computeWeeklyActivities(emptyList(), 6)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState(weeklyActivities = computeWeeklyActivities(emptyList(), 6))
    )

    fun onEvent(event: StatsUiEvent) {
        when (event) {
            is StatsUiEvent.SelectTimeRange -> {
                _selectedTimeRange.value = event.range
            }
            is StatsUiEvent.SelectDay -> {
                _selectedDayIndex.value = event.index
            }
            else -> Unit
        }
    }

    private fun computeTopApps(logs: List<ThreatLogEntity>): List<InterceptedAppItem> {
        if (logs.isEmpty()) return emptyList()

        val appGroups = logs.groupBy { it.appPackageName }
        val maxCount = appGroups.values.maxOfOrNull { it.size } ?: 1

        val list = appGroups.entries
            .sortedByDescending { it.value.size }
            .take(3)
            .map { (pkg, groupLogs) ->
                val appName = groupLogs.firstOrNull()?.appName ?: pkg
                val count = groupLogs.size
                val ratio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.1f, 1.0f)

                val (color, icon) = when {
                    pkg.contains("chrome", ignoreCase = true) -> NeonPink to AppIconType.CHROME
                    pkg.contains("instagram", ignoreCase = true) -> NeonCyan to AppIconType.INSTAGRAM
                    else -> NeonPurple to AppIconType.TWITTER_X
                }

                InterceptedAppItem(
                    name = appName,
                    packageName = pkg,
                    eventCount = count,
                    percentage = ratio,
                    barColor = color,
                    iconType = icon
                )
            }

        return list
    }

    private fun computeWeeklyActivities(allLogs: List<ThreatLogEntity>, selectedIndex: Int): List<DayActivityItem> {
        val days = mutableListOf<DayActivityItem>()

        // 7 days ending today
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val start = dayCal.timeInMillis
            val end = start + 86400 * 1000L

            val dayLogs = allLogs.filter { it.timestamp in start until end }
            val blocked = dayLogs.count { it.threatType == ThreatType.VISUAL_NSFW }
            val muted = dayLogs.count { it.threatType == ThreatType.AUDIO_EXPLICIT }
            val dayName = dayNameFormat.format(Date(start))

            days.add(
                DayActivityItem(
                    day = dayName,
                    blockedFrames = blocked,
                    mutedAudio = muted,
                    isSelected = (days.size == selectedIndex)
                )
            )
        }
        return days
    }
}
