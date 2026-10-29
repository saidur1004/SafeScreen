package com.safescreen.app.domain.repository

import com.safescreen.app.domain.model.ProtectionStat
import kotlinx.coroutines.flow.Flow

interface StatRepository {
    fun getAllStats(): Flow<List<ProtectionStat>>
    fun getTodayStats(startOfDayTimestamp: Long): Flow<List<ProtectionStat>>
    suspend fun insertStat(stat: ProtectionStat): Long
    suspend fun clearStats()
}

