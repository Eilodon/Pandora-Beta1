package com.pandora.core.ai.context

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.MemoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.text.SimpleDateFormat
import java.util.*

/**
 * Context Awareness System for Neural Keyboard
 * Provides intelligent context understanding and adaptation
 */
@Singleton
class ContextAwareness @Inject constructor(
    private val context: Context,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "ContextAwareness"
        private const val LOCATION_UPDATE_INTERVAL = 300000L // 5 minutes
        private const val MAX_RECENT_ACTIVITIES = 50
    }
    
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val packageManager = context.packageManager
    private val recentActivities = mutableListOf<ActivityRecord>()
    private val contextHistory = mutableListOf<ContextSnapshot>()
    
    /**
     * Get current context
     */
    fun getCurrentContext(): Flow<ContextSnapshot> = flow {
        try {
            val timestamp = System.currentTimeMillis()
            val timeContext = getTimeContext(timestamp)
            val locationContext = getLocationContext()
            val appContext = getAppContext()
            val userContext = getUserContext()
            val deviceContext = getDeviceContext()
            
            val contextSnapshot = ContextSnapshot(
                timestamp = timestamp,
                timeContext = timeContext,
                locationContext = locationContext,
                appContext = appContext,
                userContext = userContext,
                deviceContext = deviceContext,
                confidence = calculateContextConfidence(timeContext, locationContext, appContext)
            )
            
            // Store context history
            contextHistory.add(contextSnapshot)
            if (contextHistory.size > 100) {
                contextHistory.removeAt(0)
            }
            
            emit(contextSnapshot)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting current context", e)
            emit(ContextSnapshot.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get time context
     */
    private fun getTimeContext(timestamp: Long): TimeContext {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        
        return TimeContext(
            hour = hour,
            dayOfWeek = dayOfWeek,
            isWeekend = isWeekend,
            timeOfDay = when (hour) {
                in 6..11 -> TimeOfDay.MORNING
                in 12..17 -> TimeOfDay.AFTERNOON
                in 18..22 -> TimeOfDay.EVENING
                else -> TimeOfDay.NIGHT
            },
            season = getSeason(calendar),
            isHoliday = isHoliday(calendar)
        )
    }
    
    /**
     * Get location context
     */
    private fun getLocationContext(): LocationContext {
        return try {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                
                @Suppress("MissingPermission")
                val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastKnownLocation != null) {
                    LocationContext(
                        latitude = lastKnownLocation.latitude,
                        longitude = lastKnownLocation.longitude,
                        accuracy = lastKnownLocation.accuracy,
                        locationType = determineLocationType(lastKnownLocation),
                        isMoving = false, // Would need additional logic to determine movement
                        speed = lastKnownLocation.speed
                    )
                } else {
                    LocationContext.createUnknown()
                }
            } else {
                LocationContext.createUnknown()
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting location context", e)
            LocationContext.createUnknown()
        }
    }
    
    /**
     * Get app context
     */
    private fun getAppContext(): AppContext {
        return try {
            val runningApps = getRunningApps()
            val currentApp = runningApps.firstOrNull()
            
            AppContext(
                currentApp = currentApp,
                runningApps = runningApps,
                appCategory = determineAppCategory(currentApp),
                isMultitasking = runningApps.size > 1,
                appUsageTime = getAppUsageTime(currentApp)
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting app context", e)
            AppContext.createUnknown()
        }
    }
    
    /**
     * Get user context
     */
    private fun getUserContext(): UserContext {
        val recentActivity = recentActivities.takeLast(10)
        val activityLevel = calculateActivityLevel(recentActivity)
        val focusLevel = calculateFocusLevel(recentActivity)
        
        return UserContext(
            activityLevel = activityLevel,
            focusLevel = focusLevel,
            isTyping = isUserTyping(),
            isScrolling = isUserScrolling(),
            interactionPattern = analyzeInteractionPattern(recentActivity),
            stressLevel = estimateStressLevel(recentActivity)
        )
    }
    
    /**
     * Get device context
     */
    private fun getDeviceContext(): DeviceContext {
        return DeviceContext(
            batteryLevel = getBatteryLevel(),
            isCharging = isDeviceCharging(),
            isConnected = isDeviceConnected(),
            screenBrightness = getScreenBrightness(),
            isInDarkMode = isInDarkMode(),
            deviceOrientation = getDeviceOrientation(),
            availableMemory = getAvailableMemory(),
            isLowMemory = isLowMemory()
        )
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
     * Determine location type
     */
    private fun determineLocationType(location: Location): LocationType {
        // This would typically use reverse geocoding or a location database
        // For now, return a placeholder
        return LocationType.UNKNOWN
    }
    
    /**
     * Determine app category
     */
    private fun determineAppCategory(packageName: String?): AppCategory {
        if (packageName == null) return AppCategory.UNKNOWN
        
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
     * Get app usage time
     */
    private fun getAppUsageTime(packageName: String?): Long {
        // This would typically use UsageStatsManager
        // For now, return a placeholder
        return 0L
    }
    
    /**
     * Calculate activity level
     */
    private fun calculateActivityLevel(activities: List<ActivityRecord>): ActivityLevel {
        if (activities.isEmpty()) return ActivityLevel.IDLE
        
        val recentActivityCount = activities.count { 
            System.currentTimeMillis() - it.timestamp < 300000L // 5 minutes
        }
        
        return when {
            recentActivityCount > 10 -> ActivityLevel.HIGH
            recentActivityCount > 5 -> ActivityLevel.MEDIUM
            recentActivityCount > 0 -> ActivityLevel.LOW
            else -> ActivityLevel.IDLE
        }
    }
    
    /**
     * Calculate focus level
     */
    private fun calculateFocusLevel(activities: List<ActivityRecord>): FocusLevel {
        if (activities.isEmpty()) return FocusLevel.UNFOCUSED
        
        val appSwitches = activities.count { it.type == ActivityType.APP_SWITCH }
        val timeSpent = activities.sumOf { it.duration }
        
        return when {
            appSwitches < 3 && timeSpent > 300000L -> FocusLevel.HIGH
            appSwitches < 5 && timeSpent > 60000L -> FocusLevel.MEDIUM
            else -> FocusLevel.LOW
        }
    }
    
    /**
     * Check if user is typing
     */
    private fun isUserTyping(): Boolean {
        val recentTyping = recentActivities.any { 
            it.type == ActivityType.TYPING && 
            System.currentTimeMillis() - it.timestamp < 5000L // 5 seconds
        }
        return recentTyping
    }
    
    /**
     * Check if user is scrolling
     */
    private fun isUserScrolling(): Boolean {
        val recentScrolling = recentActivities.any { 
            it.type == ActivityType.SCROLLING && 
            System.currentTimeMillis() - it.timestamp < 2000L // 2 seconds
        }
        return recentScrolling
    }
    
    /**
     * Analyze interaction pattern
     */
    private fun analyzeInteractionPattern(activities: List<ActivityRecord>): InteractionPattern {
        val typingCount = activities.count { it.type == ActivityType.TYPING }
        val scrollingCount = activities.count { it.type == ActivityType.SCROLLING }
        val appSwitchCount = activities.count { it.type == ActivityType.APP_SWITCH }
        
        return when {
            typingCount > scrollingCount && typingCount > appSwitchCount -> InteractionPattern.TYPING_HEAVY
            scrollingCount > typingCount && scrollingCount > appSwitchCount -> InteractionPattern.SCROLLING_HEAVY
            appSwitchCount > typingCount && appSwitchCount > scrollingCount -> InteractionPattern.MULTITASKING
            else -> InteractionPattern.BALANCED
        }
    }
    
    /**
     * Estimate stress level
     */
    private fun estimateStressLevel(activities: List<ActivityRecord>): StressLevel {
        val rapidSwitches = activities.count { 
            it.type == ActivityType.APP_SWITCH && it.duration < 5000L // Less than 5 seconds
        }
        val typingErrors = activities.count { 
            it.type == ActivityType.TYPING_ERROR 
        }
        
        return when {
            rapidSwitches > 5 || typingErrors > 3 -> StressLevel.HIGH
            rapidSwitches > 2 || typingErrors > 1 -> StressLevel.MEDIUM
            else -> StressLevel.LOW
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
            50 // Default to 50%
        }
    }
    
    /**
     * Check if device is charging
     */
    private fun isDeviceCharging(): Boolean {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val status = batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_STATUS)
            status == android.os.BatteryManager.BATTERY_STATUS_CHARGING
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if device is connected
     */
    private fun isDeviceConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        @Suppress("MissingPermission")
        val activeNetwork = connectivityManager.activeNetwork
        return activeNetwork != null
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
            (brightness * 100 / 255) // Convert to percentage
        } catch (e: Exception) {
            50 // Default to 50%
        }
    }
    
    /**
     * Check if in dark mode
     */
    private fun isInDarkMode(): Boolean {
        return try {
            val nightMode = context.resources.configuration.uiMode and 
                           android.content.res.Configuration.UI_MODE_NIGHT_MASK
            nightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get device orientation
     */
    private fun getDeviceOrientation(): DeviceOrientation {
        val orientation = context.resources.configuration.orientation
        return when (orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> DeviceOrientation.PORTRAIT
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> DeviceOrientation.LANDSCAPE
            else -> DeviceOrientation.UNKNOWN
        }
    }
    
    /**
     * Get available memory
     */
    private fun getAvailableMemory(): Long {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.availMem
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * Check if low memory
     */
    private fun isLowMemory(): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memoryInfo = android.app.ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            memoryInfo.lowMemory
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get season
     */
    private fun getSeason(calendar: Calendar): Season {
        val month = calendar.get(Calendar.MONTH)
        return when (month) {
            in 2..4 -> Season.SPRING
            in 5..7 -> Season.SUMMER
            in 8..10 -> Season.AUTUMN
            else -> Season.WINTER
        }
    }
    
    /**
     * Check if holiday
     */
    private fun isHoliday(calendar: Calendar): Boolean {
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Simple holiday check - in production, use a proper holiday API
        return when (month) {
            Calendar.DECEMBER -> day == 25 // Christmas
            Calendar.JANUARY -> day == 1 // New Year
            Calendar.JULY -> day == 4 // Independence Day (US)
            else -> false
        }
    }
    
    /**
     * Calculate context confidence
     */
    private fun calculateContextConfidence(
        timeContext: TimeContext,
        locationContext: LocationContext,
        appContext: AppContext
    ): Float {
        var confidence = 0.5f
        
        // Time context is always available
        confidence += 0.2f
        
        // Location context confidence
        if (locationContext.latitude != 0.0 && locationContext.longitude != 0.0) {
            confidence += 0.2f
        }
        
        // App context confidence
        if (appContext.currentApp != null) {
            confidence += 0.1f
        }
        
        return confidence.coerceIn(0f, 1f)
    }
    
    /**
     * Record user activity
     */
    fun recordActivity(type: ActivityType, duration: Long = 0L) {
        val activity = ActivityRecord(
            type = type,
            timestamp = System.currentTimeMillis(),
            duration = duration
        )
        
        recentActivities.add(activity)
        if (recentActivities.size > MAX_RECENT_ACTIVITIES) {
            recentActivities.removeAt(0)
        }
    }
}

/**
 * Context snapshot
 */
data class ContextSnapshot(
    val timestamp: Long,
    val timeContext: TimeContext,
    val locationContext: LocationContext,
    val appContext: AppContext,
    val userContext: UserContext,
    val deviceContext: DeviceContext,
    val confidence: Float
) {
    companion object {
        fun createEmpty() = ContextSnapshot(
            timestamp = System.currentTimeMillis(),
            timeContext = TimeContext.createEmpty(),
            locationContext = LocationContext.createUnknown(),
            appContext = AppContext.createUnknown(),
            userContext = UserContext.createEmpty(),
            deviceContext = DeviceContext.createEmpty(),
            confidence = 0f
        )
    }
}

/**
 * Time context
 */
data class TimeContext(
    val hour: Int,
    val dayOfWeek: Int,
    val isWeekend: Boolean,
    val timeOfDay: TimeOfDay,
    val season: Season,
    val isHoliday: Boolean
) {
    companion object {
        fun createEmpty() = TimeContext(
            hour = 12,
            dayOfWeek = 1,
            isWeekend = false,
            timeOfDay = TimeOfDay.AFTERNOON,
            season = Season.SUMMER,
            isHoliday = false
        )
    }
}

/**
 * Location context
 */
data class LocationContext(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val locationType: LocationType,
    val isMoving: Boolean,
    val speed: Float
) {
    companion object {
        fun createUnknown() = LocationContext(
            latitude = 0.0,
            longitude = 0.0,
            accuracy = 0f,
            locationType = LocationType.UNKNOWN,
            isMoving = false,
            speed = 0f
        )
    }
}

/**
 * App context
 */
data class AppContext(
    val currentApp: String?,
    val runningApps: List<String>,
    val appCategory: AppCategory,
    val isMultitasking: Boolean,
    val appUsageTime: Long
) {
    companion object {
        fun createUnknown() = AppContext(
            currentApp = null,
            runningApps = emptyList(),
            appCategory = AppCategory.UNKNOWN,
            isMultitasking = false,
            appUsageTime = 0L
        )
    }
}

/**
 * User context
 */
data class UserContext(
    val activityLevel: ActivityLevel,
    val focusLevel: FocusLevel,
    val isTyping: Boolean,
    val isScrolling: Boolean,
    val interactionPattern: InteractionPattern,
    val stressLevel: StressLevel
) {
    companion object {
        fun createEmpty() = UserContext(
            activityLevel = ActivityLevel.IDLE,
            focusLevel = FocusLevel.UNFOCUSED,
            isTyping = false,
            isScrolling = false,
            interactionPattern = InteractionPattern.BALANCED,
            stressLevel = StressLevel.LOW
        )
    }
}

/**
 * Device context
 */
data class DeviceContext(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val isConnected: Boolean,
    val screenBrightness: Int,
    val isInDarkMode: Boolean,
    val deviceOrientation: DeviceOrientation,
    val availableMemory: Long,
    val isLowMemory: Boolean
) {
    companion object {
        fun createEmpty() = DeviceContext(
            batteryLevel = 50,
            isCharging = false,
            isConnected = true,
            screenBrightness = 50,
            isInDarkMode = false,
            deviceOrientation = DeviceOrientation.PORTRAIT,
            availableMemory = 0L,
            isLowMemory = false
        )
    }
}

/**
 * Activity record
 */
data class ActivityRecord(
    val type: ActivityType,
    val timestamp: Long,
    val duration: Long
)

/**
 * Enums
 */
enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }
enum class Season { SPRING, SUMMER, AUTUMN, WINTER }
enum class LocationType { HOME, WORK, TRAVEL, UNKNOWN }
enum class AppCategory { MESSAGING, CALENDAR, NOTES, BROWSER, MUSIC, UNKNOWN }
enum class ActivityLevel { IDLE, LOW, MEDIUM, HIGH }
enum class FocusLevel { UNFOCUSED, LOW, MEDIUM, HIGH }
enum class InteractionPattern { TYPING_HEAVY, SCROLLING_HEAVY, MULTITASKING, BALANCED }
enum class StressLevel { LOW, MEDIUM, HIGH }
enum class DeviceOrientation { PORTRAIT, LANDSCAPE, UNKNOWN }
enum class ActivityType { TYPING, SCROLLING, APP_SWITCH, TYPING_ERROR }
