package com.safescreen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Type of security/wellbeing threat intercepted by SafeScreen.
 */
enum class ThreatType {
    VISUAL_NSFW,
    AUDIO_EXPLICIT,
    REELS_LIMIT
}

/**
 * Concrete defensive action taken by SafeScreen guardian.
 */
enum class ThreatAction {
    BLURRED,
    MUTED,
    BLOCKED
}

/**
 * Room Database entity representing an individual threat event.
 * Stored 100% locally on-device in SQLite; zero cloud transmission.
 */
@Entity(
    tableName = "threat_logs",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["threatType"]),
        Index(value = ["appPackageName"])
    ]
)
data class ThreatLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val threatType: ThreatType,
    val appPackageName: String,
    val appName: String,
    val severity: Float = 0.90f,
    val actionTaken: ThreatAction,
    val details: String? = null
)

