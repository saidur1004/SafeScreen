package com.safescreen.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safescreen.app.domain.model.ProtectionStat
import com.safescreen.app.domain.model.ProtectionType

@Entity(tableName = "protection_stats")
data class StatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val typeName: String,
    val count: Int,
    val appPackage: String?,
    val details: String?
) {
    fun toDomain(): ProtectionStat {
        val type = try {
            ProtectionType.valueOf(typeName)
        } catch (e: Exception) {
            ProtectionType.NSFW_IMAGE
        }
        return ProtectionStat(
            id = id,
            timestamp = timestamp,
            type = type,
            count = count,
            appPackage = appPackage,
            details = details
        )
    }

    companion object {
        fun fromDomain(stat: ProtectionStat): StatEntity {
            return StatEntity(
                id = stat.id,
                timestamp = stat.timestamp,
                typeName = stat.type.name,
                count = stat.count,
                appPackage = stat.appPackage,
                details = stat.details
            )
        }
    }
}

