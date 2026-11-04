package com.safescreen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a recurring Focus block schedule.
 */
@Entity(tableName = "focus_schedules")
data class FocusScheduleEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val startTimeDisplay: String,
    val endTimeDisplay: String,
    val repeatSummary: String,
    val activeDaysBitmask: Int = 127, // 7-bit bitmask for Mon-Sun (1111111 = 127)
    val isEnabled: Boolean = true,
    val iconType: String = "CUSTOM_CLOCK",
    val accentColorHex: Long = 0xFF00E5FFL
)

