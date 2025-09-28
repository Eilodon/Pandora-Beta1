package com.pandora.core.ai.context

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Time Context Enhancer
 * Provides advanced time-based context understanding
 */
@Singleton
class TimeContextEnhancer @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "TimeContextEnhancer"
        private const val PREFS_NAME = "time_context_prefs"
        private const val KEY_WORKING_HOURS_START = "working_hours_start"
        private const val KEY_WORKING_HOURS_END = "working_hours_end"
        private const val KEY_TIMEZONE = "timezone"
        private const val KEY_WORK_DAYS = "work_days"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Get enhanced time context
     */
    fun getEnhancedTimeContext(): Flow<EnhancedTimeContext> = flow {
        try {
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = currentTime
            
            val timezone = getCurrentTimezone()
            val workingHours = getWorkingHours()
            val workDays = getWorkDays()
            val timePatterns = analyzeTimePatterns()
            val timeBasedSuggestions = getTimeBasedSuggestions(calendar)
            
            val enhancedContext = EnhancedTimeContext(
                timestamp = currentTime,
                timezone = timezone,
                workingHours = workingHours,
                workDays = workDays,
                timePatterns = timePatterns,
                timeBasedSuggestions = timeBasedSuggestions,
                isWorkingTime = isWorkingTime(calendar, workingHours, workDays),
                isBreakTime = isBreakTime(calendar, workingHours),
                isRushHour = isRushHour(calendar),
                isQuietTime = isQuietTime(calendar),
                timeOfDay = getTimeOfDay(calendar),
                dayType = getDayType(calendar),
                season = getSeason(calendar),
                isHoliday = isHoliday(calendar),
                timeUntilNextEvent = getTimeUntilNextEvent(calendar),
                timeSinceLastActivity = getTimeSinceLastActivity()
            )
            
            emit(enhancedContext)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting enhanced time context", e)
            emit(EnhancedTimeContext.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get current timezone
     */
    private fun getCurrentTimezone(): String {
        return prefs.getString(KEY_TIMEZONE, TimeZone.getDefault().id) ?: TimeZone.getDefault().id
    }
    
    /**
     * Get working hours
     */
    private fun getWorkingHours(): WorkingHours {
        val startHour = prefs.getInt(KEY_WORKING_HOURS_START, 9)
        val endHour = prefs.getInt(KEY_WORKING_HOURS_END, 17)
        return WorkingHours(startHour, endHour)
    }
    
    /**
     * Get work days
     */
    private fun getWorkDays(): Set<Int> {
        val workDaysString = prefs.getString(KEY_WORK_DAYS, "1,2,3,4,5") ?: "1,2,3,4,5"
        return workDaysString.split(",").map { it.toInt() }.toSet()
    }
    
    /**
     * Analyze time patterns
     */
    private fun analyzeTimePatterns(): TimePatterns {
        // This would typically analyze historical data
        // For now, return placeholder patterns
        return TimePatterns(
            mostActiveHour = 14, // 2 PM
            leastActiveHour = 3, // 3 AM
            averageSessionDuration = 25 * 60 * 1000L, // 25 minutes
            peakProductivityHours = listOf(9, 10, 14, 15),
            lowProductivityHours = listOf(12, 13, 18, 19),
            weekendActivityLevel = 0.3f,
            weekdayActivityLevel = 0.8f
        )
    }
    
    /**
     * Get time-based suggestions
     */
    private fun getTimeBasedSuggestions(calendar: Calendar): List<TimeBasedSuggestion> {
        val suggestions = mutableListOf<TimeBasedSuggestion>()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        when {
            hour in 6..8 -> {
                suggestions.add(TimeBasedSuggestion(
                    "Good morning! Time for your daily planning",
                    "morning_routine",
                    0.9f
                ))
            }
            hour in 9..11 -> {
                suggestions.add(TimeBasedSuggestion(
                    "Focus time - tackle your most important tasks",
                    "focus_time",
                    0.8f
                ))
            }
            hour in 12..13 -> {
                suggestions.add(TimeBasedSuggestion(
                    "Lunch break - time to recharge",
                    "lunch_break",
                    0.7f
                ))
            }
            hour in 14..16 -> {
                suggestions.add(TimeBasedSuggestion(
                    "Afternoon productivity - continue your work",
                    "afternoon_work",
                    0.8f
                ))
            }
            hour in 17..19 -> {
                suggestions.add(TimeBasedSuggestion(
                    "End of workday - time to wrap up",
                    "end_of_day",
                    0.6f
                ))
            }
            hour in 20..22 -> {
                suggestions.add(TimeBasedSuggestion(
                    "Evening wind-down - relax and prepare for tomorrow",
                    "evening_wind_down",
                    0.5f
                ))
            }
            else -> {
                suggestions.add(TimeBasedSuggestion(
                    "Late night - consider getting some rest",
                    "late_night",
                    0.3f
                ))
            }
        }
        
        // Weekend suggestions
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            suggestions.add(TimeBasedSuggestion(
                "Weekend time - enjoy your free time",
                "weekend",
                0.7f
            ))
        }
        
        return suggestions
    }
    
    /**
     * Check if it's working time
     */
    private fun isWorkingTime(calendar: Calendar, workingHours: WorkingHours, workDays: Set<Int>): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        return dayOfWeek in workDays && hour in workingHours.startHour..workingHours.endHour
    }
    
    /**
     * Check if it's break time
     */
    private fun isBreakTime(calendar: Calendar, workingHours: WorkingHours): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in 12..13 // Lunch break
    }
    
    /**
     * Check if it's rush hour
     */
    private fun isRushHour(calendar: Calendar): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in 7..9 || hour in 17..19
    }
    
    /**
     * Check if it's quiet time
     */
    private fun isQuietTime(calendar: Calendar): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in 22..6
    }
    
    /**
     * Get time of day
     */
    private fun getTimeOfDay(calendar: Calendar): TimeOfDay {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 6..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            in 18..22 -> TimeOfDay.EVENING
            else -> TimeOfDay.NIGHT
        }
    }
    
    /**
     * Get day type
     */
    private fun getDayType(calendar: Calendar): DayType {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return when (dayOfWeek) {
            Calendar.SATURDAY, Calendar.SUNDAY -> DayType.WEEKEND
            Calendar.MONDAY -> DayType.MONDAY
            Calendar.FRIDAY -> DayType.FRIDAY
            else -> DayType.WEEKDAY
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
        
        return when (month) {
            Calendar.DECEMBER -> day == 25 // Christmas
            Calendar.JANUARY -> day == 1 // New Year
            Calendar.JULY -> day == 4 // Independence Day (US)
            else -> false
        }
    }
    
    /**
     * Get time until next event
     */
    private fun getTimeUntilNextEvent(calendar: Calendar): Long {
        // This would typically check calendar events
        // For now, return placeholder
        return 0L
    }
    
    /**
     * Get time since last activity
     */
    private fun getTimeSinceLastActivity(): Long {
        // This would typically check last user activity
        // For now, return placeholder
        return 0L
    }
    
    /**
     * Update working hours
     */
    fun updateWorkingHours(startHour: Int, endHour: Int) {
        prefs.edit()
            .putInt(KEY_WORKING_HOURS_START, startHour)
            .putInt(KEY_WORKING_HOURS_END, endHour)
            .apply()
    }
    
    /**
     * Update work days
     */
    fun updateWorkDays(workDays: Set<Int>) {
        val workDaysString = workDays.joinToString(",")
        prefs.edit()
            .putString(KEY_WORK_DAYS, workDaysString)
            .apply()
    }
    
    /**
     * Update timezone
     */
    fun updateTimezone(timezone: String) {
        prefs.edit()
            .putString(KEY_TIMEZONE, timezone)
            .apply()
    }
}

