package com.pandora.core.ai.hybrid

import com.pandora.core.ai.storage.ModelMetadata
import java.nio.ByteBuffer

/**
 * Result of model loading operation
 */
data class ModelLoadResult(
    val success: Boolean,
    val modelId: String? = null,
    val modelBuffer: ByteBuffer? = null,
    val metadata: ModelMetadata? = null,
    val source: LoadSource? = null,
    val loadTime: Long = 0L,
    val updateSize: Long = 0L,
    val compressionRatio: Float = 0f,
    val error: String? = null,
    val sessionId: String? = null,
    val modelData: ByteArray? = null
)