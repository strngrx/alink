package com.aana.aegislink.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        BlacklistEntity::class,
        WhitelistEntity::class,
        SettingsEntity::class,
        StatisticsEntity::class,
        VtCacheEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun blacklistDao(): BlacklistDao
    abstract fun whitelistDao(): WhitelistDao
    abstract fun settingsDao(): SettingsDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun vtCacheDao(): VtCacheDao
}
