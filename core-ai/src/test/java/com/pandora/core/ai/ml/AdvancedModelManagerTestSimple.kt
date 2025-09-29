package com.pandora.core.ai.ml

import android.content.Context
import com.pandora.core.ai.TestDataFactory
import com.pandora.core.ai.TestUtils
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.first
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Simplified unit tests for AdvancedModelManager without Log dependencies
 */
class AdvancedModelManagerTestSimple {
    
    private lateinit var context: Context
    private lateinit var advancedModelManager: AdvancedModelManager
    
    @BeforeEach
    fun setUp() {
        context = mockk(relaxed = true)
        
        every { context.assets } returns mockk(relaxed = true)
        
        advancedModelManager = AdvancedModelManager(context)
    }
    
    @Test
    fun `advancedModelManager should be initialized`() {
        assertNotNull(advancedModelManager)
    }
    
    @Test
    fun `analyzeTextAdvanced should return result for empty text`() = runTest {
        val result = advancedModelManager.analyzeTextAdvanced("", TestDataFactory.createTextContext())
        val analysis = result.first()
        assertNotNull(analysis)
    }
    
    @Test
    fun `cleanup should complete without error`() = runTest {
        advancedModelManager.cleanup()
        // Test passes if no exception is thrown
    }
}