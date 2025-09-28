package com.pandora.core.ai.flows

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flow Scheduler for Mini-Flows
 * Manages background execution of automation flows
 */
@Singleton
class FlowScheduler @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule a mini-flow for execution
     */
    fun scheduleMiniFlow(flow: MiniFlow) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresStorageNotLow(true)
            .build()
            
        val request = OneTimeWorkRequestBuilder<FlowWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(
                "flow_id" to flow.id,
                "flow_type" to flow.type.name,
                "flow_data" to flow.data
            ))
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1000L, // 1 second
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .build()
            
        workManager.enqueueUniqueWork(
            flow.id,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
    
    /**
     * Cancel a scheduled flow
     */
    fun cancelFlow(flowId: String) {
        workManager.cancelUniqueWork(flowId)
    }
    
    /**
     * Get flow execution status
     */
    fun getFlowStatus(flowId: String): Flow<WorkInfo.State> = flow {
        val workInfos = workManager.getWorkInfosForUniqueWork(flowId)
        val workInfoList = workInfos.get()
        val workInfo = workInfoList.firstOrNull()
        if (workInfo != null) {
            emit(workInfo.state)
        } else {
            emit(WorkInfo.State.ENQUEUED)
        }
    }
    
    /**
     * Schedule all enabled flows
     */
    fun scheduleEnabledFlows() {
        val enabledFlows = getEnabledFlows()
        enabledFlows.forEach { flow ->
            scheduleMiniFlow(flow)
        }
    }
    
    /**
     * Get list of enabled flows
     */
    private fun getEnabledFlows(): List<MiniFlow> {
        return listOf(
            MiniFlow(
                id = "calendar_flow",
                type = FlowType.CALENDAR,
                data = "calendar_flow_data",
                enabled = true
            ),
            MiniFlow(
                id = "spotify_flow",
                type = FlowType.SPOTIFY,
                data = "spotify_flow_data",
                enabled = true
            ),
            MiniFlow(
                id = "maps_flow",
                type = FlowType.MAPS,
                data = "maps_flow_data",
                enabled = true
            ),
            MiniFlow(
                id = "camera_flow",
                type = FlowType.CAMERA,
                data = "camera_flow_data",
                enabled = true
            ),
            MiniFlow(
                id = "keep_flow",
                type = FlowType.KEEP,
                data = "keep_flow_data",
                enabled = true
            )
        )
    }
}

/**
 * Mini-Flow data class
 */
data class MiniFlow(
    val id: String,
    val type: FlowType,
    val data: String,
    val enabled: Boolean = true
)

/**
 * Flow types
 */
enum class FlowType {
    CALENDAR, SPOTIFY, MAPS, CAMERA, KEEP
}
