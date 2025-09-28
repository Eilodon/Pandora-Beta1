package com.pandora.core.ai.storage

import java.nio.ByteBuffer

/**
 * Interface for Model Storage Manager
 * Allows for easy mocking in tests
 */
interface IModelStorageManager {
    suspend fun loadModel(modelId: String): LoadResult
    suspend fun saveModel(modelId: String, modelBuffer: ByteBuffer, metadata: ModelMetadata): Boolean
    suspend fun deleteModel(modelId: String): Boolean
    fun decompressModelData(data: ByteArray, compressionType: String): ByteArray
    fun getStorageStatistics(): StorageStatistics
}
