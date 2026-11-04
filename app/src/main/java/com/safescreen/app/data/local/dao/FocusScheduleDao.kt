package com.safescreen.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safescreen.app.data.local.entity.FocusScheduleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for recurring Focus Schedules.
 */
@Dao
interface FocusScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FocusScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSchedules(schedules: List<FocusScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: FocusScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: FocusScheduleEntity)

    @Query("DELETE FROM focus_schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: String)

    @Query("SELECT * FROM focus_schedules ORDER BY startTimeMinutes ASC")
    fun getAllSchedulesFlow(): Flow<List<FocusScheduleEntity>>

    @Query("SELECT * FROM focus_schedules WHERE isEnabled = 1 ORDER BY startTimeMinutes ASC")
    fun getEnabledSchedulesFlow(): Flow<List<FocusScheduleEntity>>

    @Query("SELECT * FROM focus_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): FocusScheduleEntity?

    @Query("UPDATE focus_schedules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateScheduleEnabled(id: String, isEnabled: Boolean)
}

