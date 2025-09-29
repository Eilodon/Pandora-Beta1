package com.pandora.core.ai.hybrid

import android.content.Context
import com.pandora.core.ai.storage.IModelStorageManager
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.LoadResult
import com.pandora.core.ai.storage.StorageStatistics
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer

/**
 * Performance tests for model load times without relying on instrumentation.
 */
class ModelLoadPerformanceTest {
    
    private lateinit var storage: FakeStorageManager
    private lateinit var manager: SimpleHybridModelManager
    
    @BeforeEach
    fun setUp() {
        storage = FakeStorageManager()
        val ctx: Context = mockk(relaxed = true)
        manager = SimpleHybridModelManager(ctx, storage)
        manager.initialize()
    }
    
    @Test
    fun `loadModel - network path should take about 1s due to simulated download`() = runBlocking {
        val modelId = "perf-model-1"
        val url = "https://example.com/models/perf-model-1.tflite"
        val expectedVersion = "1.0.0"
        val compression = "none"
        val checksum = "abc123"
        
        val start = System.currentTimeMillis()
        val result = manager.loadModel(
            modelId = modelId,
            modelUrl = url,
            expectedVersion = expectedVersion,
            expectedCompressionType = compression,
            expectedChecksum = checksum,
            forceDownload = true
        )
        val elapsed = System.currentTimeMillis() - start
        
        assertTrue(result.success)
        // Simulated delay is 1000ms; allow tolerance for CI variance
        assertTrue(elapsed in 900..2500, "Expected ~1s load time, actual ${elapsed}ms")
    }
    
    @Test
    fun `loadModel - cache path should be fast after first successful load`() = runBlocking {
        val modelId = "perf-model-2"
        val url = "https://example.com/models/perf-model-2.tflite"
        val expectedVersion = "1.0.0"
        val compression = "none"
        val checksum = "abc123"
        
        // First load (network)
        val first = manager.loadModel(
            modelId = modelId,
            modelUrl = url,
            expectedVersion = expectedVersion,
            expectedCompressionType = compression,
            expectedChecksum = checksum,
            forceDownload = true
        )
        assertTrue(first.success)
        
        // Second load (cache)
        val start = System.currentTimeMillis()
        val second = manager.loadModel(
            modelId = modelId,
            modelUrl = url,
            expectedVersion = expectedVersion,
            expectedCompressionType = compression,
            expectedChecksum = checksum,
            forceDownload = false
        )
        val elapsed = System.currentTimeMillis() - start
        
        assertTrue(second.success)
        // Cache path should be much faster than network path; assert under 200ms for safety
        assertTrue(elapsed in 0..200, "Expected fast cache load, actual ${elapsed}ms")
    }
}

private class FakeStorageManager : IModelStorageManager {
    private val idToBuffer = mutableMapOf<String, ByteBuffer>()
    private val idToMetadata = mutableMapOf<String, ModelMetadata>()
    
    override suspend fun loadModel(modelId: String): LoadResult {
        val buffer = idToBuffer[modelId]
        val metadata = idToMetadata[modelId]
        return if (buffer != null && metadata != null) {
            LoadResult(
                success = true,
                modelId = modelId,
                modelData = null,
                modelBuffer = buffer,
                metadata = metadata,
                error = null
            )
        } else {
            LoadResult(
                success = false,
                modelId = modelId,
                modelData = null,
                modelBuffer = null,
                metadata = null,
                error = "not found"
            )
        }
    }
    
    override suspend fun saveModel(modelId: String, modelBuffer: ByteBuffer, metadata: ModelMetadata): Boolean {
        idToBuffer[modelId] = modelBuffer
        idToMetadata[modelId] = metadata
        return true
    }
    
    override suspend fun deleteModel(modelId: String): Boolean {
        val had = idToBuffer.remove(modelId) != null
        idToMetadata.remove(modelId)
        return had
    }
    
    override fun decompressModelData(data: ByteArray, compressionType: String): ByteArray {
        // For this performance test, just pass-through
        return data
    }
    
    override fun getStorageStatistics(): StorageStatistics {
        val totalSize = idToBuffer.values.sumOf { it.remaining().toLong() }
        return StorageStatistics(
            totalSize = totalSize,
            maxSize = 500L * 1024 * 1024,
            usagePercentage = if (totalSize == 0L) 0 else (totalSize * 100 / (500L * 1024 * 1024)),
            totalModels = idToBuffer.size,
            maxModels = 100,
            pinnedModels = 0,
            availableSpace = (500L * 1024 * 1024) - totalSize
        )
    }
}
