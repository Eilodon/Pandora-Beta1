package com.pandora.feature.b2b

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages B2B analytics and insights
 */
@Singleton
class B2BAnalyticsManager @Inject constructor(
    private val storage: B2BStorage
) {
    private val analyticsScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Track analytics event
     */
    suspend fun trackEvent(
        organizationId: String,
        teamId: String? = null,
        userId: String? = null,
        metricType: AnalyticsMetricType,
        value: Float,
        metadata: Map<String, String> = emptyMap()
    ) {
        try {
            val analytics = B2BAnalytics(
                organizationId = organizationId,
                teamId = teamId,
                userId = userId,
                metricType = metricType,
                value = value,
                metadata = metadata
            )
            
            storage.saveAnalytics(analytics)
            Log.d("B2BAnalyticsManager", "Event tracked: $metricType = $value")
        } catch (e: Exception) {
            Log.e("B2BAnalyticsManager", "Failed to track event", e)
        }
    }

    /**
     * Track activity event
     */
    suspend fun trackEvent(
        organizationId: String,
        eventType: ActivityType,
        description: String,
        userId: String? = null,
        teamId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        try {
            val analytics = B2BAnalytics(
                organizationId = organizationId,
                teamId = teamId,
                userId = userId,
                metricType = AnalyticsMetricType.USER_ACTIVITY,
                value = 1f,
                metadata = metadata + mapOf(
                    "event_type" to eventType.name,
                    "description" to description
                )
            )
            
            storage.saveAnalytics(analytics)
            Log.d("B2BAnalyticsManager", "Activity tracked: $eventType - $description")
        } catch (e: Exception) {
            Log.e("B2BAnalyticsManager", "Failed to track activity", e)
        }
    }

    /**
     * Generate dashboard data
     */
    suspend fun generateDashboard(organizationId: String): B2BDashboard {
        return try {
            val users = storage.loadUsers(organizationId)
            val teams = storage.loadTeams(organizationId)
            val analytics = storage.loadAnalytics(organizationId)
            val notifications = storage.loadNotifications(organizationId)
            
            val totalUsers = users.size
            val activeUsers = users.count { it.isActive }
            
            val allShortcuts = teams.flatMap { team ->
                storage.loadTeamShortcuts(team.id)
            }
            val totalShortcuts = allShortcuts.size
            val shortcutsUsed = allShortcuts.sumOf { it.usageCount }
            
            val productivityScore = calculateProductivityScore(analytics)
            
            val topShortcuts = allShortcuts
                .sortedByDescending { it.usageCount }
                .take(10)
                .map { shortcut ->
                    ShortcutUsage(
                        shortcutId = shortcut.id,
                        name = shortcut.name,
                        usageCount = shortcut.usageCount,
                        lastUsed = shortcut.updatedAt,
                        rating = shortcut.rating
                    )
                }
            
            val teamStats = teams.map { team ->
                val teamShortcuts = storage.loadTeamShortcuts(team.id)
                val teamAnalytics = analytics.filter { it.teamId == team.id }
                val teamProductivityScore = calculateProductivityScore(teamAnalytics)
                
                TeamStats(
                    teamId = team.id,
                    name = team.name,
                    memberCount = users.count { it.teamId == team.id },
                    shortcutsCreated = teamShortcuts.size,
                    productivityScore = teamProductivityScore,
                    activityLevel = calculateActivityLevel(teamAnalytics)
                )
            }
            
            val recentActivity = generateRecentActivity(analytics, users)
            
            B2BDashboard(
                organizationId = organizationId,
                totalUsers = totalUsers,
                activeUsers = activeUsers,
                totalShortcuts = totalShortcuts,
                shortcutsUsed = shortcutsUsed,
                productivityScore = productivityScore,
                topShortcuts = topShortcuts,
                teamStats = teamStats,
                recentActivity = recentActivity
            )
        } catch (e: Exception) {
            Log.e("B2BAnalyticsManager", "Failed to generate dashboard", e)
            B2BDashboard(
                organizationId = organizationId,
                totalUsers = 0,
                activeUsers = 0,
                totalShortcuts = 0,
                shortcutsUsed = 0,
                productivityScore = 0f,
                topShortcuts = emptyList(),
                teamStats = emptyList(),
                recentActivity = emptyList()
            )
        }
    }

    /**
     * Get team analytics
     */
    suspend fun getTeamAnalytics(teamId: String): TeamAnalytics {
        return try {
            val analytics = storage.loadAnalytics("").filter { it.teamId == teamId }
            val shortcuts = storage.loadTeamShortcuts(teamId)
            
            val totalShortcuts = shortcuts.size
            val shortcutsUsed = shortcuts.sumOf { it.usageCount }
            val averageRating = if (shortcuts.isNotEmpty()) {
                shortcuts.map { it.rating }.average().toFloat()
            } else 0f
            
            val productivityScore = calculateProductivityScore(analytics)
            val activityLevel = calculateActivityLevel(analytics)
            
            TeamAnalytics(
                teamId = teamId,
                totalShortcuts = totalShortcuts,
                shortcutsUsed = shortcutsUsed,
                averageRating = averageRating,
                productivityScore = productivityScore,
                activityLevel = activityLevel,
                lastActivity = analytics.maxOfOrNull { it.timestamp } ?: 0L
            )
        } catch (e: Exception) {
            Log.e("B2BAnalyticsManager", "Failed to get team analytics", e)
            TeamAnalytics(
                teamId = teamId,
                totalShortcuts = 0,
                shortcutsUsed = 0,
                averageRating = 0f,
                productivityScore = 0f,
                activityLevel = ActivityLevel.LOW,
                lastActivity = 0L
            )
        }
    }

    /**
     * Get user analytics
     */
    suspend fun getUserAnalytics(userId: String): UserAnalytics {
        return try {
            val analytics = storage.loadAnalytics("").filter { it.userId == userId }
            
            val shortcutsCreated = analytics.count { 
                it.metadata["event_type"] == ActivityType.SHORTCUT_CREATED.name 
            }
            val shortcutsUsed = analytics.count { 
                it.metadata["event_type"] == ActivityType.SHORTCUT_USED.name 
            }
            
            val productivityScore = calculateProductivityScore(analytics)
            val activityLevel = calculateActivityLevel(analytics)
            
            UserAnalytics(
                userId = userId,
                shortcutsCreated = shortcutsCreated,
                shortcutsUsed = shortcutsUsed,
                productivityScore = productivityScore,
                activityLevel = activityLevel,
                lastActivity = analytics.maxOfOrNull { it.timestamp } ?: 0L
            )
        } catch (e: Exception) {
            Log.e("B2BAnalyticsManager", "Failed to get user analytics", e)
            UserAnalytics(
                userId = userId,
                shortcutsCreated = 0,
                shortcutsUsed = 0,
                productivityScore = 0f,
                activityLevel = ActivityLevel.LOW,
                lastActivity = 0L
            )
        }
    }

    /**
     * Calculate productivity score based on analytics
     */
    private fun calculateProductivityScore(analytics: List<B2BAnalytics>): Float {
        if (analytics.isEmpty()) return 0f
        
        val shortcutsCreated = analytics.count { 
            it.metadata["event_type"] == ActivityType.SHORTCUT_CREATED.name 
        }
        val shortcutsUsed = analytics.count { 
            it.metadata["event_type"] == ActivityType.SHORTCUT_USED.name 
        }
        val userActivity = analytics.count { 
            it.metadata["event_type"] == ActivityType.USER_LOGIN.name 
        }
        
        // Simple productivity calculation
        val score = (shortcutsCreated * 0.3f + shortcutsUsed * 0.5f + userActivity * 0.2f)
        return (score * 10f).coerceIn(0f, 100f)
    }

    /**
     * Calculate activity level based on analytics
     */
    private fun calculateActivityLevel(analytics: List<B2BAnalytics>): ActivityLevel {
        val recentAnalytics = analytics.filter { 
            it.timestamp > System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // Last 7 days
        }
        
        val activityCount = recentAnalytics.size
        return when {
            activityCount >= 100 -> ActivityLevel.VERY_HIGH
            activityCount >= 50 -> ActivityLevel.HIGH
            activityCount >= 20 -> ActivityLevel.MEDIUM
            else -> ActivityLevel.LOW
        }
    }

    /**
     * Generate recent activity events
     */
    private suspend fun generateRecentActivity(
        analytics: List<B2BAnalytics>,
        users: List<B2BUser>
    ): List<ActivityEvent> {
        val userMap = users.associateBy { it.id }
        
        return analytics
            .filter { it.metadata.containsKey("event_type") }
            .sortedByDescending { it.timestamp }
            .take(20)
            .map { analytic ->
                val user = userMap[analytic.userId]
                ActivityEvent(
                    type = ActivityType.valueOf(analytic.metadata["event_type"] ?: "USER_LOGIN"),
                    userId = analytic.userId ?: "unknown",
                    userName = user?.name ?: "Unknown User",
                    description = analytic.metadata["description"] ?: "Activity",
                    metadata = analytic.metadata,
                    timestamp = analytic.timestamp
                )
            }
    }
}

/**
 * Team analytics data
 */
data class TeamAnalytics(
    val teamId: String,
    val totalShortcuts: Int,
    val shortcutsUsed: Int,
    val averageRating: Float,
    val productivityScore: Float,
    val activityLevel: ActivityLevel,
    val lastActivity: Long
)

/**
 * User analytics data
 */
data class UserAnalytics(
    val userId: String,
    val shortcutsCreated: Int,
    val shortcutsUsed: Int,
    val productivityScore: Float,
    val activityLevel: ActivityLevel,
    val lastActivity: Long
)
