package com.pandora.core.cac.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_entries")
data class MemoryEntryEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val source: String,
    val content: String,
    // Embedding và causalLinks sẽ được lưu dưới dạng JSON String hoặc trong bảng riêng ở các giai đoạn sau.
    // Tạm thời để trống để đơn giản hóa.
    val embeddingJson: String? = null,
    val causalLinksJson: String? = null
)
