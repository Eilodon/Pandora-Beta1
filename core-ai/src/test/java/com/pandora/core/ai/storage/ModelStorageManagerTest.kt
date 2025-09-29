package com.pandora.core.ai.storage

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestBase
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Tests for ModelStorageManager
 */
class ModelStorageManagerTest : TestBase() {
    
    private lateinit var context: Context
    private lateinit var storageManager: ModelStorageManager
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        storageManager = ModelStorageManager(context)
    }
    
    @Test
    fun `saveModel should return true for valid data`() = runTest {
        // Given
        val modelId = "test-model-1"
        val modelData = "test model data".toByteArray()
        val metadata = TestDataFactory.createModelMetadata(id = modelId)
        
        // When
        val result = storageManager.saveModel(modelId, java.nio.ByteBuffer.wrap(modelData), metadata)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `loadModel should return failure for non-existent model`() = runTest {
        // Given
        val nonExistentModelId = "non-existent-model"
        
        // When
        val result = storageManager.loadModel(nonExistentModelId)
        
        // Then
        assertFalse(result.success)
        assertTrue(result.error != null)
    }
    
    @Test
    fun `deleteModel should return false for non-existent model`() = runTest {
        // Given
        val nonExistentModelId = "non-existent-model"
        
        // When
        val result = storageManager.deleteModel(nonExistentModelId)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getStorageStatus should return valid status`() = runTest {
        // When
        val status = storageManager.storageStatus.value
        
        // Then
        assertTrue(status.totalSize >= 0L)
        assertTrue(status.availableSpace >= 0L)
        assertTrue(status.totalModels >= 0)
    }
    
    @Test
    fun `getCachedModels should return valid models`() = runTest {
        // When
        val models = storageManager.cachedModels.value
        
        // Then
        assertTrue(models is Map<String, CachedModel>)
    }
    
    @Test
    fun `decompressModelData should handle gzip data`() = runTest {
        // Given
        val originalData = "test data for compression".toByteArray()
        val gzipCodec = com.pandora.core.ai.compression.GzipCodec()
        val compressedData = gzipCodec.compress(originalData)
        
        // When
        val decompressedData = storageManager.decompressModelData(compressedData, "gzip")
        
        // Then
        assertTrue(decompressedData.isNotEmpty())
        assertEquals(originalData.size, decompressedData.size)
        assertTrue(originalData.contentEquals(decompressedData))
    }
    
    @Test
    fun `decompressModelData should handle uncompressed data`() = runTest {
        // Given
        val originalData = "uncompressed test data".toByteArray()
        
        // When
        val result = storageManager.decompressModelData(originalData, "none")
        
        // Then
        assertTrue(result.isNotEmpty())
        assertEquals(originalData.size, result.size)
        assertTrue(originalData.contentEquals(result))
    }
}
