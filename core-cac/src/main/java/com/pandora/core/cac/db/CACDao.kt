package com.pandora.core.cac.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CACDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntry)

    @Query("SELECT * FROM memories WHERE source = :source ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getMemoriesBySource(source: String, limit: Int = 100): List<MemoryEntry>

    @Query("SELECT * FROM memories WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getMemoriesSince(since: Long): List<MemoryEntry>
}
