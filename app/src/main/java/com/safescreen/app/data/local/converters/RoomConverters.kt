package com.safescreen.app.data.local.converters

import androidx.room.TypeConverter
import com.safescreen.app.data.local.entity.ThreatAction
import com.safescreen.app.data.local.entity.ThreatType

/**
 * Room TypeConverters for persisting enums as readable text strings.
 */
class RoomConverters {

    @TypeConverter
    fun fromThreatType(type: ThreatType?): String? = type?.name

    @TypeConverter
    fun toThreatType(value: String?): ThreatType? = value?.let {
        runCatching { ThreatType.valueOf(it) }.getOrDefault(ThreatType.VISUAL_NSFW)
    }

    @TypeConverter
    fun fromThreatAction(action: ThreatAction?): String? = action?.name

    @TypeConverter
    fun toThreatAction(value: String?): ThreatAction? = value?.let {
        runCatching { ThreatAction.valueOf(it) }.getOrDefault(ThreatAction.BLURRED)
    }
}

