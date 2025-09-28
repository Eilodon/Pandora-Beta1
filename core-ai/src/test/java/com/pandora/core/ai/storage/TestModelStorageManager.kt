package com.pandora.core.ai.storage

import java.nio.ByteBuffer
import kotlinx.coroutines.delay

/**
 * Test implementation of IModelStorageManager for unit tests
 */
class TestModelStorageManager : IModelStorageManager {
    
    private val models = mutableMapOf<String, Pair<ByteBuffer, ModelMetadata>>()
    
    override suspend fun loadModel(modelId: String): LoadResult {
        delay(10) // Simulate loading time
        val model = models[modelId]
        return if (model != null) {
            LoadResult(
                success = true,
                modelId = modelId,
                modelBuffer = model.first,
                metadata = model.second
            )
        } else {
            LoadResult(
                success = false,
                modelId = modelId,
                error = "Model not found"
            )
        }
    }
    
    override suspend fun saveModel(modelId: String, modelBuffer: ByteBuffer, metadata: ModelMetadata): Boolean {
        delay(10) // Simulate saving time
        models[modelId] = Pair(modelBuffer, metadata)
        return true
    }
    
    override suspend fun deleteModel(modelId: String): Boolean {
        delay(10) // Simulate deletion time
        return models.remove(modelId) != null
    }
    
    override fun decompressModelData(data: ByteArray, compressionType: String): ByteArray {
        // Simple implementation - just return the data as-is
        return data
    }
    
    override fun getStorageStatistics(): StorageStatistics {
        return StorageStatistics(
            totalSize = models.values.sumOf { it.first.remaining().toLong() },
            maxSize = Long.MAX_VALUE,
            usagePercentage = 0L,
            totalModels = models.size,
            maxModels = 100,
            pinnedModels = 0,
            availableSpace = Long.MAX_VALUE
        )
    }
}
