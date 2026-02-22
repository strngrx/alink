package com.aana.aegislink.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blacklist")
data class BlacklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val addedDate: Long,
    val source: String
)

@Entity(tableName = "whitelist")
data class WhitelistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val domain: String,
    val addedDate: Long,
    val userAdded: Boolean
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "statistics")
data class StatisticsEntity(
    @PrimaryKey val statName: String,
    val count: Long
)

@Entity(tableName = "vt_cache")
data class VtCacheEntity(
    @PrimaryKey val urlHash: String,
    val scanResult: String,
    val timestamp: Long
)
