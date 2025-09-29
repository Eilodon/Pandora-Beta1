package com.pandora.feature.onboarding

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages onboarding analytics and tracking
 */
@Singleton
class OnboardingAnalytics @Inject constructor() {
    
    private val _analytics = MutableStateFlow<List<OnboardingAnalyticsData>>(emptyList())
    val analytics: StateFlow<List<OnboardingAnalyticsData>> = _analytics.asStateFlow()
    
    private val _currentStepAnalytics = MutableStateFlow<OnboardingAnalyticsData?>(null)
    val currentStepAnalytics: StateFlow<OnboardingAnalyticsData?> = _currentStepAnalytics.asStateFlow()
    
    private val _userInteractions = MutableStateFlow<List<UserInteraction>>(emptyList())
    val userInteractions: StateFlow<List<UserInteraction>> = _userInteractions.asStateFlow()
    
    /**
     * Start tracking a step
     */
    fun startStep(stepId: String, stepType: StepType) {
        val stepAnalytics = OnboardingAnalyticsData(
            stepId = stepId,
            stepType = stepType,
            startTime = System.currentTimeMillis(),
            wasSkipped = false,
            wasCompleted = false,
            validationPassed = null,
            userInteractions = emptyList()
        )
        
        _currentStepAnalytics.value = stepAnalytics
        Log.d("OnboardingAnalytics", "Started tracking step: $stepId")
    }
    
    /**
     * Complete a step
     */
    fun completeStep(stepId: String, validationPassed: Boolean = true) {
        val current = _currentStepAnalytics.value
        if (current?.stepId == stepId) {
            val completedAnalytics = current.copy(
                endTime = System.currentTimeMillis(),
                timeSpent = System.currentTimeMillis() - current.startTime,
                wasCompleted = true,
                validationPassed = validationPassed,
                userInteractions = _userInteractions.value
            )
            
            _analytics.value = _analytics.value + completedAnalytics
            _currentStepAnalytics.value = null
            _userInteractions.value = emptyList()
            
            Log.d("OnboardingAnalytics", "Completed step: $stepId in ${completedAnalytics.timeSpent}ms")
        }
    }
    
    /**
     * Skip a step
     */
    fun skipStep(stepId: String) {
        val current = _currentStepAnalytics.value
        if (current?.stepId == stepId) {
            val skippedAnalytics = current.copy(
                endTime = System.currentTimeMillis(),
                timeSpent = System.currentTimeMillis() - current.startTime,
                wasSkipped = true,
                wasCompleted = false,
                userInteractions = _userInteractions.value
            )
            
            _analytics.value = _analytics.value + skippedAnalytics
            _currentStepAnalytics.value = null
            _userInteractions.value = emptyList()
            
            Log.d("OnboardingAnalytics", "Skipped step: $stepId")
        }
    }
    
    /**
     * Track user interaction
     */
    fun trackInteraction(
        interactionType: String,
        elementId: String? = null,
        value: String? = null
    ) {
        val interaction = UserInteraction(
            interactionType = interactionType,
            timestamp = System.currentTimeMillis(),
            elementId = elementId,
            value = value
        )
        
        _userInteractions.value = _userInteractions.value + interaction
        Log.d("OnboardingAnalytics", "Tracked interaction: $interactionType")
    }
    
    /**
     * Get step analytics
     */
    fun getStepAnalytics(stepId: String): OnboardingAnalyticsData? {
        return _analytics.value.find { it.stepId == stepId }
    }
    
    /**
     * Get completion rate
     */
    fun getCompletionRate(): Float {
        val totalSteps = _analytics.value.size
        if (totalSteps == 0) return 0f
        
        val completedSteps = _analytics.value.count { it.wasCompleted }
        return completedSteps.toFloat() / totalSteps.toFloat()
    }
    
