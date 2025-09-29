package com.pandora.feature.gamification

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * Represents a badge that users can earn
 */
@Entity(tableName = "badges")
data class Badge(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconRes: String, // Resource name for icon
    val category: BadgeCategory,
    val rarity: BadgeRarity,
    val requirements: List<BadgeRequirement>,
    val points: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Float = 0f // 0.0 to 1.0
)

/**
 * Categories of badges
 */
enum class BadgeCategory {
    TYPING_SPEED,
    ACCURACY,
    QUICK_ACTIONS,
    LEARNING,
    SOCIAL,
    ACHIEVEMENT,
    MILESTONE,
    SPECIAL
}

/**
 * Rarity levels for badges
 */
enum class BadgeRarity {
    COMMON,    // Green
    UNCOMMON,  // Blue
    RARE,      // Purple
    EPIC,      // Orange
    LEGENDARY  // Gold
}

/**
 * Requirements for earning a badge
 */
data class BadgeRequirement(
    val type: RequirementType,
    val target: Int,
    val current: Int = 0,
    val description: String
)

enum class RequirementType {
    TYPING_SPEED_WPM,
    ACCURACY_PERCENTAGE,
    QUICK_ACTIONS_USED,
    LEARNING_SESSIONS,
    CONSECUTIVE_DAYS,
    TOTAL_WORDS_TYPED,
    TOTAL_TIME_SPENT,
    CUSTOM_METRIC
}

/**
 * User's gamification profile
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String = "default",
    val totalPoints: Int = 0,
    val level: Int = 1,
    val experience: Int = 0,
    val experienceToNextLevel: Int = 100,
    val badges: List<Badge> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val stats: UserStats = UserStats(),
    val preferences: GamificationPreferences = GamificationPreferences(),
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * User statistics for gamification
 */
data class UserStats(
    val totalWordsTyped: Int = 0,
    val totalTimeSpent: Long = 0, // in milliseconds
    val averageWpm: Float = 0f,
    val averageAccuracy: Float = 0f,
    val quickActionsUsed: Int = 0,
    val learningSessions: Int = 0,
    val consecutiveDays: Int = 0,
    val lastActivityDate: Long = System.currentTimeMillis(),
    val streakStartDate: Long = System.currentTimeMillis()
)

/**
 * User preferences for gamification features
 */
data class GamificationPreferences(
    val enableNotifications: Boolean = true,
    val enableSoundEffects: Boolean = true,
    val enableHapticFeedback: Boolean = true,
    val showProgressAnimations: Boolean = true,
    val enableLeaderboards: Boolean = true,
    val enableSocialFeatures: Boolean = true,
    val notificationFrequency: NotificationFrequency = NotificationFrequency.MEDIUM
)

enum class NotificationFrequency {
    LOW,    // Only major achievements
    MEDIUM, // Regular progress updates
    HIGH    // All notifications
}

/**
 * Achievement system
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val iconRes: String,
    val category: AchievementCategory,
    val points: Int,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val progress: Float = 0f,
    val requirements: List<AchievementRequirement>
)

enum class AchievementCategory {
    DAILY,
    WEEKLY,
    MONTHLY,
    LIFETIME,
    SPECIAL
}

data class AchievementRequirement(
    val type: RequirementType,
    val target: Int,
    val current: Int = 0,
    val description: String
)

/**
 * Progress tracking for various metrics
 */
@Entity(tableName = "progress_tracking")
data class ProgressEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val metricType: MetricType,
    val value: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionId: String? = null
)

enum class MetricType {
    TYPING_SPEED,
    ACCURACY,
    QUICK_ACTIONS,
    LEARNING_TIME,
    WORDS_TYPED,
    SESSION_DURATION
}

/**
 * Leaderboard entry
 */
data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val avatar: String? = null,
    val score: Int,
    val rank: Int,
    val badge: Badge? = null,
    val isCurrentUser: Boolean = false
)

/**
 * Daily challenge
 */
data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val type: ChallengeType,
    val target: Int,
    val current: Int = 0,
    val reward: ChallengeReward,
    val isCompleted: Boolean = false,
    val expiresAt: Long,
    val difficulty: ChallengeDifficulty
)

enum class ChallengeType {
    TYPING_SPEED,
    ACCURACY,
    QUICK_ACTIONS,
    LEARNING_TIME,
    WORDS_TYPED,
    STREAK_DAYS
}

enum class ChallengeDifficulty {
    EASY,
    MEDIUM,
    HARD,
    EXPERT
}

data class ChallengeReward(
    val points: Int,
    val experience: Int,
    val badge: Badge? = null,
    val specialReward: String? = null
)

/**
 * Gamification events for analytics
 */
data class GamificationEvent(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val eventType: GamificationEventType,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class GamificationEventType {
    BADGE_EARNED,
    ACHIEVEMENT_COMPLETED,
    LEVEL_UP,
    POINTS_EARNED,
    CHALLENGE_COMPLETED,
    STREAK_BROKEN,
    STREAK_MAINTAINED,
    PROGRESS_MADE
}

/**
 * Gamification analytics data
 */
data class GamificationAnalytics(
    val userId: String,
    val totalEvents: Int,
    val engagementScore: Float,
    val retentionRate: Float,
    val averageSessionDuration: Long,
    val mostActiveHours: List<Int>,
    val favoriteBadges: List<String>,
    val completionRates: Map<String, Float>,
    val lastUpdated: Long = System.currentTimeMillis()
)
