package com.pandora.core.ai.hybrid

import android.content.Context
import com.pandora.core.ai.storage.IModelStorageManager
import org.mockito.Mockito.mock
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.LoadResult
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue


/**
 * Comprehensive Integration Tests for SimpleHybridModelManager
 * Tests all major workflows and edge cases
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SimpleHybridModelManagerIntegrationTest {

    private lateinit var mockContext: Context
    private lateinit var mockStorageManager: MockModelStorageManager
    private lateinit var hybridModelManager: SimpleHybridModelManager
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        // Create mock objects
        mockContext = mock<Context>()
        mockStorageManager = MockModelStorageManager()

        hybridModelManager = SimpleHybridModelManager(mockContext, mockStorageManager)
    }

    @After
    fun teardown() {
        // Clean up
    }

    @Test
    fun testModelLoadFromCache() = runTest(testDispatcher) {
        val modelId = "cachedModel"
        val modelUrl = "http://example.com/cached.tflite"
        val expectedVersion = "1.0"
        val expectedCompressionType = "none"
        val expectedChecksum = "cached123"
        val dummyModelData = "cached_content".toByteArray()
        val dummyModelBuffer = ByteBuffer.wrap(dummyModelData)
        val metadata = ModelMetadata(
            id = modelId,
            name = modelId,
            version = expectedVersion,
            type = "tflite",
            description = "Cached AI Model",
            tags = listOf("ai", "cached"),
            created = System.currentTimeMillis(),
            updated = System.currentTimeMillis(),
            compressionType = expectedCompressionType,
            checksum = expectedChecksum,
            sizeBytes = dummyModelData.size.toLong()
        )

        // Setup mock to return cached model
        mockStorageManager.setLoadModelResult(
            LoadResult(
                success = true,
                modelId = modelId,
                modelData = dummyModelData,
                modelBuffer = dummyModelBuffer,
                metadata = metadata
            )
        )

        val result = hybridModelManager.loadModel(
            modelId = modelId,
            modelUrl = modelUrl,
            expectedVersion = expectedVersion,
            expectedCompressionType = expectedCompressionType,
            expectedChecksum = expectedChecksum,
            forceDownload = false
        )

        assertTrue(result.success)
        assertEquals(modelId, result.modelId)
        assertEquals(LoadSource.CACHE, result.source)
        assertTrue(result.modelBuffer != null)
        assertEquals(metadata.version, result.metadata?.version)
    }

    @Test
    fun testModelLoadFromNetwork() = runTest(testDispatcher) {
        val modelId = "networkModel"
        val modelUrl = "http://example.com/network.tflite"
        val expectedVersion = "1.0"
        val expectedCompressionType = "none"
        val expectedChecksum = "network123"
        val dummyModelData = "network_content".toByteArray()

        // Setup mock to simulate network download
        mockStorageManager.setLoadModelResult(LoadResult(success = false, modelId = modelId))
        mockStorageManager.setDecompressModelDataResult(dummyModelData)
        mockStorageManager.setSaveModelResult(true)

        val result = hybridModelManager.loadModel(
            modelId = modelId,
            modelUrl = modelUrl,
            expectedVersion = expectedVersion,
            expectedCompressionType = expectedCompressionType,
            expectedChecksum = expectedChecksum,
            forceDownload = true
        )

        assertTrue(result.success)
        assertEquals(modelId, result.modelId)
        assertEquals(LoadSource.NETWORK_FULL, result.source)
        assertTrue(result.modelBuffer != null)
    }

    @Test
    fun testModelUnload() = runTest(testDispatcher) {
        val modelId = "testModel"
        mockStorageManager.setDeleteModelResult(true)
        
        val result = hybridModelManager.unloadModel(modelId)
        assertTrue(result)
    }

    @Test
    fun testManagerInitialization() = runTest(testDispatcher) {
        hybridModelManager.initialize()
        
        val status = hybridModelManager.managerStatus.value
        assertTrue(status.isInitialized)
        assertFalse(status.isLoading)
    }

    @Test
    fun testConcurrentModelLoading() = runTest(testDispatcher) {
        val dummyModelData = "concurrent_content".toByteArray()
        
        // Setup mock for concurrent loading
        mockStorageManager.setLoadModelResult(LoadResult(success = false, modelId = "concurrent"))
        mockStorageManager.setDecompressModelDataResult(dummyModelData)
        mockStorageManager.setSaveModelResult(true)
        
        val jobs = (1..3).map { i ->
            async {
                hybridModelManager.loadModel(
                    modelId = "concurrent$i",
                    modelUrl = "http://example.com/concurrent$i.tflite",
                    expectedVersion = "1.0",
                    expectedCompressionType = "none",
                    expectedChecksum = "concurrent$i",
                    forceDownload = true
                )
            }
        }
        
        val results = jobs.awaitAll()
        
        results.forEach { result ->
            assertTrue(result.success)
            assertEquals(LoadSource.NETWORK_FULL, result.source)
        }
    }
}