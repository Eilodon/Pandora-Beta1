package com.pandora.core.cac.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

@Entity(tableName = "memories")
data class MemoryEntry(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val source: String, // e.g., "keyboard", "app_context"
    val content: String,
    // val embedding: ByteArray, // Sẽ thêm ở Mệnh lệnh sau
    val causalLinks: List<String> = emptyList() // List of MemoryEntry IDs
)

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toStringList(string: String): List<String> =
        if (string.isEmpty()) emptyList() else string.split(",")
}
