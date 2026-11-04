package com.safescreen.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safescreen.app.data.local.entity.WhitelistEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for App Whitelist management.
 * Provides high-speed lookups for background services and reactive flows for UI.
 */
@Dao
interface WhitelistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: WhitelistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAppsIgnore(apps: List<WhitelistEntity>)

    @Update
    suspend fun updateApp(app: WhitelistEntity)

    @Query("SELECT * FROM app_whitelist ORDER BY isBankingDefault DESC, appName ASC")
    fun getAllAppsFlow(): Flow<List<WhitelistEntity>>

    @Query("SELECT * FROM app_whitelist WHERE isExempted = 1 ORDER BY isBankingDefault DESC, appName ASC")
    fun getExemptedAppsFlow(): Flow<List<WhitelistEntity>>

    @Query("SELECT packageName FROM app_whitelist WHERE isExempted = 1")
    fun getExemptedPackageNamesFlow(): Flow<List<String>>

    @Query("SELECT packageName FROM app_whitelist WHERE isExempted = 1")
    suspend fun getExemptedPackageNames(): List<String>

    @Query("SELECT * FROM app_whitelist WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppByPackageName(packageName: String): WhitelistEntity?

    @Query("SELECT isExempted FROM app_whitelist WHERE packageName = :packageName LIMIT 1")
    suspend fun isPackageExempted(packageName: String): Boolean?

    @Query("UPDATE app_whitelist SET isExempted = :isExempted WHERE packageName = :packageName")
    suspend fun updateExemptionStatus(packageName: String, isExempted: Boolean)

    @Query("SELECT * FROM app_whitelist WHERE appName LIKE '%' || :query || '%' OR packageName LIKE '%' || :query || '%' ORDER BY appName ASC")
    fun searchAppsFlow(query: String): Flow<List<WhitelistEntity>>

    @Delete
    suspend fun deleteApp(app: WhitelistEntity)

    @Query("SELECT COUNT(*) FROM app_whitelist WHERE isExempted = 1")
    fun getExemptedCountFlow(): Flow<Int>
}