    /**
     * Get average time per step
     */
    fun getAverageTimePerStep(): Long {
        val completedSteps = _analytics.value.filter { it.wasCompleted && it.timeSpent != null }
        if (completedSteps.isEmpty()) return 0L
        
        val totalTime = completedSteps.sumOf { it.timeSpent!! }
        return totalTime / completedSteps.size
    }
    
    /**
     * Get drop-off points
     */
    fun getDropOffPoints(): List<String> {
        return _analytics.value
            .filter { it.wasSkipped }
            .map { it.stepId }
    }
    
    /**
     * Get most engaging steps
     */
    fun getMostEngagingSteps(limit: Int = 5): List<OnboardingAnalyticsData> {
        return _analytics.value
            .filter { it.wasCompleted }
            .sortedByDescending { it.userInteractions.size }
            .take(limit)
    }
    
    /**
     * Get validation failure rate
     */
    fun getValidationFailureRate(): Float {
        val stepsWithValidation = _analytics.value.filter { it.validationPassed != null }
        if (stepsWithValidation.isEmpty()) return 0f
        
        val failedValidations = stepsWithValidation.count { it.validationPassed == false }
        return failedValidations.toFloat() / stepsWithValidation.size.toFloat()
    }
    
    /**
     * Get total time spent
     */
    fun getTotalTimeSpent(): Long {
        return _analytics.value
            .filter { it.timeSpent != null }
            .sumOf { it.timeSpent!! }
    }
    
    /**
     * Get step type performance
     */
    fun getStepTypePerformance(): Map<StepType, StepTypePerformance> {
        return _analytics.value
            .groupBy { it.stepType }
            .mapValues { (stepType, analytics) ->
                val completed = analytics.count { it.wasCompleted }
                val skipped = analytics.count { it.wasSkipped }
                val total = analytics.size
                val avgTime = analytics
                    .filter { it.timeSpent != null }
                    .map { it.timeSpent!! }
                    .average()
                    .toLong()
                
                StepTypePerformance(
                    stepType = stepType,
                    totalSteps = total,
                    completedSteps = completed,
                    skippedSteps = skipped,
                    completionRate = if (total > 0) completed.toFloat() / total.toFloat() else 0f,
                    averageTimeMs = avgTime
                )
            }
    }
    
    /**
     * Clear analytics data
     */
    fun clearAnalytics() {
        _analytics.value = emptyList()
        _currentStepAnalytics.value = null
        _userInteractions.value = emptyList()
        Log.d("OnboardingAnalytics", "Cleared all analytics data")
    }
    
    /**
     * Export analytics data
     */
    fun exportAnalytics(): OnboardingAnalyticsExport {
        return OnboardingAnalyticsExport(
            totalSteps = _analytics.value.size,
            completedSteps = _analytics.value.count { it.wasCompleted },
            skippedSteps = _analytics.value.count { it.wasSkipped },
            completionRate = getCompletionRate(),
            averageTimePerStep = getAverageTimePerStep(),
            totalTimeSpent = getTotalTimeSpent(),
            dropOffPoints = getDropOffPoints(),
            validationFailureRate = getValidationFailureRate(),
            stepTypePerformance = getStepTypePerformance(),
            stepAnalytics = _analytics.value
        )
    }
}

/**
 * Performance metrics for step types
 */
data class StepTypePerformance(
    val stepType: StepType,
    val totalSteps: Int,
    val completedSteps: Int,
    val skippedSteps: Int,
    val completionRate: Float,
    val averageTimeMs: Long
)

/**
 * Complete analytics export
 */
data class OnboardingAnalyticsExport(
    val totalSteps: Int,
    val completedSteps: Int,
    val skippedSteps: Int,
    val completionRate: Float,
    val averageTimePerStep: Long,
    val totalTimeSpent: Long,
    val dropOffPoints: List<String>,
    val validationFailureRate: Float,
    val stepTypePerformance: Map<StepType, StepTypePerformance>,
    val stepAnalytics: List<OnboardingAnalyticsData>
)
