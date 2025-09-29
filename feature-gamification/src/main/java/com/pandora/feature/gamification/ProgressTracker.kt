package com.pandora.feature.gamification

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks user progress and updates statistics
 */
@Singleton
class ProgressTracker @Inject constructor() {

    private var _isInitialized = false
    private val _progressData = mutableMapOf<MetricType, MutableList<Float>>()

    /**
     * Initialize the progress tracker
     */
    suspend fun initialize() {
        _isInitialized = true
        Log.d("ProgressTracker", "Progress tracker initialized")
    }

    /**
     * Update progress for a specific metric
     */
    suspend fun updateProgress(metricType: MetricType, value: Float) {
        if (!_isInitialized) {
            Log.w("ProgressTracker", "Progress tracker not initialized")
            return
        }

        val data = _progressData.getOrPut(metricType) { mutableListOf() }
        data.add(value)
        
        // Keep only last 1000 entries to prevent memory issues
        if (data.size > 1000) {
            data.removeAt(0)
        }
        
        Log.d("ProgressTracker", "Updated progress: $metricType = $value")
    }

    /**
     * Get updated statistics based on current progress
     */
    suspend fun getUpdatedStats(currentStats: UserStats): UserStats {
        if (!_isInitialized) {
            return currentStats
        }

        val typingSpeedData = _progressData[MetricType.TYPING_SPEED] ?: emptyList()
        val accuracyData = _progressData[MetricType.ACCURACY] ?: emptyList()
        val quickActionsData = _progressData[MetricType.QUICK_ACTIONS] ?: emptyList()
        val learningTimeData = _progressData[MetricType.LEARNING_TIME] ?: emptyList()
        val wordsTypedData = _progressData[MetricType.WORDS_TYPED] ?: emptyList()
        val sessionDurationData = _progressData[MetricType.SESSION_DURATION] ?: emptyList()

        // Calculate average typing speed
        val averageWpm = if (typingSpeedData.isNotEmpty()) {
            typingSpeedData.average().toFloat()
        } else {
            currentStats.averageWpm
        }

        // Calculate average accuracy
        val averageAccuracy = if (accuracyData.isNotEmpty()) {
            accuracyData.average().toFloat()
        } else {
            currentStats.averageAccuracy
        }

        // Calculate total quick actions used
        val totalQuickActions = currentStats.quickActionsUsed + quickActionsData.sum().toInt()

        // Calculate total learning sessions
        val totalLearningSessions = currentStats.learningSessions + learningTimeData.size

        // Calculate total words typed
        val totalWordsTyped = currentStats.totalWordsTyped + wordsTypedData.sum().toInt()

        // Calculate total time spent
        val totalTimeSpent = currentStats.totalTimeSpent + sessionDurationData.sum().toLong()

        // Calculate consecutive days (simplified logic)
        val consecutiveDays = calculateConsecutiveDays(currentStats)

        // Calculate experience based on various factors
        val experience = calculateExperience(
            totalWordsTyped,
            totalTimeSpent,
            averageWpm,
            averageAccuracy,
            totalQuickActions,
            totalLearningSessions
        )

        return currentStats.copy(
            totalWordsTyped = totalWordsTyped,
            totalTimeSpent = totalTimeSpent,
            averageWpm = averageWpm,
            averageAccuracy = averageAccuracy,
            quickActionsUsed = totalQuickActions,
            learningSessions = totalLearningSessions,
            consecutiveDays = consecutiveDays,
            lastActivityDate = System.currentTimeMillis()
        )
    }

    /**
     * Calculate consecutive days (simplified implementation)
     */
    private fun calculateConsecutiveDays(currentStats: UserStats): Int {
        val today = System.currentTimeMillis()
        val lastActivity = currentStats.lastActivityDate
        val streakStart = currentStats.streakStartDate

        // If last activity was today, maintain streak
        if (isSameDay(today, lastActivity)) {
            return currentStats.consecutiveDays
        }

        // If last activity was yesterday, increment streak
        if (isSameDay(today - 24 * 60 * 60 * 1000, lastActivity)) {
            return currentStats.consecutiveDays + 1
        }

        // If last activity was more than 1 day ago, reset streak
        return 1
    }

    /**
     * Check if two timestamps are on the same day
     */
    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val day1 = timestamp1 / (24 * 60 * 60 * 1000)
        val day2 = timestamp2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }

    /**
     * Calculate experience points based on various metrics
     */
    private fun calculateExperience(
        wordsTyped: Int,
        timeSpent: Long,
        averageWpm: Float,
        averageAccuracy: Float,
        quickActionsUsed: Int,
        learningSessions: Int
    ): Int {
        var experience = 0

        // Base experience from words typed
        experience += wordsTyped / 10 // 1 XP per 10 words

        // Bonus experience for time spent
        experience += (timeSpent / 60000).toInt() // 1 XP per minute

        // Bonus experience for high typing speed
        if (averageWpm > 60) {
            experience += (averageWpm - 60).toInt() * 2 // 2 XP per WPM above 60
        }

        // Bonus experience for high accuracy
        if (averageAccuracy > 90) {
            experience += (averageAccuracy - 90).toInt() * 5 // 5 XP per % above 90
        }

        // Bonus experience for quick actions
        experience += quickActionsUsed * 2 // 2 XP per quick action

        // Bonus experience for learning sessions
        experience += learningSessions * 10 // 10 XP per learning session

        return experience
    }

    /**
     * Get progress data for a specific metric
     */
    fun getProgressData(metricType: MetricType): List<Float> {
        return _progressData[metricType] ?: emptyList()
    }

    /**
     * Get progress summary for all metrics
     */
    fun getProgressSummary(): Map<MetricType, ProgressSummary> {
        return _progressData.mapValues { (_, data) ->
            ProgressSummary(
                count = data.size,
                average = if (data.isNotEmpty()) data.average().toFloat() else 0f,
                max = if (data.isNotEmpty()) data.maxOrNull() ?: 0f else 0f,
                min = if (data.isNotEmpty()) data.minOrNull() ?: 0f else 0f,
                total = data.sum()
            )
        }
    }

    /**
     * Reset all progress data
     */
    suspend fun reset() {
        _progressData.clear()
        _isInitialized = false
        Log.d("ProgressTracker", "Progress tracker reset")
    }

    /**
     * Check if tracker is initialized
     */
    fun isInitialized(): Boolean {
        return _isInitialized
    }
}

/**
 * Progress summary for a metric
 */
data class ProgressSummary(
    val count: Int,
    val average: Float,
    val max: Float,
    val min: Float,
    val total: Float
)
