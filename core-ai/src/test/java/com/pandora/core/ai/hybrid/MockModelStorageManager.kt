package com.pandora.core.ai.hybrid

import com.pandora.core.ai.storage.IModelStorageManager
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.LoadResult
import java.nio.ByteBuffer

/**
 * Mock implementation of IModelStorageManager for testing
 */
class MockModelStorageManager : IModelStorageManager {
    private var loadModelResult: LoadResult = LoadResult(success = false, modelId = "test")
    private var saveModelResult: Boolean = true
    private var deleteModelResult: Boolean = true
    private var decompressModelDataResult: ByteArray = "test_data".toByteArray()
    
    fun setLoadModelResult(result: LoadResult) {
        loadModelResult = result
    }
    
    fun setSaveModelResult(result: Boolean) {
        saveModelResult = result
    }
    
    fun setDeleteModelResult(result: Boolean) {
        deleteModelResult = result
    }
    
    fun setDecompressModelDataResult(result: ByteArray) {
        decompressModelDataResult = result
    }
    
    override suspend fun loadModel(modelId: String): LoadResult = loadModelResult
    override fun getStorageStatistics() = com.pandora.core.ai.storage.StorageStatistics(
        totalSize = 0L,
        maxSize = 1000L,
        usagePercentage = 0L,
        totalModels = 0,
        maxModels = 10,
        pinnedModels = 0,
        availableSpace = 1000L
    )
    override fun decompressModelData(data: ByteArray, compressionType: String): ByteArray = decompressModelDataResult
    override suspend fun saveModel(modelId: String, modelBuffer: java.nio.ByteBuffer, metadata: ModelMetadata): Boolean = saveModelResult
    override suspend fun deleteModel(modelId: String): Boolean = deleteModelResult
}
