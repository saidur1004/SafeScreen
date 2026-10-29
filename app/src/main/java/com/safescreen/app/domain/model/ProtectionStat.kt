package com.safescreen.app.domain.model

data class ProtectionStat(
    val id: Long = 0,
    val timestamp: Long,
    val type: ProtectionType,
    val count: Int = 1,
    val appPackage: String? = null,
    val details: String? = null
)

enum class ProtectionType {
    NSFW_IMAGE,
    NSFW_VIDEO,
    MUSIC_MUTED,
    REELS_LIMITED,
    APP_BLOCKED
}

