package com.pandora.core.ai.context

import com.pandora.core.ai.context.AppUsageIntelligenceResult

/**
 * Comprehensive context data class that combines all context information
 * from various context enhancers
 */
data class ComprehensiveContext(
    val timestamp: Long,
    val timeContext: EnhancedTimeContext,
    val locationContext: EnhancedLocationContext,
    val activityAnalysis: UserActivityAnalysis,
    val appUsage: AppUsageIntelligenceResult,
    val contextConfidence: Float,
    val contextInsights: List<ContextInsight>,
    val contextRecommendations: List<ContextRecommendation>,
    val contextPredictions: List<ContextPrediction>
) {
    companion object {
        fun createEmpty() = ComprehensiveContext(
            timestamp = System.currentTimeMillis(),
            timeContext = EnhancedTimeContext.createEmpty(),
            locationContext = EnhancedLocationContext.createEmpty(),
            activityAnalysis = UserActivityAnalysis.createEmpty(),
            appUsage = AppUsageIntelligenceResult.createEmpty(),
            contextConfidence = 0f,
            contextInsights = emptyList(),
            contextRecommendations = emptyList(),
            contextPredictions = emptyList()
        )
    }
}

/**
 * Context insight data class
 */
data class ContextInsight(
    val type: String,
    val description: String,
    val confidence: Float,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Context recommendation data class
 */
data class ContextRecommendation(
    val type: String,
    val title: String,
    val description: String,
    val priority: Int,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Context prediction data class
 */
data class ContextPrediction(
    val type: String,
    val prediction: String,
    val confidence: Float,
    val timeHorizon: Long, // milliseconds
    val metadata: Map<String, Any> = emptyMap()
)
