package com.pandora.core.ai.hybrid

import com.pandora.core.ai.storage.ModelMetadata

/**
 * Information about a model update
 */
data class ModelUpdate(
    val modelId: String,
    val currentVersion: String,
    val newVersion: String,
    val updateSize: Long,
    val updateType: UpdateType,
    val deltaUrl: String? = null,
    val fullUrl: String? = null,
    val checksum: String,
    val compressionType: String,
    val metadata: ModelMetadata
)

/**
 * Type of model update
 */
enum class UpdateType {
    DELTA, // Only changed parts
    FULL,  // Complete model replacement
    PATCH  // Small fixes
}
