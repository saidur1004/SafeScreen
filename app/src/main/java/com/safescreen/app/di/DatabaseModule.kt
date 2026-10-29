package com.safescreen.app.di

import android.content.Context
import androidx.room.Room
import com.safescreen.app.data.local.AppDatabase
import com.safescreen.app.data.local.dao.StatDao
import com.safescreen.app.data.repository.StatRepositoryImpl
import com.safescreen.app.domain.repository.StatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        ).fallbackToDestructiveMigration().build()
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

