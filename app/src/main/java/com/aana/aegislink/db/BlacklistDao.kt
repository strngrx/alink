package com.aana.aegislink.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlacklistDao {
    @Query("SELECT EXISTS(SELECT 1 FROM blacklist WHERE domain = :domain LIMIT 1)")
    suspend fun containsDomain(domain: String): Boolean

    @Query("SELECT domain FROM blacklist ORDER BY domain ASC")
    suspend fun allDomains(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BlacklistEntity): Long

    @Query("DELETE FROM blacklist WHERE domain = :domain")
    suspend fun delete(domain: String): Int
}
