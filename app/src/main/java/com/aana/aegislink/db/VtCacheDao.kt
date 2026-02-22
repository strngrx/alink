package com.aana.aegislink.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface VtCacheDao {
    @Query("SELECT scanResult FROM vt_cache WHERE urlHash = :hash LIMIT 1")
    suspend fun getResult(hash: String): String?

    @Query("SELECT timestamp FROM vt_cache WHERE urlHash = :hash LIMIT 1")
    suspend fun getTimestamp(hash: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VtCacheEntity)

    @Query("DELETE FROM vt_cache WHERE timestamp < :minTimestamp")
    suspend fun prune(minTimestamp: Long): Int
}
