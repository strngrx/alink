package com.aana.aegislink.utils

import android.content.Context
import com.aana.aegislink.db.BlacklistDao
import com.aana.aegislink.db.BlacklistEntity
import com.aana.aegislink.db.SettingsDao
import com.aana.aegislink.db.SettingsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeedLoader(
    private val context: Context,
    private val settingsDao: SettingsDao,
    private val blacklistDao: BlacklistDao
) {
    suspend fun seedBlacklistIfNeeded(assetName: String = "blacklist_seed.txt") = withContext(Dispatchers.IO) {
        val seeded = settingsDao.getValue(SettingsKeys.SEED_BLACKLIST_DONE)
            ?.toBooleanStrictOrNull()
            ?: false
        if (seeded) return@withContext

        val lines = runCatching {
            context.assets.open(assetName).bufferedReader().readLines()
        }.getOrDefault(emptyList())

        for (line in lines) {
            val domain = line.trim().lowercase()
            if (domain.isBlank()) continue
            blacklistDao.insert(
                BlacklistEntity(
                    domain = domain,
                    addedDate = System.currentTimeMillis(),
                    source = "seed"
                )
            )
        }

        settingsDao.upsert(SettingsEntity(SettingsKeys.SEED_BLACKLIST_DONE, "true"))
    }
}
