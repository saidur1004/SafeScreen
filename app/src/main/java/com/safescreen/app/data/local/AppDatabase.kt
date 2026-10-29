package com.safescreen.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safescreen.app.data.local.dao.StatDao
import com.safescreen.app.data.local.entity.StatEntity

@Database(
    entities = [StatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun statDao(): StatDao

    companion object {
        const val DATABASE_NAME = "safescreen_db"
    }
}

