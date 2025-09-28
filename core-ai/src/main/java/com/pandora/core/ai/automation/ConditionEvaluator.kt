package com.pandora.core.ai.automation

import android.content.Context
import android.util.Log
import com.pandora.core.ai.context.EnhancedContextIntegration
import com.pandora.core.ai.context.ComprehensiveContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

/**
 * Condition Evaluator
 * Evaluates complex conditions for workflow automation
 */
@Singleton
class ConditionEvaluator @Inject constructor(
    private val context: Context,
    private val enhancedContextIntegration: EnhancedContextIntegration
) {
    
    companion object {
        private const val TAG = "ConditionEvaluator"
    }
    
    /**
     * Evaluate conditions
     */
    suspend fun evaluateConditions(
        conditions: List<ConditionDefinition>,
        context: MutableMap<String, Any>
    ): Boolean {
        if (conditions.isEmpty()) return true
        
        // Evaluate all conditions with AND logic
        for (condition in conditions) {
            val result = evaluateCondition(condition, context)
            if (!result) {
                Log.d(TAG, "Condition failed: ${condition.field} ${condition.operator} ${condition.value}")
                return false
            }
        }
        
        return true
    }
    
    /**
     * Evaluate single condition
     */
    private suspend fun evaluateCondition(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        return try {
            when (condition.type) {
                ConditionType.TEXT_CONTAINS -> evaluateTextContains(condition, context)
                ConditionType.TEXT_EQUALS -> evaluateTextEquals(condition, context)
                ConditionType.TIME_RANGE -> evaluateTimeRange(condition, context)
                ConditionType.LOCATION_WITHIN -> evaluateLocationWithin(condition, context)
                ConditionType.APP_RUNNING -> evaluateAppRunning(condition, context)
                ConditionType.VARIABLE_EQUALS -> evaluateVariableEquals(condition, context)
                ConditionType.VARIABLE_GREATER_THAN -> evaluateVariableGreaterThan(condition, context)
                ConditionType.VARIABLE_LESS_THAN -> evaluateVariableLessThan(condition, context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating condition: ${condition.field}", e)
            false
        }
    }
    
    /**
     * Evaluate text contains condition
     */
    private fun evaluateTextContains(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val fieldValue = getFieldValue(condition.field, context) as? String ?: ""
        val targetValue = condition.value as? String ?: ""
        
        return fieldValue.contains(targetValue, ignoreCase = true)
    }
    
    /**
     * Evaluate text equals condition
     */
    private fun evaluateTextEquals(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val fieldValue = getFieldValue(condition.field, context) as? String ?: ""
        val targetValue = condition.value as? String ?: ""
        
        return fieldValue.equals(targetValue, ignoreCase = true)
    }
    
    /**
     * Evaluate time range condition
     */
    private fun evaluateTimeRange(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        
        val timeRange = condition.value as? Map<String, Any> ?: return false
        val startTime = timeRange["start"] as? String ?: return false
        val endTime = timeRange["end"] as? String ?: return false
        
        val startMinutes = parseTimeToMinutes(startTime)
        val endMinutes = parseTimeToMinutes(endTime)
        
        return if (startMinutes <= endMinutes) {
            currentTimeInMinutes in startMinutes..endMinutes
        } else {
            // Handle overnight range (e.g., 22:00 to 06:00)
            currentTimeInMinutes >= startMinutes || currentTimeInMinutes <= endMinutes
        }
    }
    
    /**
     * Evaluate location within condition
     */
    private suspend fun evaluateLocationWithin(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        try {
            val comprehensiveContext = enhancedContextIntegration.getComprehensiveContext().first()
            val currentLocation = comprehensiveContext.locationContext.currentLocation
            
            if (currentLocation == null) return false
            
            val targetLat = condition.value as? Double ?: return false
            val radius = 100.0 // meters - default radius
            
            val distance = calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude,
                targetLat,
                0.0 // default longitude
            )
            
            return distance <= radius
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating location condition", e)
            return false
        }
    }
    
    /**
     * Evaluate app running condition
     */
    private fun evaluateAppRunning(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val targetPackage = condition.value as? String ?: return false
        val runningApps = getRunningApps()
        
        return runningApps.contains(targetPackage)
    }
    
    /**
     * Evaluate variable equals condition
     */
    private fun evaluateVariableEquals(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val fieldValue = getFieldValue(condition.field, context)
        val targetValue = condition.value
        
        return fieldValue == targetValue
    }
    
    /**
     * Evaluate variable greater than condition
     */
    private fun evaluateVariableGreaterThan(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val fieldValue = getFieldValue(condition.field, context)
        val targetValue = condition.value
        
        return when {
            fieldValue is Number && targetValue is Number -> {
                fieldValue.toDouble() > targetValue.toDouble()
            }
            fieldValue is String && targetValue is String -> {
                fieldValue.compareTo(targetValue) > 0
            }
            else -> false
        }
    }
    
    /**
     * Evaluate variable less than condition
     */
    private fun evaluateVariableLessThan(
        condition: ConditionDefinition,
        context: MutableMap<String, Any>
    ): Boolean {
        val fieldValue = getFieldValue(condition.field, context)
        val targetValue = condition.value
        
        return when {
            fieldValue is Number && targetValue is Number -> {
                fieldValue.toDouble() < targetValue.toDouble()
            }
            fieldValue is String && targetValue is String -> {
                fieldValue.compareTo(targetValue) < 0
            }
            else -> false
        }
    }
    
    /**
     * Get field value from context
     */
    private fun getFieldValue(field: String, context: MutableMap<String, Any>): Any? {
        return when {
            field.startsWith("input.") -> {
                val key = field.substring(6)
                context["input.$key"]
            }
            field.startsWith("system_context.") -> {
                val key = field.substring(15)
                val systemContext = context["system_context"] as? ComprehensiveContext
                getSystemContextValue(systemContext, key)
            }
            field.startsWith("timestamp") -> {
                System.currentTimeMillis()
            }
            field.startsWith("time.") -> {
                val key = field.substring(5)
                getTimeValue(key)
            }
            field.startsWith("location.") -> {
                val key = field.substring(9)
                getLocationValue(key, context)
            }
            else -> {
                context[field]
            }
        }
    }
    
    /**
     * Get system context value
     */
    private fun getSystemContextValue(
        systemContext: ComprehensiveContext?,
        key: String
    ): Any? {
        if (systemContext == null) return null
        
        return when (key) {
            "time.hour" -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = systemContext.timeContext.timestamp
                calendar.get(Calendar.HOUR_OF_DAY)
            }
            "time.day_of_week" -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = systemContext.timeContext.timestamp
                calendar.get(Calendar.DAY_OF_WEEK)
            }
            "time.is_weekend" -> systemContext.timeContext.dayType.name == "WEEKEND"
            "location.latitude" -> systemContext.locationContext.currentLocation?.latitude
            "location.longitude" -> systemContext.locationContext.currentLocation?.longitude
            "app.current" -> systemContext.appUsage.currentApp
            "activity.type" -> systemContext.activityAnalysis.currentActivity.type.name
            else -> null
        }
    }
    
    /**
     * Get time value
     */
    private fun getTimeValue(key: String): Any? {
        val calendar = Calendar.getInstance()
        
        return when (key) {
            "hour" -> calendar.get(Calendar.HOUR_OF_DAY)
            "minute" -> calendar.get(Calendar.MINUTE)
            "day_of_week" -> calendar.get(Calendar.DAY_OF_WEEK)
            "day_of_month" -> calendar.get(Calendar.DAY_OF_MONTH)
            "month" -> calendar.get(Calendar.MONTH)
            "year" -> calendar.get(Calendar.YEAR)
            "is_weekend" -> {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            }
            "is_workday" -> {
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            }
            else -> null
        }
    }
    
    /**
     * Get location value
     */
    private fun getLocationValue(key: String, context: MutableMap<String, Any>): Any? {
        return try {
            val systemContext = context["system_context"] as? ComprehensiveContext
            val location = systemContext?.locationContext?.currentLocation
            
            when (key) {
                "latitude" -> location?.latitude
                "longitude" -> location?.longitude
                "accuracy" -> location?.accuracy
                "speed" -> location?.speed
                "is_moving" -> location?.hasSpeed() == true && location.speed > 0
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parse time string to minutes
     */
    private fun parseTimeToMinutes(timeString: String): Int {
        val parts = timeString.split(":")
        val hour = parts[0].toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return hour * 60 + minute
    }
    
    /**
     * Calculate distance between two points
     */
    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * Get running apps
     */
    private fun getRunningApps(): List<String> {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningTasks = activityManager.getRunningTasks(10)
            runningTasks.mapNotNull { it.topActivity?.packageName }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Evaluate complex condition expression
     */
    suspend fun evaluateExpression(
        expression: String,
        context: MutableMap<String, Any>
    ): Boolean {
        // Simple expression evaluator for complex conditions
        // Format: "condition1 AND condition2 OR condition3"
        
        val tokens = expression.split("\\s+".toRegex())
        var result = true
        var currentOperator = "AND"
        
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            
            when (token.uppercase()) {
                "AND" -> {
                    currentOperator = "AND"
                    i++
                }
                "OR" -> {
                    currentOperator = "OR"
                    i++
                }
                "NOT" -> {
                    // Handle NOT operator
                    i++
                    if (i < tokens.size) {
                        val condition = parseConditionFromToken(tokens[i])
                        val conditionResult = if (condition != null) {
                            evaluateCondition(condition, context)
                        } else {
                            false
                        }
                        
                        result = if (currentOperator == "AND") {
                            result && !conditionResult
                        } else {
                            result || !conditionResult
                        }
                        i++
                    }
                }
                else -> {
                    // Parse condition from token
                    val condition = parseConditionFromToken(token)
                    val conditionResult = if (condition != null) {
                        evaluateCondition(condition, context)
                    } else {
                        false
                    }
                    
                    result = if (currentOperator == "AND") {
                        result && conditionResult
                    } else {
                        result || conditionResult
                    }
                    i++
                }
            }
        }
        
        return result
    }
    
    /**
     * Parse condition from token
     */
    private fun parseConditionFromToken(token: String): ConditionDefinition? {
        // Simple condition parser
        // Format: "field:operator:value"
        val parts = token.split(":")
        if (parts.size != 3) return null
        
        val field = parts[0]
        val operator = parts[1]
        val value = parts[2]
        
        val conditionType = when (operator) {
            "contains" -> ConditionType.TEXT_CONTAINS
            "equals" -> ConditionType.TEXT_EQUALS
            ">" -> ConditionType.VARIABLE_GREATER_THAN
            "<" -> ConditionType.VARIABLE_LESS_THAN
            "==" -> ConditionType.VARIABLE_EQUALS
            else -> return null
        }
        
        return ConditionDefinition(
            type = conditionType,
            field = field,
            operator = operator,
            value = value
        )
    }
}
