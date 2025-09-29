package com.pandora.core.ai

import com.pandora.core.ai.compression.GzipCodec
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Debug test to check compression
 */
class DebugTest {
    
    @Test
    fun `debug gzip compression`() = runTest {
        // Given
        val gzipCodec = GzipCodec()
        val originalData = "Hello, World!".toByteArray()
        
        println("Original data size: ${originalData.size}")
        println("Original data: ${String(originalData)}")
        
        // When
        val compressed = gzipCodec.compress(originalData)
        println("Compressed data size: ${compressed.size}")
        
        val decompressed = gzipCodec.decompress(compressed)
        println("Decompressed data size: ${decompressed.size}")
        println("Decompressed data: ${String(decompressed)}")
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertEquals(originalData.size, decompressed.size)
        assertTrue(originalData.contentEquals(decompressed))
    }
}
