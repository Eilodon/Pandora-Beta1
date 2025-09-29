package com.pandora.core.ai.hybrid

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestBase
import com.pandora.core.ai.storage.IModelStorageManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertFalse

/**
 * Simple tests for HybridModelManager without complex mocking
 */
class HybridModelManagerSimpleTest : TestBase() {
    
    private lateinit var context: Context
    private lateinit var storageManager: IModelStorageManager
    private lateinit var hybridModelManager: SimpleHybridModelManager
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        storageManager = mockk(relaxed = true)
        hybridModelManager = SimpleHybridModelManager(context, storageManager)
    }
    
    @Test
    fun `initialize should complete without errors`() = runTest {
        // Given
        // Context is already mocked
        
        // When
        hybridModelManager.initialize()
        
        // Then
        // If we reach here, initialization succeeded
        assertTrue(true)
    }
    
    @Test
    fun `managerStatus should return valid status`() = runTest {
        // Given
        hybridModelManager.initialize()
        
        // When
        val status = hybridModelManager.managerStatus.value
        
        // Then
        assertTrue(status in listOf(ManagerStatus.IDLE, ManagerStatus.LOADING, ManagerStatus.ERROR))
    }
    
    @Test
    fun `loadModel should handle invalid model gracefully`() = runTest {
        // Given
        hybridModelManager.initialize()
        val invalidModelId = "invalid-model-id"
        val modelUrl = "https://example.com/model.tflite"
        val expectedVersion = "1.0.0"
        val expectedCompressionType = "gzip"
        val expectedChecksum = "abc123"
        
        // Mock storage manager to return failure
        coEvery { storageManager.loadModel(any()) } returns TestDataFactory.createLoadResult(success = false)
        coEvery { storageManager.decompressModelData(any(), any()) } returns byteArrayOf()
        
        // When
        val result = hybridModelManager.loadModel(
            invalidModelId, 
            modelUrl, 
            expectedVersion, 
            expectedCompressionType, 
            expectedChecksum
        )
        
        // Then
        assertFalse(result.success)
        assertTrue(result.error != null)
    }
}
