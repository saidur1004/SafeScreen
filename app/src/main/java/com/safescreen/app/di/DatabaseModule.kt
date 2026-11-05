package com.safescreen.app.di

import android.content.Context
import androidx.room.Room
import com.safescreen.app.data.local.AppDatabase
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.dao.StatDao
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.repository.StatRepositoryImpl
import com.safescreen.app.domain.repository.StatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger-Hilt Module for providing thread-safe Room Database instances and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addCallback(AppDatabase.PREPOPULATE_CALLBACK)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideThreatLogDao(database: AppDatabase): ThreatLogDao {
        return database.threatLogDao()
    }

    @Provides
    @Singleton
    fun provideWhitelistDao(database: AppDatabase): WhitelistDao {
        return database.whitelistDao()
    }

    @Provides
    @Singleton
    fun provideFocusScheduleDao(database: AppDatabase): FocusScheduleDao {
        return database.focusScheduleDao()
    }

    @Provides
    @Singleton
    fun provideStatDao(database: AppDatabase): StatDao {
        return database.statDao()
    }

    @Provides
    @Singleton
    fun provideStatRepository(statDao: StatDao): StatRepository {
        return StatRepositoryImpl(statDao)
    }
}
