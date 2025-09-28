package com.pandora.core.ai.context

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

/**
 * User Activity Pattern Recognition
 * Analyzes user behavior patterns and provides intelligent insights
 */
@Singleton
class UserActivityAnalyzer @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "UserActivityAnalyzer"
        private const val MAX_ACTIVITY_HISTORY = 1000
        private const val PATTERN_ANALYSIS_WINDOW = 7 * 24 * 60 * 60 * 1000L // 7 days
    }
    
    private val activityHistory = mutableListOf<UserActivity>()
    private val activityPatterns = mutableMapOf<String, ActivityPattern>()
    private val userBehaviorProfile = UserBehaviorProfile()
    
    /**
     * Get user activity analysis
     */
    fun getUserActivityAnalysis(): Flow<UserActivityAnalysis> = flow {
        try {
            val currentActivity = getCurrentActivity()
            val activityPatterns = analyzeActivityPatterns()
            val behaviorInsights = generateBehaviorInsights()
            val productivityMetrics = calculateProductivityMetrics()
            val activityRecommendations = generateActivityRecommendations()
            
            val analysis = UserActivityAnalysis(
                currentActivity = currentActivity,
                activityPatterns = activityPatterns,
                behaviorInsights = behaviorInsights,
                productivityMetrics = productivityMetrics,
                activityRecommendations = activityRecommendations,
                userBehaviorProfile = userBehaviorProfile,
                activityHistory = activityHistory.takeLast(50),
                lastAnalysisTime = System.currentTimeMillis()
            )
            
            emit(analysis)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting user activity analysis", e)
            emit(UserActivityAnalysis.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current activity
     */
    private fun getCurrentActivity(): UserActivity {
        val timestamp = System.currentTimeMillis()
        val activityType = determineCurrentActivityType()
        val appContext = getCurrentAppContext()
        val interactionLevel = getCurrentInteractionLevel()
        
        return UserActivity(
            type = activityType,
            timestamp = timestamp,
            duration = 0L,
            appContext = appContext,
            interactionLevel = interactionLevel,
            metadata = getCurrentActivityMetadata()
        )
    }
    
    /**
     * Determine current activity type
     */
    private fun determineCurrentActivityType(): ActivityType {
        val recentActivities = activityHistory.takeLast(10)
        val typingCount = recentActivities.count { it.type == ActivityType.TYPING }
        val scrollingCount = recentActivities.count { it.type == ActivityType.SCROLLING }
        val appSwitchCount = recentActivities.count { it.type == ActivityType.APP_SWITCH }
        
        return when {
            typingCount > scrollingCount && typingCount > appSwitchCount -> ActivityType.TYPING
            scrollingCount > typingCount && scrollingCount > appSwitchCount -> ActivityType.SCROLLING
            appSwitchCount > typingCount && appSwitchCount > scrollingCount -> ActivityType.APP_SWITCH
            else -> ActivityType.TYPING
        }
    }
    
    /**
     * Get current app context
     */
    private fun getCurrentAppContext(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val runningTasks = activityManager.getRunningTasks(1)
                runningTasks.firstOrNull()?.topActivity?.packageName
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get current interaction level
     */
    private fun getCurrentInteractionLevel(): InteractionLevel {
        val recentActivities = activityHistory.takeLast(5)
        val activityCount = recentActivities.size
        val timeSpan = if (recentActivities.isNotEmpty()) {
            System.currentTimeMillis() - recentActivities.first().timestamp
        } else {
            0L
        }
        
        return when {
            activityCount > 10 && timeSpan < 60000L -> InteractionLevel.HIGH
            activityCount > 5 && timeSpan < 300000L -> InteractionLevel.MEDIUM
            activityCount > 0 -> InteractionLevel.LOW
            else -> InteractionLevel.LOW
        }
    }
    
    /**
     * Get current activity metadata
     */
    private fun getCurrentActivityMetadata(): Map<String, Any> {
        return mapOf(
            "screen_brightness" to getScreenBrightness(),
            "battery_level" to getBatteryLevel(),
            "is_charging" to isCharging(),
            "network_type" to getNetworkType(),
            "time_of_day" to getTimeOfDay()
        )
    }
    
    /**
     * Analyze activity patterns
     */
    private fun analyzeActivityPatterns(): List<ActivityPattern> {
        val patterns = mutableListOf<ActivityPattern>()
        val recentActivities = activityHistory.filter { 
            System.currentTimeMillis() - it.timestamp < PATTERN_ANALYSIS_WINDOW 
        }
        
        // Analyze typing patterns
        val typingPattern = analyzeTypingPattern(recentActivities)
        if (typingPattern != null) patterns.add(typingPattern)
        
        // Analyze app usage patterns
        val appUsagePattern = analyzeAppUsagePattern(recentActivities)
        if (appUsagePattern != null) patterns.add(appUsagePattern)
        
        // Analyze time-based patterns
        val timePattern = analyzeTimeBasedPattern(recentActivities)
        if (timePattern != null) patterns.add(timePattern)
        
        return patterns
    }
    
    /**
     * Analyze typing pattern
     */
    private fun analyzeTypingPattern(activities: List<UserActivity>): ActivityPattern? {
        val typingActivities = activities.filter { it.type == ActivityType.TYPING }
        if (typingActivities.isEmpty()) return null
        
        val averageTypingSpeed = calculateAverageTypingSpeed(typingActivities)
        val typingSessions = groupTypingSessions(typingActivities)
        val mostActiveTypingHour = getMostActiveTypingHour(typingActivities)
        
        return ActivityPattern(
            type = "typing",
            frequency = typingActivities.size,
            averageDuration = calculateAverageDuration(typingActivities),
            confidence = 0.8f,
            metadata = mapOf(
                "average_speed" to averageTypingSpeed,
                "session_count" to typingSessions.size,
                "most_active_hour" to mostActiveTypingHour
            )
        )
    }
    
    /**
     * Analyze app usage pattern
     */
    private fun analyzeAppUsagePattern(activities: List<UserActivity>): ActivityPattern? {
        val appUsage = activities.groupBy { it.appContext }
        if (appUsage.isEmpty()) return null
        
        val mostUsedApp = appUsage.maxByOrNull { it.value.size }?.key
        val appDiversity = appUsage.size
        val averageAppSessionDuration = calculateAverageAppSessionDuration(activities)
        
        return ActivityPattern(
            type = "app_usage",
            frequency = activities.size,
            averageDuration = averageAppSessionDuration,
            confidence = 0.7f,
            metadata = mapOf(
                "most_used_app" to (mostUsedApp ?: "unknown"),
                "app_diversity" to appDiversity,
                "total_apps" to appUsage.size
            )
        )
    }
    
    /**
     * Analyze time-based pattern
     */
    private fun analyzeTimeBasedPattern(activities: List<UserActivity>): ActivityPattern? {
        val hourlyActivity = activities.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        
        val peakHour = hourlyActivity.maxByOrNull { it.value.size }?.key ?: 0
        val activityDistribution = hourlyActivity.mapValues { it.value.size }
        
        return ActivityPattern(
            type = "time_based",
            frequency = activities.size,
            averageDuration = calculateAverageDuration(activities),
            confidence = 0.6f,
            metadata = mapOf(
                "peak_hour" to peakHour,
                "activity_distribution" to activityDistribution
            )
        )
    }
    
    /**
     * Generate behavior insights
     */
    private fun generateBehaviorInsights(): List<BehaviorInsight> {
        val insights = mutableListOf<BehaviorInsight>()
        
        // Productivity insight
        val productivityInsight = generateProductivityInsight()
        if (productivityInsight != null) insights.add(productivityInsight)
        
        // Focus insight
        val focusInsight = generateFocusInsight()
        if (focusInsight != null) insights.add(focusInsight)
        
        // Work-life balance insight
        val workLifeBalanceInsight = generateWorkLifeBalanceInsight()
        if (workLifeBalanceInsight != null) insights.add(workLifeBalanceInsight)
        
        return insights
    }
    
    /**
     * Generate productivity insight
     */
    private fun generateProductivityInsight(): BehaviorInsight? {
        val recentActivities = activityHistory.takeLast(100)
        val productivityScore = calculateProductivityScore(recentActivities)
        
        return BehaviorInsight(
            type = "productivity",
            title = "Productivity Analysis",
            description = "Your productivity score is ${(productivityScore * 100).toInt()}%",
            confidence = 0.8f,
            recommendations = listOf(
                "Take regular breaks to maintain focus",
                "Use the Pomodoro technique for better time management"
            )
        )
    }
    
    /**
     * Generate focus insight
     */
    private fun generateFocusInsight(): BehaviorInsight? {
        val recentActivities = activityHistory.takeLast(50)
        val focusScore = calculateFocusScore(recentActivities)
        
        return BehaviorInsight(
            type = "focus",
            title = "Focus Analysis",
            description = "Your focus level is ${(focusScore * 100).toInt()}%",
            confidence = 0.7f,
            recommendations = listOf(
                "Minimize distractions during work hours",
                "Use focus mode to block notifications"
            )
        )
    }
    
    /**
     * Generate work-life balance insight
     */
    private fun generateWorkLifeBalanceInsight(): BehaviorInsight? {
        val recentActivities = activityHistory.takeLast(200)
        val workLifeBalanceScore = calculateWorkLifeBalanceScore(recentActivities)
        
        return BehaviorInsight(
            type = "work_life_balance",
            title = "Work-Life Balance",
            description = "Your work-life balance score is ${(workLifeBalanceScore * 100).toInt()}%",
            confidence = 0.6f,
            recommendations = listOf(
                "Set clear boundaries between work and personal time",
                "Take regular breaks and engage in leisure activities"
            )
        )
    }
    
    /**
     * Calculate productivity metrics
     */
    private fun calculateProductivityMetrics(): ProductivityMetrics {
        val recentActivities = activityHistory.takeLast(100)
        
        return ProductivityMetrics(
            totalSessions = recentActivities.size,
            averageSessionDuration = calculateAverageDuration(recentActivities),
            productivityScore = calculateProductivityScore(recentActivities),
            focusScore = calculateFocusScore(recentActivities),
            workLifeBalanceScore = calculateWorkLifeBalanceScore(recentActivities),
            mostProductiveHour = getMostProductiveHour(recentActivities),
            leastProductiveHour = getLeastProductiveHour(recentActivities)
        )
    }
    
    /**
     * Generate activity recommendations
     */
    private fun generateActivityRecommendations(): List<ActivityRecommendation> {
        val recommendations = mutableListOf<ActivityRecommendation>()
        
        val recentActivities = activityHistory.takeLast(50)
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Time-based recommendations
        when (currentHour) {
            in 9..11 -> {
                recommendations.add(ActivityRecommendation(
                    "Focus on important tasks during your peak productivity hours",
                    "productivity",
                    0.8f
                ))
            }
            in 12..13 -> {
                recommendations.add(ActivityRecommendation(
                    "Take a break and recharge for the afternoon",
                    "break",
                    0.7f
                ))
            }
            in 14..16 -> {
                recommendations.add(ActivityRecommendation(
                    "Continue with your afternoon tasks",
                    "productivity",
                    0.6f
                ))
            }
            in 17..19 -> {
                recommendations.add(ActivityRecommendation(
                    "Wrap up your work and prepare for the evening",
                    "wind_down",
                    0.5f
                ))
            }
        }
        
        // Activity-based recommendations
        val typingCount = recentActivities.count { it.type == ActivityType.TYPING }
        if (typingCount > 20) {
            recommendations.add(ActivityRecommendation(
                "You've been typing a lot - consider taking a break",
                "health",
                0.6f
            ))
        }
        
        return recommendations
    }
    
    /**
     * Record user activity
     */
    fun recordActivity(activity: UserActivity) {
        activityHistory.add(activity)
        if (activityHistory.size > MAX_ACTIVITY_HISTORY) {
            activityHistory.removeAt(0)
        }
    }
    
    /**
     * Get screen brightness
     */
    private fun getScreenBrightness(): Int {
        return try {
            val brightness = android.provider.Settings.System.getInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS
            )
            (brightness * 100 / 255)
        } catch (e: Exception) {
            50
        }
    }
    
    /**
     * Get battery level
     */
    private fun getBatteryLevel(): Int {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (e: Exception) {
            50
        }
    }
    
    /**
     * Check if charging
     */
    private fun isCharging(): Boolean {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val status = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_STATUS)
            status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get network type
     */
    private fun getNetworkType(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = @Suppress("MissingPermission") connectivityManager.activeNetwork
            if (activeNetwork != null) "connected" else "disconnected"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    /**
     * Get time of day
     */
    private fun getTimeOfDay(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..22 -> "evening"
            else -> "night"
        }
    }
    
    /**
     * Calculate average typing speed
     */
    private fun calculateAverageTypingSpeed(activities: List<UserActivity>): Float {
        // This would calculate actual typing speed
        return 40.0f // Placeholder
    }
    
    /**
     * Group typing sessions
     */
    private fun groupTypingSessions(activities: List<UserActivity>): List<List<UserActivity>> {
        // This would group consecutive typing activities
        return listOf(activities)
    }
    
    /**
     * Get most active typing hour
     */
    private fun getMostActiveTypingHour(activities: List<UserActivity>): Int {
        val hourlyCount = activities.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        return hourlyCount.maxByOrNull { it.value.size }?.key ?: 0
    }
    
    /**
     * Calculate average duration
     */
    private fun calculateAverageDuration(activities: List<UserActivity>): Long {
        if (activities.isEmpty()) return 0L
        return activities.map { it.duration }.average().toLong()
    }
    
    /**
     * Calculate average app session duration
     */
    private fun calculateAverageAppSessionDuration(activities: List<UserActivity>): Long {
        // This would calculate actual app session duration
        return 300000L // 5 minutes placeholder
    }
    
    /**
     * Calculate productivity score
     */
    private fun calculateProductivityScore(activities: List<UserActivity>): Float {
        // This would calculate actual productivity score
        return 0.7f // Placeholder
    }
    
    /**
     * Calculate focus score
     */
    private fun calculateFocusScore(activities: List<UserActivity>): Float {
        // This would calculate actual focus score
        return 0.6f // Placeholder
    }
    
    /**
     * Calculate work-life balance score
     */
    private fun calculateWorkLifeBalanceScore(activities: List<UserActivity>): Float {
        // This would calculate actual work-life balance score
        return 0.5f // Placeholder
    }
    
    /**
     * Get most productive hour
     */
    private fun getMostProductiveHour(activities: List<UserActivity>): Int {
        val hourlyCount = activities.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        return hourlyCount.maxByOrNull { it.value.size }?.key ?: 0
    }
    
    /**
     * Get least productive hour
     */
    private fun getLeastProductiveHour(activities: List<UserActivity>): Int {
        val hourlyCount = activities.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        return hourlyCount.minByOrNull { it.value.size }?.key ?: 0
    }
}

/**
 * User activity
 */
data class UserActivity(
    val type: ActivityType,
    val timestamp: Long,
    val duration: Long,
    val appContext: String?,
    val interactionLevel: InteractionLevel,
    val metadata: Map<String, Any>
)

/**
 * Activity pattern
 */
data class ActivityPattern(
    val type: String,
    val frequency: Int,
    val averageDuration: Long,
    val confidence: Float,
    val metadata: Map<String, Any>
)

/**
 * Behavior insight
 */
data class BehaviorInsight(
    val type: String,
    val title: String,
    val description: String,
    val confidence: Float,
    val recommendations: List<String>
)

/**
 * Productivity metrics
 */
data class ProductivityMetrics(
    val totalSessions: Int,
    val averageSessionDuration: Long,
    val productivityScore: Float,
    val focusScore: Float,
    val workLifeBalanceScore: Float,
    val mostProductiveHour: Int,
    val leastProductiveHour: Int
)

/**
 * Activity recommendation
 */
data class ActivityRecommendation(
    val recommendation: String,
    val category: String,
    val confidence: Float
)

/**
 * User activity analysis
 */
data class UserActivityAnalysis(
    val currentActivity: UserActivity,
    val activityPatterns: List<ActivityPattern>,
    val behaviorInsights: List<BehaviorInsight>,
    val productivityMetrics: ProductivityMetrics,
    val activityRecommendations: List<ActivityRecommendation>,
    val userBehaviorProfile: UserBehaviorProfile,
    val activityHistory: List<UserActivity>,
    val lastAnalysisTime: Long
) {
    companion object {
        fun createEmpty() = UserActivityAnalysis(
            currentActivity = UserActivity(
                type = ActivityType.TYPING,
                timestamp = System.currentTimeMillis(),
                duration = 0L,
                appContext = null,
                interactionLevel = InteractionLevel.LOW,
                metadata = emptyMap()
            ),
            activityPatterns = emptyList(),
            behaviorInsights = emptyList(),
            productivityMetrics = ProductivityMetrics(
                totalSessions = 0,
                averageSessionDuration = 0L,
                productivityScore = 0f,
                focusScore = 0f,
                workLifeBalanceScore = 0f,
                mostProductiveHour = 0,
                leastProductiveHour = 0
            ),
            activityRecommendations = emptyList(),
            userBehaviorProfile = UserBehaviorProfile(),
            activityHistory = emptyList(),
            lastAnalysisTime = System.currentTimeMillis()
        )
    }
}

/**
 * User behavior profile
 */
data class UserBehaviorProfile(
    val personalityType: String = "unknown",
    val workStyle: String = "unknown",
    val communicationStyle: String = "unknown",
    val learningStyle: String = "unknown"
)

/**
 * Interaction level
 */
enum class InteractionLevel {
    IDLE, LOW, MEDIUM, HIGH
}
