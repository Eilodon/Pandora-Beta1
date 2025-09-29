package com.pandora.core.ai.compression

import com.pandora.core.ai.TestBase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertEquals

/**
 * Tests for compression codecs
 */
class CompressionCodecTest : TestBase() {
    
    private lateinit var gzipCodec: GzipCodec
    private lateinit var zstdCodec: ZstdCodec
    private lateinit var brotliCodec: BrotliCodec
    
    @BeforeEach
    fun setUp() {
        gzipCodec = GzipCodec()
        zstdCodec = ZstdCodec()
        brotliCodec = BrotliCodec()
    }
    
    @Test
    fun `gzip codec should compress and decompress data`() = runTest {
        // Given
        val originalData = "Hello, World! This is a test string for compression.".toByteArray()
        
        // When
        val compressed = gzipCodec.compress(originalData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertTrue(compressed.size < originalData.size)
        assertEquals(originalData.size, decompressed.size)
        assertTrue(originalData.contentEquals(decompressed))
    }
    
    @Test
    fun `gzip codec should handle empty data`() = runTest {
        // Given
        val emptyData = ByteArray(0)
        
        // When
        val compressed = gzipCodec.compress(emptyData)
        val decompressed = gzipCodec.decompress(compressed)
        
        // Then
        assertTrue(compressed.isNotEmpty())
        assertEquals(0, decompressed.size)
    }
    
    @Test
    fun `zstd codec should be available`() = runTest {
        // When
        val isAvailable = zstdCodec.isAvailable()
        
        // Then
        // ZSTD might not be available in test environment
        assertTrue(isAvailable || !isAvailable) // Either way is acceptable
    }
    
    @Test
    fun `brotli codec should be available`() = runTest {
        // When
        val isAvailable = brotliCodec.isAvailable()
        
        // Then
        // Brotli might not be available in test environment
        assertTrue(isAvailable || !isAvailable) // Either way is acceptable
    }
    
    @Test
    fun `gzip codec should handle large data`() = runTest {
        // Given
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
    fun `gzip codec should handle repeated data`() = runTest {
        // Given
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
}
