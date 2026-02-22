package com.aana.aegislink.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface StatisticsDao {
    @Query("SELECT count FROM statistics WHERE statName = :name LIMIT 1")
    suspend fun getCount(name: String): Long?

    @Query("UPDATE statistics SET count = count + :delta WHERE statName = :name")
    suspend fun increment(name: String, delta: Long = 1): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StatisticsEntity)
}
