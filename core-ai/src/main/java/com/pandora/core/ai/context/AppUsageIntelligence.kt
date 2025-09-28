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
 * App Usage Context Intelligence
 * Provides intelligent analysis of app usage patterns and context
 */
@Singleton
class AppUsageIntelligence @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AppUsageIntelligence"
        private const val MAX_APP_HISTORY = 500
        private const val ANALYSIS_WINDOW = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val appUsageHistory = mutableListOf<AppUsageRecord>()
    private val appCategories = mutableMapOf<String, AppCategory>()
    private val appIntelligence = mutableMapOf<String, AppIntelligence>()
    
    /**
     * Get app usage intelligence
     */
    fun getAppUsageIntelligence(): Flow<AppUsageIntelligenceResult> = flow {
        try {
            val currentApp = getCurrentApp()
            val appUsagePatterns = analyzeAppUsagePatterns()
            val appContext = getAppContext(currentApp)
            val appRecommendations = generateAppRecommendations()
            val appInsights = generateAppInsights()
            val appPredictions = generateAppPredictions()
            
            val intelligence = AppUsageIntelligenceResult(
                currentApp = currentApp,
                appUsagePatterns = appUsagePatterns,
                appContext = appContext,
                appRecommendations = appRecommendations,
                appInsights = appInsights,
                appPredictions = appPredictions,
                appUsageHistory = appUsageHistory.takeLast(50),
                lastAnalysisTime = System.currentTimeMillis()
            )
            
            emit(intelligence)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting app usage intelligence", e)
            emit(AppUsageIntelligenceResult.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current app
     */
    private fun getCurrentApp(): String? {
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
     * Analyze app usage patterns
     */
    private fun analyzeAppUsagePatterns(): List<AppUsagePattern> {
        val patterns = mutableListOf<AppUsagePattern>()
        val recentUsage = appUsageHistory.filter { 
            System.currentTimeMillis() - it.timestamp < ANALYSIS_WINDOW 
        }
        
        // Analyze most used apps
        val mostUsedApps = getMostUsedApps(recentUsage)
        if (mostUsedApps.isNotEmpty()) {
            patterns.add(AppUsagePattern(
                type = "most_used_apps",
                apps = mostUsedApps,
                confidence = 0.9f,
                metadata = mapOf("count" to mostUsedApps.size)
            ))
        }
        
        // Analyze app switching patterns
        val appSwitchingPattern = analyzeAppSwitchingPattern(recentUsage)
        if (appSwitchingPattern != null) {
            patterns.add(appSwitchingPattern)
        }
        
        // Analyze time-based app usage
        val timeBasedPattern = analyzeTimeBasedAppUsage(recentUsage)
        if (timeBasedPattern != null) {
            patterns.add(timeBasedPattern)
        }
        
        // Analyze app category usage
        val categoryPattern = analyzeAppCategoryUsage(recentUsage)
        if (categoryPattern != null) {
            patterns.add(categoryPattern)
        }
        
        return patterns
    }
    
    /**
     * Get most used apps
     */
    private fun getMostUsedApps(usage: List<AppUsageRecord>): List<AppUsageInfo> {
        val appUsageCount = usage.groupBy { it.packageName }
            .mapValues { it.value.size }
        
        return appUsageCount.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (packageName, count) ->
                AppUsageInfo(
                    packageName = packageName,
                    usageCount = count,
                    category = getAppCategory(packageName),
                    lastUsed = usage.filter { it.packageName == packageName }
                        .maxByOrNull { it.timestamp }?.timestamp ?: 0L
                )
            }
    }
    
    /**
     * Analyze app switching pattern
     */
    private fun analyzeAppSwitchingPattern(usage: List<AppUsageRecord>): AppUsagePattern? {
        val appSwitches = usage.zipWithNext { current, next ->
            if (current.packageName != next.packageName) {
                AppSwitch(current.packageName, next.packageName, next.timestamp - current.timestamp)
            } else null
        }.filterNotNull()
        
        if (appSwitches.isEmpty()) return null
        
        val averageSwitchTime = appSwitches.map { it.duration }.average()
        val mostCommonSwitches = appSwitches.groupBy { "${it.from} -> ${it.to}" }
            .mapValues { it.value.size }
            .maxByOrNull { it.value }
        
        return AppUsagePattern(
            type = "app_switching",
            apps = emptyList(),
            confidence = 0.7f,
            metadata = mapOf(
                "average_switch_time" to averageSwitchTime,
                "most_common_switch" to (mostCommonSwitches?.key ?: "none"),
                "total_switches" to appSwitches.size
            )
        )
    }
    
    /**
     * Analyze time-based app usage
     */
    private fun analyzeTimeBasedAppUsage(usage: List<AppUsageRecord>): AppUsagePattern? {
        val hourlyUsage = usage.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        
        val peakHour = hourlyUsage.maxByOrNull { it.value.size }?.key ?: 0
        val lowUsageHour = hourlyUsage.minByOrNull { it.value.size }?.key ?: 0
        
        return AppUsagePattern(
            type = "time_based_usage",
            apps = emptyList(),
            confidence = 0.6f,
            metadata = mapOf(
                "peak_hour" to peakHour,
                "low_usage_hour" to lowUsageHour,
                "hourly_distribution" to hourlyUsage.mapValues { it.value.size }
            )
        )
    }
    
    /**
     * Analyze app category usage
     */
    private fun analyzeAppCategoryUsage(usage: List<AppUsageRecord>): AppUsagePattern? {
        val categoryUsage = usage.groupBy { getAppCategory(it.packageName) }
            .mapValues { it.value.size }
        
        val mostUsedCategory = categoryUsage.maxByOrNull { it.value }?.key ?: AppCategory.UNKNOWN
        val categoryDiversity = categoryUsage.size
        
        return AppUsagePattern(
            type = "category_usage",
            apps = emptyList(),
            confidence = 0.8f,
            metadata = mapOf(
                "most_used_category" to mostUsedCategory.name,
                "category_diversity" to categoryDiversity,
                "category_distribution" to categoryUsage.mapValues { it.value }
            )
        )
    }
    
    /**
     * Get app context
     */
    private fun getAppContext(packageName: String?): AppContext {
        if (packageName == null) return AppContext.createUnknown()
        
        val category = getAppCategory(packageName)
        val intelligence = getAppIntelligence(packageName)
        val usageHistory = appUsageHistory.filter { it.packageName == packageName }
        val lastUsed = usageHistory.maxByOrNull { it.timestamp }?.timestamp ?: 0L
        val usageFrequency = usageHistory.size
        
        return AppContext(
            currentApp = packageName,
            runningApps = getRunningApps(),
            appCategory = category,
            isMultitasking = getRunningApps().size > 1,
            appUsageTime = calculateAppUsageTime(packageName)
        )
    }
    
    /**
     * Generate app recommendations
     */
    private fun generateAppRecommendations(): List<AppRecommendation> {
        val recommendations = mutableListOf<AppRecommendation>()
        val currentApp = getCurrentApp()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Time-based recommendations
        when (currentHour) {
            in 9..17 -> {
                recommendations.add(AppRecommendation(
                    "Focus on productivity apps during work hours",
                    "productivity",
                    0.8f,
                    listOf("com.google.android.apps.docs", "com.microsoft.office.officehub")
                ))
            }
            in 18..22 -> {
                recommendations.add(AppRecommendation(
                    "Switch to entertainment apps for relaxation",
                    "entertainment",
                    0.6f,
                    listOf("com.spotify.music", "com.netflix.mediaclient")
                ))
            }
        }
        
        // App-specific recommendations
        currentApp?.let { app ->
            when (getAppCategory(app)) {
                AppCategory.MESSAGING -> {
                    recommendations.add(AppRecommendation(
                        "Consider using voice messages for faster communication",
                        "efficiency",
                        0.7f,
                        emptyList()
                    ))
                }
                AppCategory.BROWSER -> {
                    recommendations.add(AppRecommendation(
                        "Use bookmarks to save frequently visited sites",
                        "organization",
                        0.6f,
                        emptyList()
                    ))
                }
                AppCategory.NOTES -> {
                    recommendations.add(AppRecommendation(
                        "Set up voice-to-text for faster note-taking",
                        "productivity",
                        0.8f,
                        emptyList()
                    ))
                }
                else -> {}
            }
        }
        
        return recommendations
    }
    
    /**
     * Generate app insights
     */
    private fun generateAppInsights(): List<AppInsight> {
        val insights = mutableListOf<AppInsight>()
        val recentUsage = appUsageHistory.takeLast(100)
        
        // Usage frequency insight
        val totalApps = recentUsage.map { it.packageName }.distinct().size
        insights.add(AppInsight(
            type = "usage_frequency",
            title = "App Diversity",
            description = "You've used $totalApps different apps recently",
            confidence = 0.8f,
            recommendations = listOf(
                "Consider organizing apps into folders",
                "Remove unused apps to reduce clutter"
            )
        ))
        
        // Productivity insight
        val productiveApps = recentUsage.count { isProductiveApp(it.packageName) }
        val totalUsage = recentUsage.size
        val productivityRatio = if (totalUsage > 0) productiveApps.toFloat() / totalUsage else 0f
        
        insights.add(AppInsight(
            type = "productivity",
            title = "Productivity Balance",
            description = "You spend ${(productivityRatio * 100).toInt()}% of your time on productive apps",
            confidence = 0.7f,
            recommendations = listOf(
                "Increase productivity app usage during work hours",
                "Limit entertainment apps during focus time"
            )
        ))
        
        return insights
    }
    
    /**
     * Generate app predictions
     */
    private fun generateAppPredictions(): List<AppPrediction> {
        val predictions = mutableListOf<AppPrediction>()
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        // Time-based predictions
        when (currentHour) {
            in 9..11 -> {
                predictions.add(AppPrediction(
                    app = "com.google.android.apps.docs",
                    probability = 0.7f,
                    reason = "Morning productivity peak",
                    confidence = 0.8f
                ))
            }
            in 12..13 -> {
                predictions.add(AppPrediction(
                    app = "com.spotify.music",
                    probability = 0.6f,
                    reason = "Lunch break music",
                    confidence = 0.6f
                ))
            }
            in 17..19 -> {
                predictions.add(AppPrediction(
                    app = "com.whatsapp",
                    probability = 0.8f,
                    reason = "Evening communication",
                    confidence = 0.7f
                ))
            }
        }
        
        // Day-based predictions
        if (currentDay == Calendar.SATURDAY || currentDay == Calendar.SUNDAY) {
            predictions.add(AppPrediction(
                app = "com.netflix.mediaclient",
                probability = 0.9f,
                reason = "Weekend entertainment",
                confidence = 0.8f
            ))
        }
        
        return predictions
    }
    
    /**
     * Get app category
     */
    private fun getAppCategory(packageName: String): AppCategory {
        return appCategories[packageName] ?: determineAppCategory(packageName)
    }
    
    /**
     * Determine app category
     */
    private fun determineAppCategory(packageName: String): AppCategory {
        return when {
            packageName.contains("messaging") || packageName.contains("whatsapp") || 
            packageName.contains("telegram") -> AppCategory.MESSAGING
            packageName.contains("calendar") || packageName.contains("google") -> AppCategory.CALENDAR
            packageName.contains("notes") || packageName.contains("keep") -> AppCategory.NOTES
            packageName.contains("browser") || packageName.contains("chrome") -> AppCategory.BROWSER
            packageName.contains("music") || packageName.contains("spotify") -> AppCategory.MUSIC
            else -> AppCategory.UNKNOWN
        }
    }
    
    /**
     * Get app intelligence
     */
    private fun getAppIntelligence(packageName: String): AppIntelligence {
        return appIntelligence[packageName] ?: AppIntelligence.createDefault()
    }
    
    /**
     * Get running apps
     */
    private fun getRunningApps(): List<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
                val runningTasks = activityManager.getRunningTasks(10)
                runningTasks.map { it.topActivity?.packageName ?: "unknown" }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Calculate app usage time
     */
    private fun calculateAppUsageTime(packageName: String): Long {
        val usage = appUsageHistory.filter { it.packageName == packageName }
        return usage.sumOf { it.duration }
    }
    
    /**
     * Check if productive app
     */
    private fun isProductiveApp(packageName: String): Boolean {
        val category = getAppCategory(packageName)
        return category in listOf(AppCategory.CALENDAR, AppCategory.NOTES, AppCategory.BROWSER)
    }
    
    /**
     * Check if entertainment app
     */
    private fun isEntertainmentApp(packageName: String): Boolean {
        val category = getAppCategory(packageName)
        return category == AppCategory.MUSIC || packageName.contains("netflix") || 
               packageName.contains("youtube") || packageName.contains("game")
    }
    
    /**
     * Check if communication app
     */
    private fun isCommunicationApp(packageName: String): Boolean {
        val category = getAppCategory(packageName)
        return category == AppCategory.MESSAGING
    }
    
    /**
     * Record app usage
     */
    fun recordAppUsage(packageName: String, duration: Long) {
        val record = AppUsageRecord(
            packageName = packageName,
            timestamp = System.currentTimeMillis(),
            duration = duration
        )
        
        appUsageHistory.add(record)
        if (appUsageHistory.size > MAX_APP_HISTORY) {
            appUsageHistory.removeAt(0)
        }
    }
    
    /**
     * Update app category
     */
    fun updateAppCategory(packageName: String, category: AppCategory) {
        appCategories[packageName] = category
    }
    
    /**
     * Update app intelligence
     */
    fun updateAppIntelligence(packageName: String, intelligence: AppIntelligence) {
        appIntelligence[packageName] = intelligence
    }
}

/**
 * App usage record
 */
data class AppUsageRecord(
    val packageName: String,
    val timestamp: Long,
    val duration: Long
)

/**
 * App usage pattern
 */
data class AppUsagePattern(
    val type: String,
    val apps: List<AppUsageInfo>,
    val confidence: Float,
    val metadata: Map<String, Any>
)

/**
 * App usage info
 */
data class AppUsageInfo(
    val packageName: String,
    val usageCount: Int,
    val category: AppCategory,
    val lastUsed: Long
)

/**
 * App switch
 */
data class AppSwitch(
    val from: String,
    val to: String,
    val duration: Long
)

/**
 * App recommendation
 */
data class AppRecommendation(
    val recommendation: String,
    val category: String,
    val confidence: Float,
    val suggestedApps: List<String>
)

/**
 * App insight
 */
data class AppInsight(
    val type: String,
    val title: String,
    val description: String,
    val confidence: Float,
    val recommendations: List<String>
)

/**
 * App prediction
 */
data class AppPrediction(
    val app: String,
    val probability: Float,
    val reason: String,
    val confidence: Float
)

/**
 * App intelligence
 */
data class AppIntelligence(
    val isProductive: Boolean,
    val isEntertainment: Boolean,
    val isCommunication: Boolean,
    val usageFrequency: Int,
    val averageSessionDuration: Long,
    val lastUsed: Long
) {
    companion object {
        fun createDefault() = AppIntelligence(
            isProductive = false,
            isEntertainment = false,
            isCommunication = false,
            usageFrequency = 0,
            averageSessionDuration = 0L,
            lastUsed = 0L
        )
    }
}

/**
 * App usage intelligence result
 */
data class AppUsageIntelligenceResult(
    val currentApp: String?,
    val appUsagePatterns: List<AppUsagePattern>,
    val appContext: AppContext,
    val appRecommendations: List<AppRecommendation>,
    val appInsights: List<AppInsight>,
    val appPredictions: List<AppPrediction>,
    val appUsageHistory: List<AppUsageRecord>,
    val lastAnalysisTime: Long
) {
    companion object {
        fun createEmpty() = AppUsageIntelligenceResult(
            currentApp = null,
            appUsagePatterns = emptyList(),
            appContext = AppContext.createUnknown(),
            appRecommendations = emptyList(),
            appInsights = emptyList(),
            appPredictions = emptyList(),
            appUsageHistory = emptyList(),
            lastAnalysisTime = System.currentTimeMillis()
        )
    }
}
