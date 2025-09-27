package com.pandora.core.cac.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [MemoryEntry::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CACDatabase : RoomDatabase() {
    abstract fun dao(): CACDao
}
