package com.safescreen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing an installed app and its whitelist exemption status.
 * Banking apps are exempted by default due to Android FLAG_SECURE and user financial privacy.
 */
@Entity(
    tableName = "app_whitelist",
    indices = [
        Index(value = ["isExempted"]),
        Index(value = ["category"])
    ]
)
data class WhitelistEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val category: String = "BANKING",
    val isExempted: Boolean = true,
    val isBankingDefault: Boolean = false,
    val isFlagSecure: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val accentColorHex: Long = 0xFF00F0FFL
)
