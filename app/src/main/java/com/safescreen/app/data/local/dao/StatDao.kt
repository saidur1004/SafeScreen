package com.safescreen.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.safescreen.app.data.local.entity.StatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatDao {
    @Query("SELECT * FROM protection_stats ORDER BY timestamp DESC")
    fun getAllStats(): Flow<List<StatEntity>>

    @Query("SELECT * FROM protection_stats WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getStatsSince(sinceTimestamp: Long): Flow<List<StatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStat(stat: StatEntity): Long

    @Query("DELETE FROM protection_stats")
    suspend fun clearAll()
}

