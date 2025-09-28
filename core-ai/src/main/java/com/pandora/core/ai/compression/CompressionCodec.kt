package com.pandora.core.ai.compression

/**
 * Interface for compression codecs
 */
interface CompressionCodec {
    fun compress(data: ByteArray): ByteArray
    fun decompress(data: ByteArray): ByteArray
    fun isAvailable(): Boolean
    fun getCompressionType(): String
}