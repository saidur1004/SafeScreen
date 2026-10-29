package com.safescreen.app.data.repository

import com.safescreen.app.data.local.dao.StatDao
import com.safescreen.app.data.local.entity.StatEntity
import com.safescreen.app.domain.model.ProtectionStat
import com.safescreen.app.domain.repository.StatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatRepositoryImpl @Inject constructor(
    private val statDao: StatDao
) : StatRepository {

    override fun getAllStats(): Flow<List<ProtectionStat>> {
        return statDao.getAllStats().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTodayStats(startOfDayTimestamp: Long): Flow<List<ProtectionStat>> {
        return statDao.getStatsSince(startOfDayTimestamp).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertStat(stat: ProtectionStat): Long {
        return statDao.insertStat(StatEntity.fromDomain(stat))
    }

    override suspend fun clearStats() {
        statDao.clearAll()
    }
}

