package com.pandora.feature.gamification

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages gamification analytics and insights
 */
@Singleton
class GamificationAnalyticsManager @Inject constructor() {

    /**
     * Generate comprehensive analytics for a user
     */
    suspend fun generateAnalytics(profile: UserProfile): GamificationAnalytics {
        val totalEvents = calculateTotalEvents(profile)
        val engagementScore = calculateEngagementScore(profile)
        val retentionRate = calculateRetentionRate(profile)
        val averageSessionDuration = calculateAverageSessionDuration(profile)
        val mostActiveHours = calculateMostActiveHours(profile)
        val favoriteBadges = calculateFavoriteBadges(profile)
        val completionRates = calculateCompletionRates(profile)

        return GamificationAnalytics(
            userId = profile.userId,
            totalEvents = totalEvents,
            engagementScore = engagementScore,
            retentionRate = retentionRate,
            averageSessionDuration = averageSessionDuration,
            mostActiveHours = mostActiveHours,
            favoriteBadges = favoriteBadges,
            completionRates = completionRates
        )
    }

    /**
     * Calculate total events for the user
     */
    private fun calculateTotalEvents(profile: UserProfile): Int {
        return profile.stats.quickActionsUsed +
                profile.stats.learningSessions +
                profile.badges.count { it.isUnlocked } +
                profile.achievements.count { it.isCompleted }
    }

    /**
     * Calculate engagement score (0.0 to 1.0)
     */
    private fun calculateEngagementScore(profile: UserProfile): Float {
        var score = 0f

        // Base score from level
        score += profile.level * 0.1f

        // Score from badges
        score += profile.badges.count { it.isUnlocked } * 0.05f

        // Score from achievements
        score += profile.achievements.count { it.isCompleted } * 0.1f

        // Score from quick actions usage
        score += (profile.stats.quickActionsUsed * 0.01f).coerceAtMost(0.2f)

        // Score from learning sessions
        score += (profile.stats.learningSessions * 0.02f).coerceAtMost(0.2f)

        // Score from consecutive days
        score += (profile.stats.consecutiveDays * 0.01f).coerceAtMost(0.3f)

        return score.coerceAtMost(1.0f)
    }

    /**
     * Calculate retention rate based on consecutive days
     */
    private fun calculateRetentionRate(profile: UserProfile): Float {
        val totalDays = calculateTotalDays(profile)
        if (totalDays == 0) return 0f

        return profile.stats.consecutiveDays.toFloat() / totalDays
    }

    /**
     * Calculate total days since first activity
     */
    private fun calculateTotalDays(profile: UserProfile): Int {
        val firstActivity = profile.stats.streakStartDate
        val now = System.currentTimeMillis()
        val daysDiff = (now - firstActivity) / (24 * 60 * 60 * 1000)
        return daysDiff.toInt().coerceAtLeast(1)
    }

    /**
     * Calculate average session duration
     */
    private fun calculateAverageSessionDuration(profile: UserProfile): Long {
        val totalSessions = profile.stats.learningSessions + 1 // +1 to avoid division by zero
        return profile.stats.totalTimeSpent / totalSessions
    }

    /**
     * Calculate most active hours (simplified implementation)
     */
    private fun calculateMostActiveHours(profile: UserProfile): List<Int> {
        // In a real implementation, this would analyze actual usage patterns
        // For now, return some default active hours
        return listOf(9, 10, 11, 14, 15, 16, 20, 21)
    }

    /**
     * Calculate favorite badges based on rarity and points
     */
    private fun calculateFavoriteBadges(profile: UserProfile): List<String> {
        return profile.badges
            .filter { it.isUnlocked }
            .sortedByDescending { it.points * getRarityMultiplier(it.rarity) }
            .take(5)
            .map { it.id }
    }

    /**
     * Get rarity multiplier for scoring
     */
    private fun getRarityMultiplier(rarity: BadgeRarity): Float {
        return when (rarity) {
            BadgeRarity.COMMON -> 1.0f
            BadgeRarity.UNCOMMON -> 1.5f
            BadgeRarity.RARE -> 2.0f
            BadgeRarity.EPIC -> 3.0f
            BadgeRarity.LEGENDARY -> 5.0f
        }
    }

