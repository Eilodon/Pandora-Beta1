package com.pandora.core.ai

import com.pandora.core.ai.hybrid.*
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.LoadResult
import java.nio.ByteBuffer

/**
 * Test data factory for creating test objects
 */
object TestDataFactory {
    
    fun createModelMetadata(
        id: String = "test-model-1",
        name: String = "Test Model",
        version: String = "1.0.0",
        type: String = "neural_network",
        description: String = "Test model for unit testing",
        tags: List<String> = listOf("test", "unit"),
        created: Long = System.currentTimeMillis(),
        updated: Long = System.currentTimeMillis(),
        compressionType: String = "none",
        checksum: String = "test-checksum",
        sizeBytes: Long = 1024L
    ): ModelMetadata {
        return ModelMetadata(
            id = id,
            name = name,
            version = version,
            type = type,
            description = description,
            tags = tags,
            created = created,
            updated = updated,
            compressionType = compressionType,
            checksum = checksum,
            sizeBytes = sizeBytes
        )
    }
    
    fun createLoadResult(
        success: Boolean = true,
        modelId: String = "test-model-1",
        modelData: ByteArray? = null,
        modelBuffer: ByteBuffer? = null,
        metadata: ModelMetadata? = null,
        error: String? = null
    ): LoadResult {
        return LoadResult(
            success = success,
            modelId = modelId,
            modelData = modelData,
            modelBuffer = modelBuffer,
            metadata = metadata,
            error = error
        )
    }
    
    fun createModelLoadResult(
        success: Boolean = true,
        modelId: String = "test-model-1",
        modelBuffer: ByteBuffer? = null,
        metadata: ModelMetadata? = null,
        source: LoadSource? = LoadSource.CACHE,
        loadTime: Long = 100L,
        updateSize: Long = 0L,
        compressionRatio: Float = 0f,
        error: String? = null,
        sessionId: String? = null,
        modelData: ByteArray? = null
    ): ModelLoadResult {
        return ModelLoadResult(
            success = success,
            modelId = modelId,
            modelBuffer = modelBuffer,
            metadata = metadata,
            source = source,
            loadTime = loadTime,
            updateSize = updateSize,
            compressionRatio = compressionRatio,
            error = error,
            sessionId = sessionId,
            modelData = modelData
        )
    }
    
    fun createManagerStatus(
        status: ManagerStatus = ManagerStatus.IDLE
    ): ManagerStatus {
        return status
    }
    
    fun createModelSession(
        sessionId: String = "test-session-1",
        modelId: String = "test-model-1",
        modelBuffer: ByteBuffer = createTestByteBuffer(),
        metadata: ModelMetadata = createModelMetadata(),
        source: String = "cache",
        loadTime: Long = 100L,
        accessCount: Int = 1,
        isActive: Boolean = true,
        createdAt: Long = System.currentTimeMillis(),
        lastAccessed: Long = System.currentTimeMillis()
    ): ModelSession {
        return ModelSession(
            sessionId = sessionId,
            modelId = modelId,
            modelBuffer = modelBuffer,
            metadata = metadata,
            source = source,
            loadTime = loadTime,
            accessCount = accessCount,
            isActive = isActive,
            createdAt = createdAt,
            lastAccessed = lastAccessed
        )
    }
    
    private fun createTestByteBuffer(): ByteBuffer {
        val data = ByteArray(1024) { it.toByte() }
        return ByteBuffer.wrap(data)
    }
    
    fun createComprehensiveContext(
        @Suppress("UNUSED_PARAMETER") timestamp: Long = System.currentTimeMillis()
    ): com.pandora.core.ai.context.ComprehensiveContext {
        return com.pandora.core.ai.context.ComprehensiveContext.createEmpty()
    }
    
    fun createTextContext(
        text: String = "test text",
        appPackage: String = "com.test.app"
    ): com.pandora.core.ai.ml.TextContext {
        return com.pandora.core.ai.ml.TextContext(
            timestamp = System.currentTimeMillis(),
            location = null,
            appPackage = appPackage,
            userActivity = null,
            recentTexts = listOf(text)
        )
    }
}
