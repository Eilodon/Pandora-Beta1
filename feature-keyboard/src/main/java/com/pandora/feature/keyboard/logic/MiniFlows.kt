package com.pandora.feature.keyboard.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.work.WorkInfo
import com.pandora.core.ai.flows.FlowScheduler
import com.pandora.core.ai.flows.FlowType
import com.pandora.core.ai.flows.MiniFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Mini-Flows for Neural Keyboard
 * Integrates with WorkManager for background execution
 */
@Singleton
class MiniFlows @Inject constructor(
    @ApplicationContext private val context: Context,
    private val flowScheduler: FlowScheduler
) {
    
    /**
     * Execute calendar flow with WorkManager
     */
    fun executeCalendarFlow(title: String, startTime: Long): Flow<Boolean> = flow {
        try {
            val flow = MiniFlow(
                id = "calendar_${System.currentTimeMillis()}",
                type = FlowType.CALENDAR,
                data = "title:$title,startTime:$startTime"
            )
            
            flowScheduler.scheduleMiniFlow(flow)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
    
    /**
     * Execute Spotify flow with WorkManager
     */
    fun executeSpotifyFlow(): Flow<Boolean> = flow {
        try {
            val flow = MiniFlow(
                id = "spotify_${System.currentTimeMillis()}",
                type = FlowType.SPOTIFY,
                data = "auto_play:true"
            )
            
            flowScheduler.scheduleMiniFlow(flow)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
    
    /**
     * Execute Maps flow with WorkManager
     */
    fun executeMapsFlow(query: String): Flow<Boolean> = flow {
        try {
            val flow = MiniFlow(
                id = "maps_${System.currentTimeMillis()}",
                type = FlowType.MAPS,
                data = "query:$query"
            )
            
            flowScheduler.scheduleMiniFlow(flow)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
    
    /**
     * Execute Camera flow with WorkManager
     */
    fun executeCameraFlow(): Flow<Boolean> = flow {
        try {
            val flow = MiniFlow(
                id = "camera_${System.currentTimeMillis()}",
                type = FlowType.CAMERA,
                data = "mode:photo"
            )
            
            flowScheduler.scheduleMiniFlow(flow)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
    
    /**
     * Execute Keep flow with WorkManager
     */
    fun executeKeepFlow(title: String, content: String): Flow<Boolean> = flow {
        try {
            val flow = MiniFlow(
                id = "keep_${System.currentTimeMillis()}",
                type = FlowType.KEEP,
                data = "title:$title,content:$content"
            )
            
            flowScheduler.scheduleMiniFlow(flow)
            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }
    
    /**
     * Execute flow based on action type
     */
    fun executeFlow(action: PandoraAction): Flow<Boolean> = flow {
        when (action) {
            is PandoraAction.AddToCalendar -> {
                executeCalendarFlow(action.title, action.startTime).collect { result ->
                    emit(result)
                }
            }
            is PandoraAction.CreateReminder -> {
                executeKeepFlow(action.title, action.message).collect { result ->
                    emit(result)
                }
            }
            is PandoraAction.SendMessage -> {
                executeKeepFlow("Tin nhắn", action.message).collect { result ->
                    emit(result)
                }
            }
            is PandoraAction.CreateNote -> {
                executeKeepFlow(action.title, action.content).collect { result ->
                    emit(result)
                }
            }
            is PandoraAction.Calculate -> {
                executeKeepFlow("Tính toán", action.expression).collect { result ->
                    emit(result)
                }
            }
            is PandoraAction.Search -> {
                executeMapsFlow(action.query).collect { result ->
                    emit(result)
                }
            }
            else -> {
                emit(false)
            }
        }
    }
    
    /**
     * Get flow execution status
     */
    fun getFlowStatus(flowId: String): Flow<Boolean> = flow {
        flowScheduler.getFlowStatus(flowId).collect { state ->
            emit(state == WorkInfo.State.SUCCEEDED)
        }
    }
}

/**
 * Legacy functions for backward compatibility
 */
fun checkAndTriggerCalendarFlow(context: Context, text: String) {
    if (text.contains("họp", ignoreCase = true) || 
        text.contains("meeting", ignoreCase = true) ||
        text.contains("lịch", ignoreCase = true)) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

fun checkAndTriggerMapsFlow(context: Context, text: String) {
    if (text.contains("đi", ignoreCase = true) || 
        text.contains("đường", ignoreCase = true) ||
        text.contains("maps", ignoreCase = true) ||
        text.contains("địa chỉ", ignoreCase = true)) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

fun checkAndTriggerCameraFlow(context: Context, text: String) {
    if (text.contains("chụp", ignoreCase = true) || 
        text.contains("photo", ignoreCase = true) ||
        text.contains("ảnh", ignoreCase = true) ||
        text.contains("camera", ignoreCase = true)) {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

fun checkAndTriggerNoteFlow(context: Context, text: String) {
    if (text.contains("note", ignoreCase = true) || 
        text.contains("ghi chú", ignoreCase = true) ||
        text.contains("nhớ", ignoreCase = true) ||
        text.contains("keep", ignoreCase = true)) {
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.keep")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

/**
 * Advanced Mini-Flows (3 flows còn thiếu theo blueprint)
 */

/**
 * Mute Phone Flow - Calendar trigger
 */
fun checkAndTriggerMutePhoneFlow(context: Context, text: String) {
    if (text.contains("họp", ignoreCase = true) || 
        text.contains("meeting", ignoreCase = true) ||
        text.contains("cuộc họp", ignoreCase = true)) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            audioManager.setRingerMode(android.media.AudioManager.RINGER_MODE_SILENT)
            android.util.Log.d("MiniFlows", "Phone muted for meeting")
        } catch (e: Exception) {
            android.util.Log.e("MiniFlows", "Error muting phone", e)
        }
    }
}

/**
 * Auto-reply Zalo Flow - GPS trigger (simulated)
 */
fun checkAndTriggerAutoReplyFlow(context: Context, text: String) {
    if (text.contains("đang lái", ignoreCase = true) || 
        text.contains("driving", ignoreCase = true) ||
        text.contains("đang đi đường", ignoreCase = true)) {
        try {
            // Simulate auto-reply (in real implementation, would use Zalo API)
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "Tôi đang lái xe, sẽ trả lời sau. Cảm ơn!")
                .setPackage("com.zing.zalo")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general messaging
                val fallbackIntent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "Tôi đang lái xe, sẽ trả lời sau. Cảm ơn!")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallbackIntent)
            }
            
            android.util.Log.d("MiniFlows", "Auto-reply sent for driving mode")
        } catch (e: Exception) {
            android.util.Log.e("MiniFlows", "Error sending auto-reply", e)
        }
    }
}

/**
 * WiFi Toggle Flow - Geofence trigger (simulated)
 */
fun checkAndTriggerWiFiToggleFlow(context: Context, text: String) {
    if (text.contains("rời khỏi nhà", ignoreCase = true) || 
        text.contains("leaving home", ignoreCase = true) ||
        text.contains("đi ra ngoài", ignoreCase = true)) {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            android.util.Log.d("MiniFlows", "WiFi settings opened for leaving home")
        } catch (e: Exception) {
            android.util.Log.e("MiniFlows", "Error opening WiFi settings", e)
        }
    }
}

/**
 * Open Keep Flow - App context trigger
 */
fun checkAndTriggerOpenKeepFlow(context: Context, text: String) {
    if (text.contains("note", ignoreCase = true) || 
        text.contains("ghi chú", ignoreCase = true) ||
        text.contains("keep", ignoreCase = true) ||
        text.contains("memo", ignoreCase = true)) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.keep")
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
            android.util.Log.d("MiniFlows", "Keep opened for note taking")
        } catch (e: Exception) {
            android.util.Log.e("MiniFlows", "Error opening Keep", e)
        }
    }
}

fun checkAllMiniFlows(context: Context, text: String) {
    // Original 5 flows
    checkAndTriggerCalendarFlow(context, text)
    checkAndTriggerMapsFlow(context, text)
    checkAndTriggerCameraFlow(context, text)
    checkAndTriggerNoteFlow(context, text)
    
    // Advanced 3 flows
    checkAndTriggerMutePhoneFlow(context, text)
    checkAndTriggerAutoReplyFlow(context, text)
    checkAndTriggerWiFiToggleFlow(context, text)
    checkAndTriggerOpenKeepFlow(context, text)
}