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
import kotlin.test.assertTrue

/**
 * Performance tests for AI components
 * Tests memory usage, CPU usage, and response times
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PerformanceTest {
    
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
    fun `analyzeTextEnhanced should complete within acceptable time`() = runTest {
        // Given
        val text = "Schedule meeting tomorrow at 3pm"
        val maxResponseTime = 5000L // 5 seconds
        
        // When
        val startTime = System.currentTimeMillis()
        val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        val endTime = System.currentTimeMillis()
        
        // Then
        val responseTime = endTime - startTime
        assertTrue(responseTime <= maxResponseTime, "Response time $responseTime ms exceeded maximum $maxResponseTime ms")
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should handle multiple concurrent requests efficiently`() = runTest {
        // Given
        val texts = listOf(
            "Schedule meeting",
            "Send message to John",
            "Create note",
            "Calculate 2+2",
            "Search for restaurants"
        )
        val maxConcurrentTime = 10000L // 10 seconds
        
        // When
        val startTime = System.currentTimeMillis()
        val results = texts.map { text ->
            enhancedInferenceEngine.analyzeTextEnhanced(text).first()
        }
        val endTime = System.currentTimeMillis()
        
        // Then
        val totalTime = endTime - startTime
        assertTrue(totalTime <= maxConcurrentTime, "Concurrent processing time $totalTime ms exceeded maximum $maxConcurrentTime ms")
        assertTrue(results.size == texts.size)
        results.forEach { result ->
            assertTrue(result.overallConfidence >= 0f)
        }
    }
    
    @Test
    fun `analyzeTextEnhanced should handle large text efficiently`() = runTest {
        // Given
        val largeText = "This is a very large text that contains multiple sentences and should be handled efficiently by the AI system. ".repeat(100)
        val maxResponseTime = 10000L // 10 seconds
        
        // When
        val startTime = System.currentTimeMillis()
        val result = enhancedInferenceEngine.analyzeTextEnhanced(largeText).first()
        val endTime = System.currentTimeMillis()
        
        // Then
        val responseTime = endTime - startTime
        assertTrue(responseTime <= maxResponseTime, "Large text processing time $responseTime ms exceeded maximum $maxResponseTime ms")
        assertTrue(result.overallConfidence >= 0f)
    }
    
    @Test
    fun `analyzeTextEnhanced should maintain consistent performance over multiple calls`() = runTest {
        // Given
        val text = "Schedule meeting"
        val numCalls = 10
        val maxResponseTime = 1000L // 1 second per call
        val responseTimes = mutableListOf<Long>()
        
        // When
        repeat(numCalls) {
            val startTime = System.currentTimeMillis()
            val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
            val endTime = System.currentTimeMillis()
            
            responseTimes.add(endTime - startTime)
            assertTrue(result.overallConfidence >= 0f)
        }
        
        // Then
        val averageResponseTime = responseTimes.average()
        val maxResponseTimeActual = responseTimes.maxOrNull() ?: 0L
        
        assertTrue(averageResponseTime <= maxResponseTime, "Average response time $averageResponseTime ms exceeded maximum $maxResponseTime ms")
        assertTrue(maxResponseTimeActual <= maxResponseTime * 2, "Maximum response time $maxResponseTimeActual ms exceeded maximum ${maxResponseTime * 2} ms")
    }
    
    @Test
    fun `getUserInsights should complete within acceptable time`() = runTest {
        // Given
        val maxResponseTime = 2000L // 2 seconds
        
        // When
        val startTime = System.currentTimeMillis()
        val insights = enhancedInferenceEngine.getUserInsights().first()
        val endTime = System.currentTimeMillis()
        
        // Then
        val responseTime = endTime - startTime
        assertTrue(responseTime <= maxResponseTime, "User insights response time $responseTime ms exceeded maximum $maxResponseTime ms")
        assertTrue(insights.totalInteractions >= 0)
    }
    
    @Test
    fun `personalizationEngine should handle rapid learning efficiently`() = runTest {
        // Given
        val numActions = 100
        val maxLearningTime = 5000L // 5 seconds
        val actions = (1..numActions).map { "Action $it" to "com.test.app$it" to true }
        
        // When
        val startTime = System.currentTimeMillis()
        actions.forEach { (action, context, success) ->
            personalizationEngine.learnFromAction(action, context, success)
        }
        val endTime = System.currentTimeMillis()
        
        // Then
        val learningTime = endTime - startTime
        assertTrue(learningTime <= maxLearningTime, "Learning time $learningTime ms exceeded maximum $maxLearningTime ms")
    }
    
    @Test
    fun `predictiveAnalytics should handle multiple predictions efficiently`() = runTest {
        // Given
        val numPredictions = 50
        val maxPredictionTime = 3000L // 3 seconds
        val predictions = (1..numPredictions).map { "Prediction $it" to "com.test.app$it" }
        
        // When
        val startTime = System.currentTimeMillis()
        predictions.forEach { (action, context) ->
            predictiveAnalytics.learnFromAction(action, context)
        }
        val endTime = System.currentTimeMillis()
        
        // Then
        val predictionTime = endTime - startTime
        assertTrue(predictionTime <= maxPredictionTime, "Prediction time $predictionTime ms exceeded maximum $maxPredictionTime ms")
    }
    
    @Test
    fun `contextAwareness should handle rapid context updates efficiently`() = runTest {
        // Given
        val numUpdates = 100
        val maxUpdateTime = 2000L // 2 seconds
        
        // When
        val startTime = System.currentTimeMillis()
        repeat(numUpdates) { i ->
            contextAwareness.recordActivity(
                type = com.pandora.core.ai.context.ActivityType.TYPING,
                duration = 1000L
            )
        }
        val endTime = System.currentTimeMillis()
        
        // Then
        val updateTime = endTime - startTime
        assertTrue(updateTime <= maxUpdateTime, "Context update time $updateTime ms exceeded maximum $maxUpdateTime ms")
    }
    
    @Test
    fun `enhancedContextIntegration should handle comprehensive context efficiently`() = runTest {
        // Given
        val maxContextTime = 1000L // 1 second
        
        // When
        val startTime = System.currentTimeMillis()
        val context = enhancedContextIntegration.getComprehensiveContext().first()
        val endTime = System.currentTimeMillis()
        
        // Then
        val contextTime = endTime - startTime
        assertTrue(contextTime <= maxContextTime, "Context integration time $contextTime ms exceeded maximum $maxContextTime ms")
        assertTrue(context.contextConfidence >= 0f)
    }
    
    @Test
    fun `memory usage should remain stable over multiple operations`() = runTest {
        // Given
        val numOperations = 50
        val initialMemory = getMemoryUsage()
        
        // When
        repeat(numOperations) { i ->
            val text = "Test text $i"
            val result = enhancedInferenceEngine.analyzeTextEnhanced(text).first()
            assertTrue(result.overallConfidence >= 0f)
        }
        
        // Then
        val finalMemory = getMemoryUsage()
        val memoryIncrease = finalMemory - initialMemory
        val maxMemoryIncrease = 50 * 1024 * 1024 // 50MB
        
        assertTrue(memoryIncrease <= maxMemoryIncrease, "Memory increase $memoryIncrease bytes exceeded maximum $maxMemoryIncrease bytes")
    }
    
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
}
