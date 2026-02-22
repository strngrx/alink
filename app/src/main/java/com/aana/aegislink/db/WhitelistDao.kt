package com.aana.aegislink.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WhitelistDao {
    @Query("SELECT EXISTS(SELECT 1 FROM whitelist WHERE domain = :domain LIMIT 1)")
    suspend fun containsDomain(domain: String): Boolean

    @Query("SELECT domain FROM whitelist ORDER BY domain ASC")
    suspend fun allDomains(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: WhitelistEntity): Long

    @Query("DELETE FROM whitelist WHERE domain = :domain")
    suspend fun delete(domain: String): Int
}
