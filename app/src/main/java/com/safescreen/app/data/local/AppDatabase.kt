package com.safescreen.app.data.local

import androidx.room.Database
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
    version = 1,
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
         * Automatically seeds the 5 default exempted banking apps and core focus schedules.
         */
        val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = System.currentTimeMillis()

                // 1. Pre-populate 5 Default Banking Apps (FLAG_SECURE & Privacy protected)
                db.execSQL(
                    """
                    INSERT INTO app_whitelist (packageName, appName, category, isExempted, isBankingDefault, isFlagSecure, addedTimestamp, accentColorHex)
                    VALUES 
                    ('com.paypal.android.p2pmobile', 'PayPal', 'BANKING', 1, 1, 1, $now, 4278250239),
                    ('com.infonow.bofa', 'Bank of America', 'BANKING', 1, 1, 1, $now, 4278250239),
                    ('com.google.android.apps.walletnfcrel', 'Google Pay / Wallet', 'BANKING', 1, 1, 1, $now, 4278250239),
                    ('com.chase.sig.android', 'Chase Mobile', 'BANKING', 1, 1, 1, $now, 4278250239),
                    ('com.revolut.revolut', 'Revolut', 'BANKING', 1, 1, 1, $now, 4278250239);
                    """.trimIndent()
                )

                // 2. Pre-populate Default Focus Schedules
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
    }
}
