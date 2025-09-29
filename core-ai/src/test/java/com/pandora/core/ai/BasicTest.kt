package com.pandora.core.ai

import com.pandora.core.ai.compression.GzipCodec
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Basic tests without complex assertions
 */
class BasicTest {
    
    @Test
    fun `basic gzip test`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val originalData = "Hello".toByteArray()
        
        // When
        val compressed = gzipCodec.compress(originalData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertEquals(originalData.size, decompressed.size)
        assertTrue(originalData.contentEquals(decompressed))
    }
    
    @Test
    fun `basic model metadata test`() = runTest {
        // Given
        val modelId = "test-model"
        
        // When
        val metadata = TestDataFactory.createModelMetadata(id = modelId)
        
        // Then
        assertEquals(modelId, metadata.id)
        assertTrue(metadata.name.isNotEmpty())
    }
}
