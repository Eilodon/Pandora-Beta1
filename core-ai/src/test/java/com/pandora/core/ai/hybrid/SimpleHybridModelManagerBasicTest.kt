package com.pandora.core.ai.hybrid

import android.content.Context
import com.pandora.core.ai.storage.ModelStorageManager
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
 * Basic Tests for SimpleHybridModelManager
 * Simple tests without external dependencies
 */
@RunWith(MockitoJUnitRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class SimpleHybridModelManagerBasicTest {

    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockStorageManager: ModelStorageManager
    
    private lateinit var hybridModelManager: SimpleHybridModelManager
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testDispatcher = UnconfinedTestDispatcher()
        hybridModelManager = SimpleHybridModelManager(mockContext, mockStorageManager)
    }

    @After
    fun teardown() {
        // Clean up
    }

    @Test
    fun testLoadModel_forceDownload() = runTest(testDispatcher) {
        // Given
        val modelId = "testModel"
        val modelUrl = "http://example.com/model.tflite"
        val expectedVersion = "1.0"
        val expectedCompressionType = "none"
        val expectedChecksum = "abc"
        
        val mockMetadata = ModelMetadata(
            id = modelId,
            name = "Test Model",
            version = expectedVersion,
            type = "tflite",
            description = "Test model",
            tags = listOf("test"),
            created = System.currentTimeMillis(),
            updated = System.currentTimeMillis(),
            compressionType = expectedCompressionType,
            checksum = expectedChecksum,
            sizeBytes = 1024L
        )
        
        whenever(mockStorageManager.loadModel(modelId)).thenReturn(
            LoadResult(success = false, modelId = modelId)
        )
        whenever(mockStorageManager.decompressModelData(any(), any())).thenReturn(ByteArray(1024))
        whenever(mockStorageManager.saveModel(any(), any(), any())).thenReturn(true)

        // When
        val result = hybridModelManager.loadModel(
            modelId = modelId,
            modelUrl = modelUrl,
            expectedVersion = expectedVersion,
            expectedCompressionType = expectedCompressionType,
            expectedChecksum = expectedChecksum,
            forceDownload = true
        )

        // Then
        assertTrue(result.success)
        assertEquals("testModel", result.modelId)
        assertEquals(LoadSource.NETWORK_FULL, result.source)
        assertTrue(result.modelBuffer != null)
    }

    @Test
    fun testUnloadModel() = runTest(testDispatcher) {
        // Given
        val modelId = "testModel"
        whenever(mockStorageManager.deleteModel(modelId)).thenReturn(true)

        // When
        val result = hybridModelManager.unloadModel(modelId)

        // Then
        assertTrue(result)
    }

    @Test
    fun testManagerInitialization() = runTest(testDispatcher) {
        // When
        hybridModelManager.initialize()
        
        // Then
        val status = hybridModelManager.managerStatus.value
        assertTrue(status.isInitialized)
        assertFalse(status.isLoading)
    }
}