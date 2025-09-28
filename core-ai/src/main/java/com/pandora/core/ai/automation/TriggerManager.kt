package com.pandora.core.ai.automation

import android.annotation.SuppressLint
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.util.Log
import com.pandora.core.ai.context.EnhancedContextIntegration
import com.pandora.core.ai.context.ComprehensiveContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*
import java.util.regex.Pattern

/**
 * Trigger Manager
 * Manages intelligent triggers for workflow automation
 */
@Singleton
class TriggerManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enhancedContextIntegration: EnhancedContextIntegration
) {
    
    companion object {
        private const val TAG = "TriggerManager"
        private const val MAX_TRIGGER_HISTORY = 1000
    }
    
    private val activeTriggers = mutableMapOf<String, TriggerDefinition>()
    private val triggerHistory = mutableListOf<TriggerEvent>()
    private val triggerListeners = mutableMapOf<String, (TriggerEvent) -> Unit>()
    
    // System receivers
    private val connectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ConnectivityManager.CONNECTIVITY_ACTION -> {
                    handleConnectivityChange()
                }
            }
        }
    }
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    handleBatteryChange(intent)
                }
            }
        }
    }
    
    /**
     * Initialize trigger manager
     */
    suspend fun initialize() {
        try {
            // Register system receivers
            registerSystemReceivers()
            
            // Register built-in triggers
            registerBuiltInTriggers()
            
            Log.d(TAG, "Trigger Manager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Trigger Manager", e)
        }
    }
    
    /**
     * Register trigger
     */
    fun registerTrigger(trigger: TriggerDefinition, listener: (TriggerEvent) -> Unit) {
        activeTriggers[trigger.id] = trigger
        triggerListeners[trigger.id] = listener
        Log.d(TAG, "Registered trigger: ${trigger.name}")
    }
    
    /**
     * Unregister trigger
     */
    fun unregisterTrigger(triggerId: String) {
        activeTriggers.remove(triggerId)
        triggerListeners.remove(triggerId)
        Log.d(TAG, "Unregistered trigger: $triggerId")
    }
    
    /**
     * Check text pattern triggers
     */
    suspend fun checkTextPatternTriggers(text: String): Flow<List<TriggerEvent>> = flow {
        val triggeredEvents = mutableListOf<TriggerEvent>()
        
        for (trigger in activeTriggers.values) {
            if (trigger.type == TriggerType.TEXT_PATTERN) {
                val confidence = evaluateTextPattern(trigger.pattern, text)
                if (confidence >= trigger.confidence) {
                    val event = TriggerEvent(
                        id = UUID.randomUUID().toString(),
                        triggerId = trigger.id,
                        type = trigger.type,
                        confidence = confidence,
                        data = mapOf(
                            "text" to text,
                            "pattern" to trigger.pattern,
                            "matched_text" to extractMatchedText(trigger.pattern, text)
                        ),
                        timestamp = System.currentTimeMillis()
                    )
                    
                    triggeredEvents.add(event)
                    triggerHistory.add(event)
                    
                    // Notify listener
                    triggerListeners[trigger.id]?.invoke(event)
                }
            }
        }
        
        emit(triggeredEvents)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check time-based triggers
     */
    suspend fun checkTimeBasedTriggers(): Flow<List<TriggerEvent>> = flow {
        val triggeredEvents = mutableListOf<TriggerEvent>()
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        for (trigger in activeTriggers.values) {
            if (trigger.type == TriggerType.TIME_BASED) {
                val shouldTrigger = evaluateTimeBasedTrigger(trigger, calendar)
                if (shouldTrigger) {
                    val event = TriggerEvent(
                        id = UUID.randomUUID().toString(),
                        triggerId = trigger.id,
                        type = trigger.type,
                        confidence = 1.0f,
                        data = mapOf(
                            "current_time" to currentTime,
                            "trigger_time" to (trigger.parameters["time"] ?: "")
                        ),
                        timestamp = currentTime
                    )
                    
                    triggeredEvents.add(event)
                    triggerHistory.add(event)
                    
                    // Notify listener
                    triggerListeners[trigger.id]?.invoke(event)
                }
            }
        }
        
        emit(triggeredEvents)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check location-based triggers
     */
    suspend fun checkLocationBasedTriggers(): Flow<List<TriggerEvent>> = flow {
        val triggeredEvents = mutableListOf<TriggerEvent>()
        
        try {
            val comprehensiveContext = enhancedContextIntegration.getComprehensiveContext().first()
            val location = comprehensiveContext.locationContext.currentLocation
            
            if (location != null) {
                for (trigger in activeTriggers.values) {
                    if (trigger.type == TriggerType.LOCATION_BASED) {
                        val shouldTrigger = evaluateLocationBasedTrigger(trigger, location.latitude, location.longitude)
                        if (shouldTrigger) {
                            val event = TriggerEvent(
                                id = UUID.randomUUID().toString(),
                                triggerId = trigger.id,
                                type = trigger.type,
                                confidence = 1.0f,
                                data = mapOf(
                                    "latitude" to location.latitude,
                                    "longitude" to location.longitude,
                                    "accuracy" to location.accuracy
                                ),
                                timestamp = System.currentTimeMillis()
                            )
                            
                            triggeredEvents.add(event)
                            triggerHistory.add(event)
                            
                            // Notify listener
                            triggerListeners[trigger.id]?.invoke(event)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking location-based triggers", e)
        }
        
        emit(triggeredEvents)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Check app launch triggers
     */
    suspend fun checkAppLaunchTriggers(packageName: String): Flow<List<TriggerEvent>> = flow {
        val triggeredEvents = mutableListOf<TriggerEvent>()
        
        for (trigger in activeTriggers.values) {
            if (trigger.type == TriggerType.APP_LAUNCH) {
                val targetPackage = trigger.parameters["package_name"] as? String
                if (targetPackage == packageName) {
                    val event = TriggerEvent(
                        id = UUID.randomUUID().toString(),
                        triggerId = trigger.id,
                        type = trigger.type,
                        confidence = 1.0f,
                        data = mapOf(
                            "package_name" to packageName,
                            "app_name" to getAppName(packageName)
                        ),
                        timestamp = System.currentTimeMillis()
                    )
                    
                    triggeredEvents.add(event)
                    triggerHistory.add(event)
                    
                    // Notify listener
                    triggerListeners[trigger.id]?.invoke(event)
                }
            }
        }
        
        emit(triggeredEvents)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get trigger history
     */
    fun getTriggerHistory(limit: Int = 100): List<TriggerEvent> {
        return triggerHistory.takeLast(limit)
    }
    
    /**
     * Get active triggers
     */
    fun getActiveTriggers(): List<TriggerDefinition> {
        return activeTriggers.values.toList()
    }
    
    /**
     * Register system receivers
     */
    private fun registerSystemReceivers() {
        val connectivityFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(connectivityReceiver, connectivityFilter)
        
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, batteryFilter)
    }
    
    /**
     * Register built-in triggers
     */
    private fun registerBuiltInTriggers() {
        // Meeting trigger
        registerTrigger(TriggerDefinition(
            id = "meeting_trigger",
            name = "Meeting Trigger",
            type = TriggerType.TEXT_PATTERN,
            pattern = "họp|meeting|cuộc họp|lịch họp",
            confidence = 0.8f,
            parameters = mapOf(
                "workflow_id" to "smart_meeting",
                "priority" to "high"
            )
        )) { event ->
            Log.d(TAG, "Meeting trigger activated: ${event.data}")
        }
        
        // Note trigger
        registerTrigger(TriggerDefinition(
            id = "note_trigger",
            name = "Note Trigger",
            type = TriggerType.TEXT_PATTERN,
            pattern = "ghi chú|note|memo|nhớ",
            confidence = 0.7f,
            parameters = mapOf(
                "workflow_id" to "smart_note",
                "priority" to "medium"
            )
        )) { event ->
            Log.d(TAG, "Note trigger activated: ${event.data}")
        }
        
        // Communication trigger
        registerTrigger(TriggerDefinition(
            id = "communication_trigger",
            name = "Communication Trigger",
            type = TriggerType.TEXT_PATTERN,
            pattern = "gửi|send|nhắn|message|gọi|call",
            confidence = 0.8f,
            parameters = mapOf(
                "workflow_id" to "smart_communication",
                "priority" to "high"
            )
        )) { event ->
            Log.d(TAG, "Communication trigger activated: ${event.data}")
        }
        
        // Morning routine trigger
        registerTrigger(TriggerDefinition(
            id = "morning_routine_trigger",
            name = "Morning Routine Trigger",
            type = TriggerType.TIME_BASED,
            pattern = "",
            confidence = 1.0f,
            parameters = mapOf(
                "time" to "08:00",
                "days" to listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"),
                "workflow_id" to "morning_routine"
            )
        )) { event ->
            Log.d(TAG, "Morning routine trigger activated: ${event.data}")
        }
        
        // Low battery trigger
        registerTrigger(TriggerDefinition(
            id = "low_battery_trigger",
            name = "Low Battery Trigger",
            type = TriggerType.CUSTOM_EVENT,
            pattern = "",
            confidence = 1.0f,
            parameters = mapOf(
                "event_type" to "battery_low",
                "threshold" to 20,
                "workflow_id" to "battery_management"
            )
        )) { event ->
            Log.d(TAG, "Low battery trigger activated: ${event.data}")
        }
    }
    
    /**
     * Evaluate text pattern
     */
    private fun evaluateTextPattern(pattern: String, text: String): Float {
        return try {
            val regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            val matcher = regex.matcher(text)
            
            if (matcher.find()) {
                // Calculate confidence based on match quality
                val matchLength = matcher.end() - matcher.start()
                val textLength = text.length
                val matchRatio = matchLength.toFloat() / textLength
                
                // Higher confidence for longer matches
                minOf(1.0f, 0.5f + matchRatio * 0.5f)
            } else {
                0.0f
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error evaluating text pattern: $pattern", e)
            0.0f
        }
    }
    
    /**
     * Extract matched text
     */
    private fun extractMatchedText(pattern: String, text: String): String {
        return try {
            val regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            val matcher = regex.matcher(text)
            
            if (matcher.find()) {
                matcher.group()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Evaluate time-based trigger
     */
    private fun evaluateTimeBasedTrigger(trigger: TriggerDefinition, calendar: Calendar): Boolean {
        val triggerTime = trigger.parameters["time"] as? String ?: return false
        val days = trigger.parameters["days"] as? List<String> ?: emptyList()
        
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        
        // Parse trigger time
        val timeParts = triggerTime.split(":")
        val triggerHour = timeParts[0].toIntOrNull() ?: return false
        val triggerMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
        
        // Check time match (within 5 minutes)
        val timeDiff = Math.abs((currentHour * 60 + currentMinute) - (triggerHour * 60 + triggerMinute))
        if (timeDiff > 5) return false
        
        // Check day match
        if (days.isNotEmpty()) {
            val dayNames = listOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")
            val currentDayName = dayNames[currentDay - 1]
            if (!days.contains(currentDayName)) return false
        }
        
        return true
    }
    
    /**
     * Evaluate location-based trigger
     */
    private fun evaluateLocationBasedTrigger(trigger: TriggerDefinition, latitude: Double, longitude: Double): Boolean {
        val targetLat = trigger.parameters["latitude"] as? Double ?: return false
        val targetLng = trigger.parameters["longitude"] as? Double ?: return false
        val radius = trigger.parameters["radius"] as? Double ?: 100.0 // meters
        
        val distance = calculateDistance(latitude, longitude, targetLat, targetLng)
        return distance <= radius
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
     * Handle connectivity change
     */
    @SuppressLint("MissingPermission")
    private fun handleConnectivityChange() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val isConnected = activeNetwork != null
        
        val event = TriggerEvent(
            id = UUID.randomUUID().toString(),
            triggerId = "connectivity_change",
            type = TriggerType.CUSTOM_EVENT,
            confidence = 1.0f,
            data = mapOf(
                "connected" to isConnected,
                "network_type" to if (isConnected) "connected" else "disconnected"
            ),
            timestamp = System.currentTimeMillis()
        )
        
        triggerHistory.add(event)
        Log.d(TAG, "Connectivity changed: $isConnected")
    }
    
    /**
     * Handle battery change
     */
    private fun handleBatteryChange(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPercent = (level * 100 / scale).toFloat()
        
        // Check for low battery trigger
        if (batteryPercent <= 20) {
            val event = TriggerEvent(
                id = UUID.randomUUID().toString(),
                triggerId = "low_battery_trigger",
                type = TriggerType.CUSTOM_EVENT,
                confidence = 1.0f,
                data = mapOf(
                    "battery_level" to batteryPercent,
                    "is_charging" to (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING)
                ),
                timestamp = System.currentTimeMillis()
            )
            
            triggerHistory.add(event)
            triggerListeners["low_battery_trigger"]?.invoke(event)
        }
    }
    
    /**
     * Get app name from package
     */
    private fun getAppName(packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }
    
    /**
     * Cleanup
     */
    fun cleanup() {
        try {
            context.unregisterReceiver(connectivityReceiver)
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

/**
 * Trigger event
 */
data class TriggerEvent(
    val id: String,
    val triggerId: String,
    val type: TriggerType,
    val confidence: Float,
    val data: Map<String, Any>,
    val timestamp: Long
)

/**
 * Trigger definition with ID
 */
data class TriggerDefinitionWithId(
    val id: String,
    val name: String,
    val type: TriggerType,
    val pattern: String,
    val confidence: Float,
    val parameters: Map<String, Any> = emptyMap()
)
