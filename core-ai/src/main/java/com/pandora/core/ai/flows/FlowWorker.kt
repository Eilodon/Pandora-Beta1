package com.pandora.core.ai.flows

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Flow Worker for executing Mini-Flows
 * Handles background execution of automation flows
 */
class FlowWorker @Inject constructor(
    @ApplicationContext context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        private const val TAG = "FlowWorker"
    }
    
    override suspend fun doWork(): Result {
        return try {
            val flowId = inputData.getString("flow_id") ?: return Result.failure()
            val flowType = inputData.getString("flow_type") ?: return Result.failure()
            val flowData = inputData.getString("flow_data") ?: return Result.failure()
            
            Log.d(TAG, "Executing flow: $flowId of type: $flowType")
            
            when (FlowType.valueOf(flowType)) {
                FlowType.CALENDAR -> executeCalendarFlow(flowData)
                FlowType.SPOTIFY -> executeSpotifyFlow(flowData)
                FlowType.MAPS -> executeMapsFlow(flowData)
                FlowType.CAMERA -> executeCameraFlow(flowData)
                FlowType.KEEP -> executeKeepFlow(flowData)
            }
            
            Log.d(TAG, "Flow $flowId completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Flow execution failed", e)
            Result.retry()
        }
    }
    
    /**
     * Execute Calendar Flow
     */
    private suspend fun executeCalendarFlow(data: String) {
        try {
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                .putExtra(android.provider.CalendarContract.Events.TITLE, "Cuộc họp từ PandoraOS")
                .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Tự động tạo từ PandoraOS")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            applicationContext.startActivity(intent)
            Log.d(TAG, "Calendar flow executed")
        } catch (e: Exception) {
            Log.e(TAG, "Calendar flow failed", e)
        }
    }
    
    /**
     * Execute Spotify Flow
     */
    private suspend fun executeSpotifyFlow(data: String) {
        try {
            val intent = applicationContext.packageManager.getLaunchIntentForPackage("com.spotify.music")
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                applicationContext.startActivity(intent)
                Log.d(TAG, "Spotify flow executed")
            } else {
                Log.w(TAG, "Spotify not installed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Spotify flow failed", e)
        }
    }
    
    /**
     * Execute Maps Flow
     */
    private suspend fun executeMapsFlow(data: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("geo:0,0?q=siêu thị"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            applicationContext.startActivity(intent)
            Log.d(TAG, "Maps flow executed")
        } catch (e: Exception) {
            Log.e(TAG, "Maps flow failed", e)
        }
    }
    
    /**
     * Execute Camera Flow
     */
    private suspend fun executeCameraFlow(data: String) {
        try {
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            applicationContext.startActivity(intent)
            Log.d(TAG, "Camera flow executed")
        } catch (e: Exception) {
            Log.e(TAG, "Camera flow failed", e)
        }
    }
    
    /**
     * Execute Keep Flow
     */
    private suspend fun executeKeepFlow(data: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse("https://keep.google.com"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            applicationContext.startActivity(intent)
            Log.d(TAG, "Keep flow executed")
        } catch (e: Exception) {
            Log.e(TAG, "Keep flow failed", e)
        }
    }
}
