package com.aana.aegislink.utils

import com.aana.aegislink.db.StatisticsDao
import com.aana.aegislink.db.StatisticsEntity

class StatsRecorder(
    private val dao: StatisticsDao
) {
    suspend fun recordCleaned() {
        incrementOrInsert(StatsKeys.TOTAL_CLEANED)
    }

    suspend fun recordBlocked() {
        incrementOrInsert(StatsKeys.TOTAL_BLOCKED)
    }

    private suspend fun incrementOrInsert(name: String) {
        val updated = dao.increment(name, 1)
        if (updated == 0) {
            dao.upsert(StatisticsEntity(statName = name, count = 1))
        }
    }
}
