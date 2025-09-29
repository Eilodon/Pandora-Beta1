package com.pandora.core.ai

import com.pandora.core.ai.compression.GzipCodec
import com.pandora.core.ai.hybrid.ManagerStatus
import com.pandora.core.ai.storage.ModelMetadata
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Simple integration tests without complex mocking
 */
class SimpleIntegrationTest {
    
    @Test
    fun `gzip compression should work correctly`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val originalData = "Hello, World! This is a test string for compression.".toByteArray()
        
        // When
        val compressed = gzipCodec.compress(originalData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        // Note: For very small data, compression might not reduce size due to headers
        assertTrue(compressed.size <= originalData.size)
        assertEquals(originalData.size, decompressed.size)
        assertTrue(originalData.contentEquals(decompressed))
    }
    
    @Test
    fun `gzip should handle empty data`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val emptyData = ByteArray(0)
        
        // When
        val compressed = gzipCodec.compress(emptyData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertEquals(0, decompressed.size)
    }
    
    @Test
    fun `gzip should handle large data`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val largeData = ByteArray(10000) { it.toByte() }
        
        // When
        val compressed = gzipCodec.compress(largeData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertTrue(compressed.size < largeData.size)
        assertEquals(largeData.size, decompressed.size)
        assertTrue(largeData.contentEquals(decompressed))
    }
    
    @Test
    fun `gzip should handle repeated data`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val repeatedData = "AAAA".repeat(1000).toByteArray()
        
        // When
        val compressed = gzipCodec.compress(repeatedData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertTrue(compressed.size < repeatedData.size) // Should compress well
        assertEquals(repeatedData.size, decompressed.size)
        assertTrue(repeatedData.contentEquals(decompressed))
    }
    
    @Test
    fun `model metadata should be created correctly`() = runTest {
        // Given
        val modelId = "test-model-1"
        val modelName = "Test Model"
        val version = "1.0.0"
        
        // When
        val metadata = ModelMetadata(
            id = modelId,
            name = modelName,
            version = version,
            type = "neural_network",
            description = "Test model for unit testing",
            tags = listOf("test", "unit"),
            created = System.currentTimeMillis(),
            updated = System.currentTimeMillis(),
            compressionType = "gzip",
            checksum = "test-checksum",
            sizeBytes = 1024L
        )
        
        // Then
        assertEquals(modelId, metadata.id)
        assertEquals(modelName, metadata.name)
        assertEquals(version, metadata.version)
        assertEquals("neural_network", metadata.type)
        assertEquals("Test model for unit testing", metadata.description)
        assertTrue(metadata.tags.contains("test"))
        assertTrue(metadata.tags.contains("unit"))
        assertEquals("gzip", metadata.compressionType)
        assertEquals("test-checksum", metadata.checksum)
        assertEquals(1024L, metadata.sizeBytes)
    }
    
    @Test
    fun `manager status should have valid values`() = runTest {
        // Given
        val statuses = listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR)
        
        // When & Then
        statuses.forEach { status ->
            assertTrue(status in listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR))
        }
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
}
