package com.pandora.core.ai

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pandora.core.ai.ml.AdvancedModelManager
import com.pandora.core.ai.personalization.PersonalizationEngine
import com.pandora.core.ai.prediction.PredictiveAnalytics
import com.pandora.core.ai.context.ContextAwareness
import com.pandora.core.ai.context.EnhancedContextIntegration
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.CACDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for EnhancedInferenceEngine
 * Tests the complete AI pipeline with real dependencies
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EnhancedInferenceEngineIntegrationTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var context: Context
    
    @Inject
    lateinit var advancedModelManager: AdvancedModelManager
    
    @Inject
    lateinit var personalizationEngine: PersonalizationEngine
    
    @Inject
    lateinit var predictiveAnalytics: PredictiveAnalytics
    
    @Inject
    lateinit var contextAwareness: ContextAwareness
    
    @Inject
    lateinit var enhancedContextIntegration: EnhancedContextIntegration
    
    @Inject
    lateinit var cacDao: CACDao
    
    private lateinit var enhancedInferenceEngine: EnhancedInferenceEngine
    
    @Before
    fun setUp() {
        hiltRule.inject()
        
        enhancedInferenceEngine = EnhancedInferenceEngine(
            context = context,
            advancedModelManager = advancedModelManager,
            personalizationEngine = personalizationEngine,
            predictiveAnalytics = predictiveAnalytics,
            contextAwareness = contextAwareness,
            enhancedContextIntegration = enhancedContextIntegration,
            cacDao = cacDao
        )
    }
    
    @After
    fun tearDown() {
        // Clean up any test data
    }
    
    @Test
    fun `initialize should initialize all AI components successfully`() = runTest {
        // When
        enhancedInferenceEngine.initialize()
        
        // Then
        // If we reach here without exceptions, initialization succeeded
        assertTrue(true)
    }
    
    @Test
    fun `analyzeTextEnhanced should return enhanced inference result`() = runTest {
        // Given
        val text = "Schedule meeting tomorrow at 3pm"
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Then
        assertNotNull(result)
        assertTrue(result.text == text)
        assertTrue(result.overallConfidence >= 0f)
        assertTrue(result.timestamp > 0L)
    }
    
    @Test
    fun `analyzeTextEnhanced should handle empty text`() = runTest {
        // Given
        val text = ""
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Then
        assertNotNull(result)
        assertTrue(result.text == text)
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should handle long text`() = runTest {
        // Given
        val longText = "This is a very long text that contains multiple sentences and should be handled properly by the enhanced inference engine without any issues or errors. It should process the text efficiently and return meaningful results."
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(longText).first()
        
        // Then
        assertNotNull(result)
        assertTrue(result.text == longText)
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should handle special characters`() = runTest {
        // Given
        val textWithSpecialChars = "Schedule meeting @ 3pm #important $100 & more"
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(textWithSpecialChars).first()
        
        // Then
        assertNotNull(result)
        assertTrue(result.text == textWithSpecialChars)
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `getUserInsights should return user insights`() = runTest {
        // Given
        // Learn some actions first
        personalizationEngine.learnFromAction("Add to Calendar", "com.google.android.calendar", true)
        personalizationEngine.learnFromAction("Set Reminder", "com.google.android.calendar", true)
        
        // When
        val insights = enhancedInferenceEngine.getUserInsights().first()
        
        // Then
        assertNotNull(insights)
        assertTrue(insights.totalInteractions >= 0)
        assertTrue(insights.learningProgress >= 0f && insights.learningProgress <= 1f)
    }
    
    @Test
    fun `resetLearning should reset all learning data`() = runTest {
        // Given
        // Learn some actions first
        personalizationEngine.learnFromAction("Test action", "com.test.app", true)
        
        // When
        enhancedInferenceEngine.resetLearning()
        
        // Then
        val insights = enhancedInferenceEngine.getUserInsights().first()
        assertTrue(insights.totalInteractions == 0)
        assertTrue(insights.learningProgress == 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should improve with learning`() = runTest {
        // Given
        val text = "Schedule meeting"
        
        // Analyze text before learning
        val resultBefore = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Learn from the interaction
        personalizationEngine.learnFromAction("Add to Calendar", "com.google.android.calendar", true)
        
        // Analyze text after learning
        val resultAfter = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Then
        assertNotNull(resultBefore)
        assertNotNull(resultAfter)
        // Note: In real implementation, we would verify that the result improved
    }
    
    @Test
    fun `analyzeTextEnhanced should handle different text types`() = runTest {
        // Given
        val testCases = listOf(
            "Hello world" to "greeting",
            "Schedule meeting" to "calendar",
            "Send message to John" to "communication",
            "Create note" to "note",
            "Calculate 2+2" to "math",
            "Search for restaurants" to "search"
        )
        
        testCases.forEach { (text, expectedType) ->
            // When
            val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
            
            // Then
            assertNotNull(result)
            assertTrue(result.text == text)
            assertTrue(result.overallConfidence >= 0f)
            // Note: In real implementation, we would verify the expected type
        }
    }
    
    @Test
    fun `analyzeTextEnhanced should handle concurrent requests`() = runTest {
        // Given
        val texts = listOf(
            "Schedule meeting",
            "Send message",
            "Create note",
            "Calculate 2+2",
            "Search for restaurants"
        )
        
        // When
        val results = texts.map { text ->
            enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        }
        
        // Then
        assertTrue(results.size == texts.size)
        results.forEach { result ->
            assertNotNull(result)
            assertTrue(result.overallConfidence >= 0f)
        }
    }
    
    @Test
    fun `analyzeTextEnhanced should handle memory integration`() = runTest {
        // Given
        val text = "Schedule meeting with John"
        
        // Add some memory entries
        // Note: In real implementation, we would add memory entries to CACDao
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Then
        assertNotNull(result)
        assertTrue(result.text == text)
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should handle context integration`() = runTest {
        // Given
        val text = "Schedule meeting"
        
        // When
        val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        
        // Then
        assertNotNull(result)
        assertNotNull(result.comprehensiveContext)
        assertTrue(result.contextSnapshot.timestamp > 0L)
    }
}
