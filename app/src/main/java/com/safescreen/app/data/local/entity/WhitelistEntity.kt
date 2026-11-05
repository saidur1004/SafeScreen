package com.safescreen.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing an installed launcher app and its user-selected exemption status.
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
    val category: String = "OTHER",
    val isExempted: Boolean = true,
    val isBankingDefault: Boolean = false,
    val isFlagSecure: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val accentColorHex: Long = 0xFF00F0FFL
)

