package com.pandora.core.cac.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memoryEntry: MemoryEntryEntity)

    @Query("SELECT * FROM memory_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<MemoryEntryEntity>>
    
    @Query("DELETE FROM memory_entries WHERE timestamp < :timestamp")
    suspend fun deleteEntriesOlderThan(timestamp: Long)
}
