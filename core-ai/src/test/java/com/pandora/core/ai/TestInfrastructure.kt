package com.pandora.core.ai

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Test Infrastructure for Core AI Module
 * Provides common testing utilities and setup
 */
@ExperimentalCoroutinesApi
abstract class TestInfrastructure {
    
    @RegisterExtension
    val mockkExtension = MockKAnnotations.init(this)
    
    protected val testDispatcher: TestDispatcher = StandardTestDispatcher()
    
    @BeforeEach
    open fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
    }
    
    @AfterEach
    fun tearDown() {
        clearAllMocks()
        unmockkAll()
        Dispatchers.resetMain()
    }
}

/**
 * Test data factories for AI components
 */
object TestDataFactory {
    
    fun createTextContext(
        timestamp: Long = System.currentTimeMillis(),
        location: String = "0.0,0.0",
        appPackage: String? = "com.test.app",
        userActivity: String = "TYPING",
        recentTexts: List<String> = emptyList()
    ) = com.pandora.core.ai.ml.TextContext(
        timestamp = timestamp,
        location = location,
        appPackage = appPackage,
        userActivity = userActivity,
        recentTexts = recentTexts
    )
    
    fun createAdvancedAnalysisResult(
        intent: com.pandora.core.ai.ml.IntentType = com.pandora.core.ai.ml.IntentType.UNKNOWN,
        confidence: Float = 0.8f,
        entities: List<com.pandora.core.ai.ml.Entity> = emptyList()
    ) = com.pandora.core.ai.ml.AdvancedAnalysisResult(
        intent = com.pandora.core.ai.ml.IntentResult(intent, confidence, emptyList()),
        entities = com.pandora.core.ai.ml.EntityResult(entities, confidence),
        sentiment = com.pandora.core.ai.ml.SentimentResult(com.pandora.core.ai.ml.SentimentType.NEUTRAL, confidence, emptyMap()),
        context = com.pandora.core.ai.ml.ContextResult(com.pandora.core.ai.ml.TimeContext.UNKNOWN, com.pandora.core.ai.ml.LocationContext.UNKNOWN, com.pandora.core.ai.ml.AppContext.UNKNOWN, com.pandora.core.ai.ml.UserContext.UNKNOWN, confidence),
        confidence = confidence
    )
    
    fun createPersonalizedSuggestion(
        suggestion: String = "Test suggestion",
        score: Float = 0.8f
    ) = com.pandora.core.ai.personalization.PersonalizedSuggestion(
        suggestion = suggestion,
        score = score,
        personalizationFactor = 0.5f,
        frequencyFactor = 0.5f,
        contextFactor = 0.5f
    )
    
    fun createActionPrediction(
        action: String = "Test action",
        confidence: Float = 0.8f
    ) = com.pandora.core.ai.prediction.ActionPrediction(
        action = action,
        confidence = confidence,
        reason = "test reason",
        predictionType = com.pandora.core.ai.prediction.PredictionType.UNKNOWN
    )
    
    fun createProactiveSuggestion(
        title: String = "Test proactive suggestion",
        description: String = "description",
        action: String = "test_action",
        priority: Float = 0.8f
    ) = com.pandora.core.ai.prediction.ProactiveSuggestion(
        title = title,
        description = description,
        action = action,
        priority = priority,
        suggestionType = com.pandora.core.ai.prediction.SuggestionType.UNKNOWN
    )
    
    fun createContextSnapshot(
        timestamp: Long = System.currentTimeMillis(),
        timeContext: com.pandora.core.ai.context.TimeContext = com.pandora.core.ai.context.TimeContext.createEmpty(),
        locationContext: com.pandora.core.ai.context.LocationContext = com.pandora.core.ai.context.LocationContext.createUnknown(),
        appContext: com.pandora.core.ai.context.AppContext = com.pandora.core.ai.context.AppContext.createUnknown(),
        userContext: com.pandora.core.ai.context.UserContext = com.pandora.core.ai.context.UserContext.createEmpty(),
        deviceContext: com.pandora.core.ai.context.DeviceContext = com.pandora.core.ai.context.DeviceContext.createEmpty(),
        confidence: Float = 0.8f
    ) = com.pandora.core.ai.context.ContextSnapshot(
        timestamp = timestamp,
        timeContext = timeContext,
        locationContext = locationContext,
        appContext = appContext,
        userContext = userContext,
        deviceContext = deviceContext,
        confidence = confidence
    )
    
    fun createComprehensiveContext(
        timestamp: Long = System.currentTimeMillis(),
        timeContext: com.pandora.core.ai.context.EnhancedTimeContext = com.pandora.core.ai.context.EnhancedTimeContext.createEmpty(),
        locationContext: com.pandora.core.ai.context.EnhancedLocationContext = com.pandora.core.ai.context.EnhancedLocationContext.createEmpty(),
        activityAnalysis: com.pandora.core.ai.context.UserActivityAnalysis = com.pandora.core.ai.context.UserActivityAnalysis.createEmpty(),
        appUsage: com.pandora.core.ai.context.AppUsageIntelligenceResult = com.pandora.core.ai.context.AppUsageIntelligenceResult.createEmpty(),
        contextConfidence: Float = 0.8f,
        contextInsights: List<com.pandora.core.ai.context.ContextInsight> = emptyList(),
        contextRecommendations: List<com.pandora.core.ai.context.ContextRecommendation> = emptyList(),
        contextPredictions: List<com.pandora.core.ai.context.ContextPrediction> = emptyList()
    ) = com.pandora.core.ai.context.ComprehensiveContext(
        timestamp = timestamp,
        timeContext = timeContext,
        locationContext = locationContext,
        activityAnalysis = activityAnalysis,
        appUsage = appUsage,
        contextConfidence = contextConfidence,
        contextInsights = contextInsights,
        contextRecommendations = contextRecommendations,
        contextPredictions = contextPredictions
    )
}

/**
 * Test utilities for AI components
 */
object TestUtils {
    
    fun assertFloatEquals(expected: Float, actual: Float, delta: Float = 0.01f) {
        assert(Math.abs(expected - actual) <= delta) { 
            "Expected: $expected, Actual: $actual, Delta: $delta" 
        }
    }
    
    fun assertListEquals(expected: List<Any>, actual: List<Any>) {
        assert(expected.size == actual.size) { 
            "Expected size: ${expected.size}, Actual size: ${actual.size}" 
        }
        expected.forEachIndexed { index, expectedItem ->
            assert(expectedItem == actual[index]) { 
                "Expected[$index]: $expectedItem, Actual[$index]: ${actual[index]}" 
            }
        }
    }
    
    fun assertMapEquals(expected: Map<String, Any>, actual: Map<String, Any>) {
        assert(expected.size == actual.size) { 
            "Expected size: ${expected.size}, Actual size: ${actual.size}" 
        }
        expected.forEach { (key, expectedValue) ->
            assert(actual.containsKey(key)) { "Missing key: $key" }
            assert(expectedValue == actual[key]) { 
                "Expected[$key]: $expectedValue, Actual[$key]: ${actual[key]}" 
            }
        }
    }
}
