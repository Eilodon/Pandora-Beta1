package com.pandora.feature.gamification

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main coordinator for gamification system
 */
@Singleton
class GamificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: GamificationStorage,
    private val analytics: GamificationAnalyticsManager,
    private val badgeEngine: BadgeEngine,
    private val achievementEngine: AchievementEngine,
    private val progressTracker: ProgressTracker
) {
    
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    
    private val _dailyChallenges = MutableStateFlow<List<DailyChallenge>>(emptyList())
    val dailyChallenges: StateFlow<List<DailyChallenge>> = _dailyChallenges.asStateFlow()
    
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()
    
    private val _notifications = MutableStateFlow<List<GamificationNotification>>(emptyList())
    val notifications: StateFlow<List<GamificationNotification>> = _notifications.asStateFlow()
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Initialize the gamification system
     */
    suspend fun initialize() {
        try {
            // Load user profile
            val profile = storage.loadUserProfile()
            _userProfile.value = profile
            
            // Load daily challenges
            val challenges = storage.loadDailyChallenges()
            _dailyChallenges.value = challenges
            
            // Load leaderboard
            val leaderboardData = storage.loadLeaderboard()
            _leaderboard.value = leaderboardData
            
            // Initialize progress tracking
            progressTracker.initialize()
            
            _isInitialized.value = true
            Log.d("GamificationManager", "Gamification system initialized")
        } catch (e: Exception) {
            _error.value = "Failed to initialize gamification: ${e.message}"
            Log.e("GamificationManager", "Error initializing gamification", e)
        }
    }

    /**
     * Track user activity and update progress
     */
    suspend fun trackActivity(metricType: MetricType, value: Float, sessionId: String? = null) {
        try {
            val userId = _userProfile.value?.userId ?: "default"
            
            // Create progress entry
            val progressEntry = ProgressEntry(
                userId = userId,
                metricType = metricType,
                value = value,
                sessionId = sessionId
            )
            
            // Save progress
            storage.saveProgressEntry(progressEntry)
            
            // Update progress tracker
            progressTracker.updateProgress(metricType, value)
            
            // Check for badge/achievement unlocks
            checkForUnlocks(metricType, value)
            
            // Update user profile
            updateUserProfile()
            
            Log.d("GamificationManager", "Tracked activity: $metricType = $value")
        } catch (e: Exception) {
            _error.value = "Failed to track activity: ${e.message}"
            Log.e("GamificationManager", "Error tracking activity", e)
        }
    }

    /**
     * Check for badge and achievement unlocks
     */
    private suspend fun checkForUnlocks(metricType: MetricType, value: Float) {
        val profile = _userProfile.value ?: return
        
        // Check badge unlocks
        val newBadges = badgeEngine.checkBadgeUnlocks(profile, metricType, value)
        if (newBadges.isNotEmpty()) {
            val updatedProfile = profile.copy(
                badges = profile.badges + newBadges,
                totalPoints = profile.totalPoints + newBadges.sumOf { it.points }
            )
            _userProfile.value = updatedProfile
            storage.saveUserProfile(updatedProfile)
            
            // Send notifications
            newBadges.forEach { badge ->
                addNotification(
                    GamificationNotification(
                        type = NotificationType.BADGE_EARNED,
                        title = "Badge Unlocked!",
                        message = "You earned the ${badge.name} badge!",
                        data = mapOf("badgeId" to badge.id)
                    )
                )
            }
        }
        
        // Check achievement unlocks
        val newAchievements = achievementEngine.checkAchievementUnlocks(profile, metricType, value)
        if (newAchievements.isNotEmpty()) {
            val updatedProfile = profile.copy(
                achievements = profile.achievements + newAchievements,
                totalPoints = profile.totalPoints + newAchievements.sumOf { it.points }
            )
            _userProfile.value = updatedProfile
            storage.saveUserProfile(updatedProfile)
            
            // Send notifications
            newAchievements.forEach { achievement ->
                addNotification(
                    GamificationNotification(
                        type = NotificationType.ACHIEVEMENT_COMPLETED,
                        title = "Achievement Unlocked!",
                        message = "You completed ${achievement.name}!",
                        data = mapOf("achievementId" to achievement.id)
                    )
                )
            }
        }
    }

    /**
     * Update user profile with latest stats
     */
    private suspend fun updateUserProfile() {
        val currentProfile = _userProfile.value ?: return
        val updatedStats = progressTracker.getUpdatedStats(currentProfile.stats)
        
        // Check for level up
        val newLevel = calculateLevel(updatedStats.totalTimeSpent.toInt())
        val levelUp = newLevel > currentProfile.level
        
        val updatedProfile = currentProfile.copy(
            stats = updatedStats,
            level = newLevel,
            experience = updatedStats.totalTimeSpent.toInt(), // Use totalTimeSpent as experience for now
            experienceToNextLevel = calculateExperienceToNextLevel(newLevel)
        )
        
        _userProfile.value = updatedProfile
        storage.saveUserProfile(updatedProfile)
        
        if (levelUp) {
            addNotification(
                GamificationNotification(
                    type = NotificationType.LEVEL_UP,
                    title = "Level Up!",
                    message = "You reached level $newLevel!",
                    data = mapOf("newLevel" to newLevel)
                )
            )
        }
    }

    /**
     * Complete a daily challenge
     */
    suspend fun completeChallenge(challengeId: String) {
        try {
            val challenges = _dailyChallenges.value.toMutableList()
            val challengeIndex = challenges.indexOfFirst { it.id == challengeId }
            
            if (challengeIndex != -1) {
                val challenge = challenges[challengeIndex]
                val completedChallenge = challenge.copy(
                    isCompleted = true,
                    current = challenge.target
                )
                challenges[challengeIndex] = completedChallenge
                _dailyChallenges.value = challenges
                
                // Award rewards
                val profile = _userProfile.value ?: return
                val updatedProfile = profile.copy(
                    totalPoints = profile.totalPoints + challenge.reward.points,
                    experience = profile.experience + challenge.reward.experience
                )
                _userProfile.value = updatedProfile
                storage.saveUserProfile(updatedProfile)
                
                // Send notification
                addNotification(
                    GamificationNotification(
                        type = NotificationType.CHALLENGE_COMPLETED,
                        title = "Challenge Completed!",
                        message = "You completed ${challenge.title}!",
                        data = mapOf("challengeId" to challengeId)
                    )
                )
                
                Log.d("GamificationManager", "Completed challenge: $challengeId")
            }
        } catch (e: Exception) {
            _error.value = "Failed to complete challenge: ${e.message}"
            Log.e("GamificationManager", "Error completing challenge", e)
        }
    }

    /**
     * Get user's badge progress
     */
    fun getBadgeProgress(badgeId: String): Float {
        val profile = _userProfile.value ?: return 0f
        val badge = profile.badges.find { it.id == badgeId }
        return badge?.progress ?: 0f
    }

    /**
     * Get user's achievement progress
     */
    fun getAchievementProgress(achievementId: String): Float {
        val profile = _userProfile.value ?: return 0f
        val achievement = profile.achievements.find { it.id == achievementId }
        return achievement?.progress ?: 0f
    }

    /**
     * Get leaderboard for a specific category
     */
    suspend fun getLeaderboard(category: BadgeCategory? = null): List<LeaderboardEntry> {
        return storage.loadLeaderboard(category)
    }

    /**
     * Update user preferences
     */
    suspend fun updatePreferences(preferences: GamificationPreferences) {
        val profile = _userProfile.value ?: return
        val updatedProfile = profile.copy(preferences = preferences)
        _userProfile.value = updatedProfile
        storage.saveUserProfile(updatedProfile)
    }

    /**
     * Dismiss a notification
     */
    fun dismissNotification(notificationId: String) {
        val notifications = _notifications.value.toMutableList()
        notifications.removeAll { it.id == notificationId }
        _notifications.value = notifications
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    /**
     * Get gamification analytics
     */
    suspend fun getAnalytics(): GamificationAnalytics? {
        val profile = _userProfile.value ?: return null
        return analytics.generateAnalytics(profile)
    }

    /**
     * Reset user progress (for testing or account reset)
     */
    suspend fun resetProgress() {
        val defaultProfile = UserProfile()
        _userProfile.value = defaultProfile
        storage.saveUserProfile(defaultProfile)
        progressTracker.reset()
        _notifications.value = emptyList()
        Log.d("GamificationManager", "Progress reset")
    }

    // Helper functions
    private fun calculateLevel(experience: Int): Int {
        return (experience / 100) + 1
    }

    private fun calculateExperienceToNextLevel(level: Int): Int {
        return level * 100
    }

    private fun addNotification(notification: GamificationNotification) {
        val notifications = _notifications.value.toMutableList()
        notifications.add(notification)
        _notifications.value = notifications
    }
}

/**
 * Gamification notification
 */
data class GamificationNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val data: Map<String, Any> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    BADGE_EARNED,
    ACHIEVEMENT_COMPLETED,
    LEVEL_UP,
    CHALLENGE_COMPLETED,
    STREAK_BROKEN,
    STREAK_MAINTAINED,
    PROGRESS_MADE
}