    /**
     * Calculate completion rates for different categories
     */
    private fun calculateCompletionRates(profile: UserProfile): Map<String, Float> {
        val totalBadges = profile.badges.size
        val unlockedBadges = profile.badges.count { it.isUnlocked }
        val badgeCompletionRate = if (totalBadges > 0) unlockedBadges.toFloat() / totalBadges else 0f

        val totalAchievements = profile.achievements.size
        val completedAchievements = profile.achievements.count { it.isCompleted }
        val achievementCompletionRate = if (totalAchievements > 0) completedAchievements.toFloat() / totalAchievements else 0f

        val totalChallenges = 3 // Assuming 3 daily challenges
        val completedChallenges = 0 // This would be tracked separately
        val challengeCompletionRate = if (totalChallenges > 0) completedChallenges.toFloat() / totalChallenges else 0f

        return mapOf(
            "badges" to badgeCompletionRate,
            "achievements" to achievementCompletionRate,
            "challenges" to challengeCompletionRate
        )
    }

    /**
     * Track a gamification event
     */
    suspend fun trackEvent(event: GamificationEvent) {
        Log.d("GamificationAnalytics", "Tracked event: ${event.eventType} for user ${event.userId}")
        
        // In a real implementation, this would save to analytics database
        // and potentially send to analytics service
    }

    /**
     * Get user insights and recommendations
     */
    suspend fun getUserInsights(profile: UserProfile): List<String> {
        val insights = mutableListOf<String>()

        // Speed insights
        if (profile.stats.averageWpm < 30) {
            insights.add("Try to increase your typing speed to unlock more badges!")
        } else if (profile.stats.averageWpm > 80) {
            insights.add("Great typing speed! You're in the top tier of users.")
        }

        // Accuracy insights
        if (profile.stats.averageAccuracy < 85) {
            insights.add("Focus on accuracy to improve your overall performance.")
        } else if (profile.stats.averageAccuracy > 95) {
            insights.add("Excellent accuracy! You're a precision master.")
        }

        // Quick actions insights
        if (profile.stats.quickActionsUsed < 10) {
            insights.add("Try using more quick actions to boost your productivity.")
        } else if (profile.stats.quickActionsUsed > 50) {
            insights.add("You're a quick action expert! Keep up the great work.")
        }

        // Learning insights
        if (profile.stats.learningSessions < 5) {
            insights.add("Complete more learning sessions to unlock achievements.")
        } else if (profile.stats.learningSessions > 20) {
            insights.add("You're a learning champion! Your dedication is impressive.")
        }

        // Streak insights
        if (profile.stats.consecutiveDays < 3) {
            insights.add("Build a daily habit to maintain your streak.")
        } else if (profile.stats.consecutiveDays > 30) {
            insights.add("Amazing streak! You're a consistency master.")
        }

        // Badge insights
        val unlockedBadges = profile.badges.count { it.isUnlocked }
        val totalBadges = profile.badges.size
        if (unlockedBadges < totalBadges * 0.3) {
            insights.add("You have many badges to unlock! Keep exploring features.")
        } else if (unlockedBadges > totalBadges * 0.7) {
            insights.add("You're a badge collector! Almost all badges unlocked.")
        }

        return insights
    }

    /**
     * Get performance trends
     */
    suspend fun getPerformanceTrends(profile: UserProfile): Map<String, String> {
        val trends = mutableMapOf<String, String>()

        // Typing speed trend
        when {
            profile.stats.averageWpm > 60 -> trends["typing_speed"] = "Excellent"
            profile.stats.averageWpm > 40 -> trends["typing_speed"] = "Good"
            profile.stats.averageWpm > 20 -> trends["typing_speed"] = "Improving"
            else -> trends["typing_speed"] = "Needs Practice"
        }

        // Accuracy trend
        when {
            profile.stats.averageAccuracy > 95 -> trends["accuracy"] = "Excellent"
            profile.stats.averageAccuracy > 90 -> trends["accuracy"] = "Good"
            profile.stats.averageAccuracy > 80 -> trends["accuracy"] = "Improving"
            else -> trends["accuracy"] = "Needs Practice"
        }

        // Engagement trend
        val engagementScore = calculateEngagementScore(profile)
        when {
            engagementScore > 0.8f -> trends["engagement"] = "Highly Engaged"
            engagementScore > 0.6f -> trends["engagement"] = "Engaged"
            engagementScore > 0.4f -> trends["engagement"] = "Moderately Engaged"
            else -> trends["engagement"] = "Low Engagement"
        }

        return trends
    }
}
