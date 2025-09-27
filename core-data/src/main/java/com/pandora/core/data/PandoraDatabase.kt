package com.pandora.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pandora.core.cac.db.MemoryDao
import com.pandora.core.cac.db.MemoryEntryEntity

@Database(entities = [MemoryEntryEntity::class], version = 1, exportSchema = false)
abstract class PandoraDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
