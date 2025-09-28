package com.pandora.core.ai

import android.content.Context
import com.pandora.core.ai.ml.AdvancedModelManager
import com.pandora.core.ai.ml.TextContext
import com.pandora.core.ai.ml.AdvancedAnalysisResult
import com.pandora.core.ai.personalization.PersonalizationEngine
import com.pandora.core.ai.prediction.PredictiveAnalytics
import com.pandora.core.ai.context.ContextAwareness
import com.pandora.core.ai.context.ContextSnapshot
import com.pandora.core.ai.context.EnhancedContextIntegration
import com.pandora.core.ai.context.ComprehensiveContext
import com.pandora.core.ai.context.AppContext
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.MemoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Inference Engine for Neural Keyboard
 * Integrates all advanced AI features for intelligent action inference
 */
@Singleton
class EnhancedInferenceEngine @Inject constructor(
    private val context: Context,
    private val advancedModelManager: AdvancedModelManager,
    private val personalizationEngine: PersonalizationEngine,
    private val predictiveAnalytics: PredictiveAnalytics,
    private val contextAwareness: ContextAwareness,
    private val enhancedContextIntegration: EnhancedContextIntegration,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "EnhancedInferenceEngine"
        private const val MIN_CONFIDENCE_THRESHOLD = 0.6f
        private const val MAX_SUGGESTIONS = 5
    }
    
    /**
     * Initialize the enhanced inference engine
     */
    suspend fun initialize() {
        try {
            // Initialize all AI components
            advancedModelManager.initializeModels()
            
            android.util.Log.d(TAG, "Enhanced Inference Engine initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error initializing Enhanced Inference Engine", e)
        }
    }
    
    /**
     * Analyze text with enhanced AI capabilities
     */
    fun analyzeTextEnhanced(text: String): Flow<EnhancedInferenceResult> = flow {
        try {
            // Get comprehensive context
            val comprehensiveContextFlow = enhancedContextIntegration.getComprehensiveContext()
            val comprehensiveContext = comprehensiveContextFlow.first()
            
            // Create text context with enhanced information
            val textContext = TextContext(
                timestamp = System.currentTimeMillis(),
                location = "${comprehensiveContext.locationContext.currentLocation?.latitude ?: 0.0},${comprehensiveContext.locationContext.currentLocation?.longitude ?: 0.0}",
                appPackage = comprehensiveContext.appUsage.currentApp,
                userActivity = comprehensiveContext.activityAnalysis.currentActivity.type.name,
                recentTexts = getRecentTexts()
            )
            
            // Run advanced analysis
            val analysisResultFlow = advancedModelManager.analyzeTextAdvanced(text, textContext)
            val analysisResult = analysisResultFlow.first()
            
            // Get personalized suggestions
            val personalizedSuggestionsFlow = personalizationEngine.getPersonalizedSuggestions(
                text = text,
                context = textContext.appPackage ?: "unknown",
                baseSuggestions = getBaseSuggestions(analysisResult)
            )
            val personalizedSuggestions = personalizedSuggestionsFlow.first()
            
            // Get predictive suggestions
            val predictiveSuggestionsFlow = predictiveAnalytics.predictNextActions(
                currentText = text,
                currentContext = textContext.appPackage ?: "unknown",
                timeOfDay = comprehensiveContext.timeContext.timeOfDay.ordinal
            )
            val predictiveSuggestions = predictiveSuggestionsFlow.first()
            
            // Get proactive suggestions
            val proactiveSuggestionsFlow = predictiveAnalytics.getProactiveSuggestions(
                currentTime = System.currentTimeMillis(),
                currentContext = textContext.appPackage ?: "unknown"
            )
            val proactiveSuggestions = proactiveSuggestionsFlow.first()
            
            // Combine all results with enhanced context
            val enhancedResult = EnhancedInferenceResult(
                text = text,
                analysisResult = analysisResult,
                personalizedSuggestions = personalizedSuggestions,
                predictiveSuggestions = predictiveSuggestions,
                proactiveSuggestions = proactiveSuggestions,
                contextSnapshot = comprehensiveContext.toContextSnapshot(),
                overallConfidence = calculateOverallConfidence(analysisResult, personalizedSuggestions),
                timestamp = System.currentTimeMillis(),
                comprehensiveContext = comprehensiveContext
            )
            
            // Learn from this interaction
            learnFromInteraction(text, enhancedResult)
            
            emit(enhancedResult)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error in enhanced text analysis", e)
            emit(EnhancedInferenceResult.createEmpty(text))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get base suggestions from analysis result
     */
    private suspend fun getBaseSuggestions(analysisResult: AdvancedAnalysisResult): List<String> {
        val suggestions = mutableListOf<String>()
        
        // Add intent-based suggestions
        when (analysisResult.intent.intent) {
            com.pandora.core.ai.ml.IntentType.CALENDAR -> suggestions.add("Add to Calendar")
            com.pandora.core.ai.ml.IntentType.REMIND -> suggestions.add("Set Reminder")
            com.pandora.core.ai.ml.IntentType.SEND -> suggestions.add("Send Message")
            com.pandora.core.ai.ml.IntentType.NOTE -> suggestions.add("Create Note")
            com.pandora.core.ai.ml.IntentType.MATH -> suggestions.add("Calculate")
            com.pandora.core.ai.ml.IntentType.SEARCH -> suggestions.add("Search")
            else -> suggestions.add("Unknown Action")
        }
        
        // Add entity-based suggestions
        analysisResult.entities.entities.forEach { entity ->
            when (entity.type) {
                com.pandora.core.ai.ml.EntityType.PERSON -> suggestions.add("Contact: ${entity.text}")
                com.pandora.core.ai.ml.EntityType.LOCATION -> suggestions.add("Location: ${entity.text}")
                com.pandora.core.ai.ml.EntityType.TIME -> suggestions.add("Time: ${entity.text}")
                com.pandora.core.ai.ml.EntityType.DATE -> suggestions.add("Date: ${entity.text}")
                else -> {}
            }
        }
        
        return suggestions
    }
    
    /**
     * Get recent texts for context
     */
    private suspend fun getRecentTexts(): List<String> {
        return try {
            val recentMemories = cacDao.getMemoriesBySource("keyboard", 10)
            recentMemories.map { it.content }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Calculate overall confidence
     */
    private suspend fun calculateOverallConfidence(
        analysisResult: AdvancedAnalysisResult,
        personalizedSuggestions: List<com.pandora.core.ai.personalization.PersonalizedSuggestion>
    ): Float {
        val analysisConfidence = analysisResult.confidence
        val personalizationConfidence = if (personalizedSuggestions.isNotEmpty()) {
            personalizedSuggestions.map { it.score }.average().toFloat()
        } else {
            0.5f
        }
        
        return (analysisConfidence + personalizationConfidence) / 2f
    }
    
    /**
     * Learn from interaction
     */
    private suspend fun learnFromInteraction(text: String, result: EnhancedInferenceResult) {
        try {
            // Learn from personalization
            result.personalizedSuggestions.forEach { suggestion ->
                personalizationEngine.learnFromAction(
                    action = suggestion.suggestion,
                    context = result.contextSnapshot.appContext.currentApp ?: "unknown",
                    success = suggestion.score > 0.7f
                )
            }
            
            // Learn from predictions
            result.predictiveSuggestions.forEach { prediction ->
                predictiveAnalytics.learnFromAction(
                    action = prediction.action,
                    context = result.contextSnapshot.appContext.currentApp ?: "unknown"
                )
            }
            
            // Record activity with enhanced context
            contextAwareness.recordActivity(
                type = com.pandora.core.ai.context.ActivityType.TYPING,
                duration = 1000L
            )
            
            // Record app usage
            result.comprehensiveContext?.appUsage?.currentApp?.let { app ->
                // App usage recording would be handled by the context integration
                // This is a placeholder for future implementation
            }
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error learning from interaction", e)
        }
    }
    
    /**
     * Get user insights
     */
    fun getUserInsights(): Flow<UserInsights> = flow {
        try {
            val personalizationInsightsFlow = personalizationEngine.getUserInsights()
            val personalizationInsights = personalizationInsightsFlow.first()
            
            val predictionAccuracyFlow = predictiveAnalytics.getPredictionAccuracy()
            val predictionAccuracy = predictionAccuracyFlow.first()
            
            val contextSnapshotFlow = contextAwareness.getCurrentContext()
            val contextSnapshot = contextSnapshotFlow.first()
            
            val insights = UserInsights(
                personalizationInsights = personalizationInsights,
                predictionAccuracy = predictionAccuracy,
                contextSnapshot = contextSnapshot,
                totalInteractions = getTotalInteractions(),
                learningProgress = calculateLearningProgress()
            )
            
            emit(insights)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting user insights", e)
            emit(UserInsights.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get total interactions
     */
    private suspend fun getTotalInteractions(): Int {
        return try {
            val memories = cacDao.getMemoriesBySource("keyboard", 1000)
            memories.size
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Calculate learning progress
     */
    private suspend fun calculateLearningProgress(): Float {
        return try {
            val personalizationInsightsFlow = personalizationEngine.getUserInsights()
            val personalizationInsights = personalizationInsightsFlow.first()
            
            val predictionAccuracyFlow = predictiveAnalytics.getPredictionAccuracy()
            val predictionAccuracy = predictionAccuracyFlow.first()
            
            val personalizationProgress = personalizationInsights.learningProgress
            val predictionProgress = predictionAccuracy
            
            (personalizationProgress + predictionProgress) / 2f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Reset all learning data
     */
    fun resetLearning() {
        try {
            personalizationEngine.resetPersonalization()
            android.util.Log.d(TAG, "Learning data reset successfully")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error resetting learning data", e)
        }
    }
}

/**
 * Enhanced inference result
 */
data class EnhancedInferenceResult(
    val text: String,
    val analysisResult: AdvancedAnalysisResult,
    val personalizedSuggestions: List<com.pandora.core.ai.personalization.PersonalizedSuggestion>,
    val predictiveSuggestions: List<com.pandora.core.ai.prediction.ActionPrediction>,
    val proactiveSuggestions: List<com.pandora.core.ai.prediction.ProactiveSuggestion>,
    val contextSnapshot: ContextSnapshot,
    val overallConfidence: Float,
    val timestamp: Long,
    val comprehensiveContext: ComprehensiveContext? = null
) {
    companion object {
        fun createEmpty(text: String) = EnhancedInferenceResult(
            text = text,
            analysisResult = AdvancedAnalysisResult.createEmpty(),
            personalizedSuggestions = emptyList(),
            predictiveSuggestions = emptyList(),
            proactiveSuggestions = emptyList(),
            contextSnapshot = ContextSnapshot.createEmpty(),
            overallConfidence = 0f,
            timestamp = System.currentTimeMillis(),
            comprehensiveContext = null
        )
    }
}

/**
 * User insights
 */
data class UserInsights(
    val personalizationInsights: com.pandora.core.ai.personalization.UserInsights,
    val predictionAccuracy: Float,
    val contextSnapshot: ContextSnapshot,
    val totalInteractions: Int,
    val learningProgress: Float
) {
    companion object {
        fun createEmpty() = UserInsights(
            personalizationInsights = com.pandora.core.ai.personalization.UserInsights.createEmpty(),
            predictionAccuracy = 0f,
            contextSnapshot = ContextSnapshot.createEmpty(),
            totalInteractions = 0,
            learningProgress = 0f
        )
    }
}

/**
 * Extension function to convert ComprehensiveContext to ContextSnapshot
 */
private fun ComprehensiveContext.toContextSnapshot(): ContextSnapshot {
    return ContextSnapshot(
        timestamp = this.timestamp,
        timeContext = com.pandora.core.ai.context.TimeContext(
            hour = this.timeContext.timestamp.let { 
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it
                calendar.get(java.util.Calendar.HOUR_OF_DAY)
            },
            dayOfWeek = this.timeContext.timestamp.let { 
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = it
                calendar.get(java.util.Calendar.DAY_OF_WEEK)
            },
            isWeekend = this.timeContext.dayType == com.pandora.core.ai.context.DayType.WEEKEND,
            timeOfDay = this.timeContext.timeOfDay,
            season = this.timeContext.season,
            isHoliday = this.timeContext.isHoliday
        ),
        locationContext = this.locationContext.currentLocation?.let { location ->
            com.pandora.core.ai.context.LocationContext(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                locationType = this.locationContext.locationType,
                isMoving = location.hasSpeed() && location.speed > 0,
                speed = location.speed
            )
        } ?: com.pandora.core.ai.context.LocationContext.createUnknown(),
        appContext = this.appUsage.appContext,
        userContext = com.pandora.core.ai.context.UserContext.createEmpty(),
        deviceContext = com.pandora.core.ai.context.DeviceContext.createEmpty(),
        confidence = this.contextConfidence
    )
}
