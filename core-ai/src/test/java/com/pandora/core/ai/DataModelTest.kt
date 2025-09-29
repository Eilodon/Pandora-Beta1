package com.pandora.core.ai

import com.pandora.core.ai.hybrid.*
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.StorageStatus
import com.pandora.core.ai.storage.StorageResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse

/**
 * Tests for data models and enums
 */
class DataModelTest {
    
    @Test
    fun `model metadata should have correct properties`() = runTest {
        // Given
        val metadata = ModelMetadata(
            id = "test-model",
            name = "Test Model",
            version = "1.0.0",
            type = "neural_network",
            description = "Test description",
            tags = listOf("test", "ai"),
            created = 1234567890L,
            updated = 1234567890L,
            compressionType = "gzip",
            checksum = "abc123",
            sizeBytes = 1024L
        )
        
        // Then
        assertEquals("test-model", metadata.id)
        assertEquals("Test Model", metadata.name)
        assertEquals("1.0.0", metadata.version)
        assertEquals("neural_network", metadata.type)
        assertEquals("Test description", metadata.description)
        assertTrue(metadata.tags.contains("test"))
        assertTrue(metadata.tags.contains("ai"))
        assertEquals(1234567890L, metadata.created)
        assertEquals(1234567890L, metadata.updated)
        assertEquals("gzip", metadata.compressionType)
        assertEquals("abc123", metadata.checksum)
        assertEquals(1024L, metadata.sizeBytes)
    }
    
    @Test
    fun `storage status should have correct properties`() = runTest {
        // Given
        val status = StorageStatus(
            isInitialized = true,
            totalModels = 5,
            totalSize = 1024000L,
            availableSpace = 2048000L,
            lastCleanup = 1234567890L
        )
        
        // Then
        assertTrue(status.isInitialized)
        assertEquals(5, status.totalModels)
        assertEquals(1024000L, status.totalSize)
        assertEquals(2048000L, status.availableSpace)
        assertEquals(1234567890L, status.lastCleanup)
    }
    
    @Test
    fun `storage result should have correct properties`() = runTest {
        // Given
        val successResult = StorageResult(
            success = true,
            modelId = "test-model",
            storedSize = 1024,
            compressionRatio = 0.5f,
            error = null
        )
        
        val failureResult = StorageResult(
            success = false,
            modelId = "test-model",
            storedSize = 0,
            compressionRatio = 1.0f,
            error = "Storage failed"
        )
        
        // Then
        assertTrue(successResult.success)
        assertEquals("test-model", successResult.modelId)
        assertEquals(1024, successResult.storedSize)
        assertEquals(0.5f, successResult.compressionRatio)
        assertTrue(successResult.error == null)
        
        assertFalse(failureResult.success)
        assertEquals("test-model", failureResult.modelId)
        assertEquals(0, failureResult.storedSize)
        assertEquals(1.0f, failureResult.compressionRatio)
        assertEquals("Storage failed", failureResult.error)
    }
    
    @Test
    fun `test data factory should create valid objects`() = runTest {
        // Given
        val modelId = "test-model-factory"
        
        // When
        val metadata = TestDataFactory.createModelMetadata(id = modelId)
        
        // Then
        assertEquals(modelId, metadata.id)
        assertTrue(metadata.name.isNotEmpty())
        assertTrue(metadata.version.isNotEmpty())
        assertTrue(metadata.type.isNotEmpty())
        assertTrue(metadata.description.isNotEmpty())
        assertTrue(metadata.tags.isNotEmpty())
        assertTrue(metadata.created > 0)
        assertTrue(metadata.updated > 0)
        assertTrue(metadata.sizeBytes > 0)
    }
    
    @Test
    fun `manager status should have correct values`() = runTest {
        // Given
        val idle = ManagerStatus.IDLE
        val loading = ManagerStatus.LOADING
        val error = ManagerStatus.ERROR
        
        // Then
        assertTrue(idle in listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR))
        assertTrue(loading in listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR))
        assertTrue(error in listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR))
    }
    
    @Test
    fun `load source should have correct values`() = runTest {
        // Given
        val cache = LoadSource.CACHE
        val networkFull = LoadSource.NETWORK_FULL
        val networkDelta = LoadSource.NETWORK_DELTA
        
        // Then
        assertTrue(cache in listOf(LoadSource.CACHE, LoadSource.NETWORK_FULL, LoadSource.NETWORK_DELTA))
        assertTrue(networkFull in listOf(LoadSource.CACHE, LoadSource.NETWORK_FULL, LoadSource.NETWORK_DELTA))
        assertTrue(networkDelta in listOf(LoadSource.CACHE, LoadSource.NETWORK_FULL, LoadSource.NETWORK_DELTA))
    }
    
    @Test
    fun `model priority should have correct values`() = runTest {
        // Given
        val low = ModelPriority.LOW
        val high = ModelPriority.HIGH
        
        // Then
        assertTrue(low in listOf(ModelPriority.LOW, ModelPriority.HIGH))
        assertTrue(high in listOf(ModelPriority.LOW, ModelPriority.HIGH))
    }
    
    @Test
    fun `model load result should have correct properties`() = runTest {
        // Given
        val metadata = TestDataFactory.createModelMetadata()
        val result = ModelLoadResult(
            success = true,
            modelId = "test-model",
            modelBuffer = java.nio.ByteBuffer.wrap("test".toByteArray()),
            metadata = metadata,
            source = LoadSource.CACHE,
            loadTime = 100L,
            updateSize = 512L,
            compressionRatio = 0.5f,
            error = null,
            sessionId = "session-123",
            modelData = "test".toByteArray()
        )
        
        // Then
        assertTrue(result.success)
        assertEquals("test-model", result.modelId)
        assertTrue(result.modelBuffer != null)
        assertEquals(metadata, result.metadata)
        assertEquals(LoadSource.CACHE, result.source)
        assertEquals(100L, result.loadTime)
        assertEquals(512L, result.updateSize)
        assertEquals(0.5f, result.compressionRatio)
        assertTrue(result.error == null)
        assertEquals("session-123", result.sessionId)
        assertTrue(result.modelData != null)
    }
}
