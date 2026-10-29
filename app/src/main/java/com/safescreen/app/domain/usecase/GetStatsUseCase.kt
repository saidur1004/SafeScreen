package com.safescreen.app.domain.usecase

import com.safescreen.app.domain.model.ProtectionStat
import com.safescreen.app.domain.repository.StatRepository
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class GetStatsUseCase @Inject constructor(
    private val repository: StatRepository
) {
    fun getAll(): Flow<List<ProtectionStat>> = repository.getAllStats()

    fun getToday(): Flow<List<ProtectionStat>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return repository.getTodayStats(calendar.timeInMillis)
    }
}

