package com.pandora.core.ai.hybrid

import com.pandora.core.ai.storage.ModelMetadata
import java.nio.ByteBuffer

/**
 * Represents an active model session
 */
data class ModelSession(
    val sessionId: String,
    val modelId: String,
    val modelBuffer: ByteBuffer,
    val metadata: ModelMetadata,
    val source: String,
    val loadTime: Long,
    val accessCount: Int = 0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis(),
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val priority: ModelPriority = ModelPriority.NORMAL,
    val status: LoadingStatus = LoadingStatus.COMPLETED
)
