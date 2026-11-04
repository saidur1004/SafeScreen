package com.safescreen.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.ThreatType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local threat logs.
 * Provides reactive Kotlin Coroutines Flows for real-time UI updates.
 */
@Dao
interface ThreatLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatLog(log: ThreatLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatLogs(logs: List<ThreatLogEntity>): List<Long>

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC")
    fun getAllThreatLogsFlow(): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE threatType = :type ORDER BY timestamp DESC")
    fun getThreatLogsByTypeFlow(type: ThreatType): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getThreatLogsSinceFlow(sinceTimestamp: Long): Flow<List<ThreatLogEntity>>

    @Query("SELECT COUNT(*) FROM threat_logs WHERE timestamp >= :startOfDayTimestamp")
    fun getTodayThreatCountFlow(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM threat_logs WHERE threatType = :type AND timestamp >= :startOfDayTimestamp")
    fun getTodayThreatCountByTypeFlow(type: ThreatType, startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT * FROM threat_logs WHERE appName LIKE '%' || :query || '%' OR appPackageName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchThreatLogs(query: String): Flow<List<ThreatLogEntity>>

    @Query("SELECT * FROM threat_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentThreatLogsFlow(limit: Int): Flow<List<ThreatLogEntity>>

    @Query("DELETE FROM threat_logs")
    suspend fun clearAllThreatLogs()

    @Query("DELETE FROM threat_logs WHERE id = :id")
    suspend fun deleteThreatLogById(id: Long)
}
