package com.pandora.core.ai.compression

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * GZIP compression codec
 */
class GzipCodec : CompressionCodec {
    override fun compress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzos ->
            gzos.write(data)
        }
        return baos.toByteArray()
    }

    override fun decompress(data: ByteArray): ByteArray {
        val bais = ByteArrayInputStream(data)
        val baos = ByteArrayOutputStream()
        GZIPInputStream(bais).use { gzis ->
            baos.write(gzis.readBytes())
        }
        return baos.toByteArray()
    }

    override fun isAvailable(): Boolean = true

    override fun getCompressionType(): String = "gzip"
}
