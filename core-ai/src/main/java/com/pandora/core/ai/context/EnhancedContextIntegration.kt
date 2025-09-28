package com.pandora.core.ai.context

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Context Integration
 * Integrates all context awareness systems for comprehensive understanding
 */
@Singleton
class EnhancedContextIntegration @Inject constructor(
    private val context: Context,
    private val timeContextEnhancer: TimeContextEnhancer,
    private val locationAwareness: LocationAwareness,
    private val userActivityAnalyzer: UserActivityAnalyzer,
    private val appUsageIntelligence: AppUsageIntelligence
) {
    
    companion object {
        private const val TAG = "EnhancedContextIntegration"
    }
    
    /**
     * Get comprehensive context
     */
    fun getComprehensiveContext(): Flow<ComprehensiveContext> = flow {
        try {
            // Combine all context sources
            val timeContextFlow = timeContextEnhancer.getEnhancedTimeContext()
            val locationContextFlow = locationAwareness.getEnhancedLocationContext()
            val activityAnalysisFlow = userActivityAnalyzer.getUserActivityAnalysis()
            val appUsageFlow = appUsageIntelligence.getAppUsageIntelligence()
            
            // Wait for all contexts to be available
            val timeContext = timeContextFlow.first()
            val locationContext = locationContextFlow.first()
            val activityAnalysis = activityAnalysisFlow.first()
            val appUsage = appUsageFlow.first()
            
            // Create comprehensive context
            val comprehensiveContext = ComprehensiveContext(
                timestamp = System.currentTimeMillis(),
                timeContext = timeContext,
                locationContext = locationContext,
                activityAnalysis = activityAnalysis,
                appUsage = appUsage,
                contextConfidence = calculateContextConfidence(timeContext, locationContext, activityAnalysis, appUsage),
                contextInsights = generateContextInsights(timeContext, locationContext, activityAnalysis, appUsage),
                contextRecommendations = generateContextRecommendations(timeContext, locationContext, activityAnalysis, appUsage),
                contextPredictions = generateContextPredictions(timeContext, locationContext, activityAnalysis, appUsage)
            )
            
            emit(comprehensiveContext)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting comprehensive context", e)
            emit(ComprehensiveContext.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get real-time context updates
     */
    fun getRealTimeContextUpdates(): Flow<ComprehensiveContext> = flow {
        try {
            // Combine all context sources for real-time updates
            combine(
                timeContextEnhancer.getEnhancedTimeContext(),
                locationAwareness.getEnhancedLocationContext(),
                userActivityAnalyzer.getUserActivityAnalysis(),
                appUsageIntelligence.getAppUsageIntelligence()
            ) { timeContext, locationContext, activityAnalysis, appUsage ->
                
                ComprehensiveContext(
                    timestamp = System.currentTimeMillis(),
                    timeContext = timeContext,
                    locationContext = locationContext,
                    activityAnalysis = activityAnalysis,
                    appUsage = appUsage,
                    contextConfidence = calculateContextConfidence(timeContext, locationContext, activityAnalysis, appUsage),
                    contextInsights = generateContextInsights(timeContext, locationContext, activityAnalysis, appUsage),
                    contextRecommendations = generateContextRecommendations(timeContext, locationContext, activityAnalysis, appUsage),
                    contextPredictions = generateContextPredictions(timeContext, locationContext, activityAnalysis, appUsage)
                )
            }.collect { comprehensiveContext ->
                emit(comprehensiveContext)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting real-time context updates", e)
            emit(ComprehensiveContext.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Calculate context confidence
     */
    private fun calculateContextConfidence(
        timeContext: EnhancedTimeContext,
        locationContext: EnhancedLocationContext,
        activityAnalysis: UserActivityAnalysis,
        appUsage: AppUsageIntelligenceResult
    ): Float {
        var confidence = 0.5f
        
        // Time context confidence (always available)
        confidence += 0.2f
        
        // Location context confidence
        if (locationContext.currentLocation != null) {
            confidence += 0.2f
        }
        
        // Activity analysis confidence
        if (activityAnalysis.activityHistory.isNotEmpty()) {
            confidence += 0.1f
        }
        
        // App usage confidence
        if (appUsage.currentApp != null) {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
    
    /**
     * Generate context insights
     */
    private fun generateContextInsights(
        timeContext: EnhancedTimeContext,
        locationContext: EnhancedLocationContext,
        activityAnalysis: UserActivityAnalysis,
        appUsage: AppUsageIntelligenceResult
    ): List<ContextInsight> {
        val insights = mutableListOf<ContextInsight>()
        
        // Time-based insights
        if (timeContext.isWorkingTime) {
            insights.add(ContextInsight(
                type = "work_context",
                description = "You're in work mode. Focus on productive tasks.",
                confidence = 0.9f,
                metadata = mapOf(
                    "title" to "Work Mode Active",
                    "recommendations" to listOf(
                        "Use productivity apps",
                        "Minimize distractions",
                        "Take regular breaks"
                    )
                )
            ))
        }
        
        // Location-based insights
        if (locationContext.isAtHome) {
            insights.add(ContextInsight(
                type = "home_context",
                description = "You're at home. Time to relax and unwind.",
                confidence = 0.8f,
                metadata = mapOf(
                    "title" to "At Home",
                    "recommendations" to listOf(
                        "Use entertainment apps",
                        "Connect with family",
                        "Plan for tomorrow"
                    )
                )
            ))
        }
        
        // Activity-based insights
        val productivityScore = activityAnalysis.productivityMetrics.productivityScore
        if (productivityScore > 0.7f) {
            insights.add(ContextInsight(
                type = "high_productivity",
                description = "You're being very productive today!",
                confidence = 0.8f,
                metadata = mapOf(
                    "title" to "High Productivity",
                    "recommendations" to listOf(
                        "Keep up the good work",
                        "Take a well-deserved break",
                        "Share your progress"
                    )
                )
            ))
        }
        
        // App usage insights
        val productiveApps = appUsage.appUsageHistory.count { 
            isProductiveApp(it.packageName) 
        }
        val totalApps = appUsage.appUsageHistory.size
        if (totalApps > 0) {
            val productivityRatio = productiveApps.toFloat() / totalApps
            if (productivityRatio > 0.6f) {
                insights.add(ContextInsight(
                    type = "productive_apps",
                    description = "You're using productive apps ${(productivityRatio * 100).toInt()}% of the time.",
                    confidence = 0.7f
                ))
            }
        }
        
        return insights
    }
    
    /**
     * Generate context recommendations
     */
    private fun generateContextRecommendations(
        timeContext: EnhancedTimeContext,
        locationContext: EnhancedLocationContext,
        activityAnalysis: UserActivityAnalysis,
        appUsage: AppUsageIntelligenceResult
    ): List<ContextRecommendation> {
        val recommendations = mutableListOf<ContextRecommendation>()
        
        // Time-based recommendations
        timeContext.timeBasedSuggestions.forEach { suggestion ->
            recommendations.add(ContextRecommendation(
                type = "time_recommendation",
                title = suggestion.suggestion,
                description = suggestion.suggestion,
                priority = getRecommendationPriority(suggestion.category),
                metadata = mapOf(
                    "category" to suggestion.category,
                    "confidence" to suggestion.confidence,
                    "context" to "time"
                )
            ))
        }
        
        // Location-based recommendations
        locationContext.locationBasedSuggestions.forEach { suggestion ->
            recommendations.add(ContextRecommendation(
                type = "location_recommendation",
                title = suggestion.suggestion,
                description = suggestion.suggestion,
                priority = getRecommendationPriority(suggestion.category),
                metadata = mapOf(
                    "category" to suggestion.category,
                    "confidence" to suggestion.confidence,
                    "context" to "location"
                )
            ))
        }
        
        // Activity-based recommendations
        activityAnalysis.activityRecommendations.forEach { recommendation ->
            recommendations.add(ContextRecommendation(
                type = "activity_recommendation",
                title = recommendation.recommendation,
                description = recommendation.recommendation,
                priority = getRecommendationPriority(recommendation.category),
                metadata = mapOf(
                    "category" to recommendation.category,
                    "confidence" to recommendation.confidence,
                    "context" to "activity"
                )
            ))
        }
        
        // App usage recommendations
        appUsage.appRecommendations.forEach { recommendation ->
            recommendations.add(ContextRecommendation(
                type = "app_recommendation",
                title = recommendation.recommendation,
                description = recommendation.recommendation,
                priority = getRecommendationPriority(recommendation.category),
                metadata = mapOf(
                    "category" to recommendation.category,
                    "confidence" to recommendation.confidence,
                    "suggested_apps" to recommendation.suggestedApps
                )
            ))
        }
        
        return recommendations.sortedByDescending { it.priority }
    }
    
    /**
     * Generate context predictions
     */
    private fun generateContextPredictions(
        timeContext: EnhancedTimeContext,
        locationContext: EnhancedLocationContext,
        activityAnalysis: UserActivityAnalysis,
        appUsage: AppUsageIntelligenceResult
    ): List<ContextPrediction> {
        val predictions = mutableListOf<ContextPrediction>()
        
        // Time-based predictions
        if (timeContext.isWorkingTime) {
            predictions.add(ContextPrediction(
                type = "work_continuation",
                prediction = "You'll likely continue working for the next hour",
                confidence = 0.8f,
                timeHorizon = 60 * 60 * 1000L // 1 hour
            ))
        }
        
        // Location-based predictions
        if (locationContext.isAtHome) {
            predictions.add(ContextPrediction(
                type = "home_activities",
                prediction = "You'll likely engage in home activities",
                confidence = 0.7f,
                timeHorizon = 2 * 60 * 60 * 1000L // 2 hours
            ))
        }
        
        // Activity-based predictions
        val currentActivity = activityAnalysis.currentActivity
        when (currentActivity.type) {
            ActivityType.TYPING -> {
                predictions.add(ContextPrediction(
                    type = "typing_continuation",
                    prediction = "You'll likely continue typing",
                    confidence = 0.6f,
                    timeHorizon = 30 * 60 * 1000L // 30 minutes
                ))
            }
            ActivityType.SCROLLING -> {
                predictions.add(ContextPrediction(
                    type = "scrolling_continuation",
                    prediction = "You'll likely continue scrolling",
                    confidence = 0.5f,
                    timeHorizon = 15 * 60 * 1000L // 15 minutes
                ))
            }
            else -> {}
        }
        
        // App usage predictions
        appUsage.appPredictions.forEach { prediction ->
            predictions.add(ContextPrediction(
                type = "app_usage",
                prediction = "You'll likely use ${prediction.app} (${(prediction.probability * 100).toInt()}% chance)",
                confidence = prediction.confidence,
                timeHorizon = 30 * 60 * 1000L // 30 minutes
            ))
        }
        
        return predictions
    }
    
    /**
     * Get recommendation priority
     */
    private fun getRecommendationPriority(category: String): Int {
        return when (category) {
            "productivity" -> 10
            "focus" -> 9
            "health" -> 8
            "efficiency" -> 7
            "organization" -> 6
            "entertainment" -> 5
            "break" -> 4
            "wind_down" -> 3
            "morning_routine" -> 2
            "evening_wind_down" -> 1
            else -> 0
        }
    }
    
    /**
     * Get context summary
     */
    fun getContextSummary(): Flow<ContextSummary> = flow {
        try {
            val comprehensiveContext = getComprehensiveContext().first()
            
            val summary = ContextSummary(
                timestamp = comprehensiveContext.timestamp,
                contextConfidence = comprehensiveContext.contextConfidence,
                currentContext = getCurrentContextDescription(comprehensiveContext),
                keyInsights = comprehensiveContext.contextInsights.take(3),
                topRecommendations = comprehensiveContext.contextRecommendations.take(3),
                contextPredictions = comprehensiveContext.contextPredictions.take(2),
                contextHealth = calculateContextHealth(comprehensiveContext)
            )
            
            emit(summary)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting context summary", e)
            emit(ContextSummary.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current context description
     */
    private fun getCurrentContextDescription(context: ComprehensiveContext): String {
        val timeContext = context.timeContext
        val locationContext = context.locationContext
        val activityAnalysis = context.activityAnalysis
        val appUsage = context.appUsage
        
        val descriptions = mutableListOf<String>()
        
        // Time description
        when {
            timeContext.isWorkingTime -> descriptions.add("Working hours")
            timeContext.isBreakTime -> descriptions.add("Break time")
            timeContext.isRushHour -> descriptions.add("Rush hour")
            else -> descriptions.add("Regular time")
        }
        
        // Location description
        when {
            locationContext.isAtHome -> descriptions.add("at home")
            locationContext.isAtWork -> descriptions.add("at work")
            locationContext.isTraveling -> descriptions.add("traveling")
            else -> descriptions.add("at unknown location")
        }
        
        // Activity description
        val currentActivity = activityAnalysis.currentActivity
        when (currentActivity.type) {
            ActivityType.TYPING -> descriptions.add("typing")
            ActivityType.SCROLLING -> descriptions.add("scrolling")
            ActivityType.APP_SWITCH -> descriptions.add("switching apps")
            else -> descriptions.add("idle")
        }
        
        // App description
        appUsage.currentApp?.let { app ->
            val category = appUsage.appContext.appCategory
            descriptions.add("using ${category.name.lowercase()} app")
        }
        
        return descriptions.joinToString(", ")
    }
    
    /**
     * Calculate context health
     */
    private fun calculateContextHealth(context: ComprehensiveContext): ContextHealth {
        val timeContext = context.timeContext
        val locationContext = context.locationContext
        val activityAnalysis = context.activityAnalysis
        val appUsage = context.appUsage
        
        var healthScore = 0.5f
        
        // Time health
        if (timeContext.isWorkingTime) healthScore += 0.1f
        if (timeContext.isBreakTime) healthScore += 0.1f
        
        // Location health
        if (locationContext.isAtHome || locationContext.isAtWork) healthScore += 0.1f
        
        // Activity health
        val productivityScore = activityAnalysis.productivityMetrics.productivityScore
        healthScore += productivityScore * 0.2f
        
        // App usage health
        val productiveApps = appUsage.appUsageHistory.count { 
            isProductiveApp(it.packageName) 
        }
        val totalApps = appUsage.appUsageHistory.size
        if (totalApps > 0) {
            val productivityRatio = productiveApps.toFloat() / totalApps
            healthScore += productivityRatio * 0.1f
        }
        
        return ContextHealth(
            overallScore = healthScore.coerceIn(0f, 1f),
            timeHealth = if (timeContext.isWorkingTime) 0.8f else 0.5f,
            locationHealth = if (locationContext.currentLocation != null) 0.8f else 0.3f,
            activityHealth = productivityScore,
            appUsageHealth = if (totalApps > 0) productiveApps.toFloat() / totalApps else 0.5f
        )
    }
    
    /**
     * Check if app is productive
     */
    private fun isProductiveApp(packageName: String): Boolean {
        return when {
            packageName.contains("calendar") || packageName.contains("google") -> true
            packageName.contains("notes") || packageName.contains("keep") -> true
            packageName.contains("browser") || packageName.contains("chrome") -> true
            packageName.contains("docs") || packageName.contains("office") -> true
            else -> false
        }
    }
}





/**
 * Context summary
 */
data class ContextSummary(
    val timestamp: Long,
    val contextConfidence: Float,
    val currentContext: String,
    val keyInsights: List<ContextInsight>,
    val topRecommendations: List<ContextRecommendation>,
    val contextPredictions: List<ContextPrediction>,
    val contextHealth: ContextHealth
) {
    companion object {
        fun createEmpty() = ContextSummary(
            timestamp = System.currentTimeMillis(),
            contextConfidence = 0f,
            currentContext = "Unknown",
            keyInsights = emptyList(),
            topRecommendations = emptyList(),
            contextPredictions = emptyList(),
            contextHealth = ContextHealth.createEmpty()
        )
    }
}

/**
 * Context health
 */
data class ContextHealth(
    val overallScore: Float,
    val timeHealth: Float,
    val locationHealth: Float,
    val activityHealth: Float,
    val appUsageHealth: Float
) {
    companion object {
        fun createEmpty() = ContextHealth(
            overallScore = 0.5f,
            timeHealth = 0.5f,
            locationHealth = 0.5f,
            activityHealth = 0.5f,
            appUsageHealth = 0.5f
        )
    }
}
