package com.safescreen.app.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safescreen.app.data.local.converters.RoomConverters
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.dao.StatDao
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.entity.FocusScheduleEntity
import com.safescreen.app.data.local.entity.StatEntity
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.WhitelistEntity

/**
 * Main Room SQLite Database for SafeScreen.
 * 100% on-device single source of truth (SSOT).
 */
@Database(
    entities = [
        ThreatLogEntity::class,
        WhitelistEntity::class,
        FocusScheduleEntity::class,
        StatEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun threatLogDao(): ThreatLogDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun focusScheduleDao(): FocusScheduleDao
    abstract fun statDao(): StatDao

    companion object {
        const val DATABASE_NAME = "safescreen_db"

        /**
         * Room lifecycle callback for initial database pre-population.
         * Seeds only starter focus schedules. Installed apps sync when the whitelist is opened.
         */
        val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Pre-populate Default Focus Schedules
                db.execSQL(
                    """
                    INSERT INTO focus_schedules (id, title, startTimeMinutes, endTimeMinutes, startTimeDisplay, endTimeDisplay, repeatSummary, activeDaysBitmask, isEnabled, iconType, accentColorHex)
                    VALUES 
                    ('night_sleep', 'Night Purity & Sleep', 1320, 360, '10:00 PM', '06:00 AM', 'Daily', 127, 1, 'MOON_SLEEP', 4278248959),
                    ('deep_work', 'Deep Work / Study', 540, 1020, '09:00 AM', '05:00 PM', 'Mon – Fri', 31, 1, 'WORK_STUDY', 4288507613);
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove legacy hardcoded banking defaults; only installed apps are synced now.
                db.execSQL("DELETE FROM app_whitelist WHERE isBankingDefault = 1")
            }
        }
    }
}