/**
 * Enhanced time context
 */
data class EnhancedTimeContext(
    val timestamp: Long,
    val timezone: String,
    val workingHours: WorkingHours,
    val workDays: Set<Int>,
    val timePatterns: TimePatterns,
    val timeBasedSuggestions: List<TimeBasedSuggestion>,
    val isWorkingTime: Boolean,
    val isBreakTime: Boolean,
    val isRushHour: Boolean,
    val isQuietTime: Boolean,
    val timeOfDay: TimeOfDay,
    val dayType: DayType,
    val season: Season,
    val isHoliday: Boolean,
    val timeUntilNextEvent: Long,
    val timeSinceLastActivity: Long
) {
    companion object {
        fun createEmpty() = EnhancedTimeContext(
            timestamp = System.currentTimeMillis(),
            timezone = TimeZone.getDefault().id,
            workingHours = WorkingHours(9, 17),
            workDays = setOf(1, 2, 3, 4, 5),
            timePatterns = TimePatterns.createEmpty(),
            timeBasedSuggestions = emptyList(),
            isWorkingTime = false,
            isBreakTime = false,
            isRushHour = false,
            isQuietTime = false,
            timeOfDay = TimeOfDay.AFTERNOON,
            dayType = DayType.WEEKDAY,
            season = Season.SUMMER,
            isHoliday = false,
            timeUntilNextEvent = 0L,
            timeSinceLastActivity = 0L
        )
    }
}

/**
 * Working hours
 */
data class WorkingHours(
    val startHour: Int,
    val endHour: Int
)

/**
 * Time patterns
 */
data class TimePatterns(
    val mostActiveHour: Int,
    val leastActiveHour: Int,
    val averageSessionDuration: Long,
    val peakProductivityHours: List<Int>,
    val lowProductivityHours: List<Int>,
    val weekendActivityLevel: Float,
    val weekdayActivityLevel: Float
) {
    companion object {
        fun createEmpty() = TimePatterns(
            mostActiveHour = 14,
            leastActiveHour = 3,
            averageSessionDuration = 25 * 60 * 1000L,
            peakProductivityHours = listOf(9, 10, 14, 15),
            lowProductivityHours = listOf(12, 13, 18, 19),
            weekendActivityLevel = 0.3f,
            weekdayActivityLevel = 0.8f
        )
    }
}

/**
 * Time-based suggestion
 */
data class TimeBasedSuggestion(
    val suggestion: String,
    val category: String,
    val confidence: Float
)

/**
 * Day type
 */
enum class DayType {
    WEEKDAY, MONDAY, FRIDAY, WEEKEND
}
