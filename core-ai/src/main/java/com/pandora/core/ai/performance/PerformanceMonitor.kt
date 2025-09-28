package com.pandora.core.ai.performance

import android.content.Context
import android.os.Debug
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance Monitor for AI operations
 * Tracks CPU usage, memory usage, and inference times
 */
@Singleton
class PerformanceMonitor @Inject constructor(
    private val context: Context
) {
    private var startTime: Long = 0
    private var inferenceCount: Int = 0
    private var totalInferenceTime: Long = 0
    
    companion object {
        private const val TAG = "PerformanceMonitor"
        private const val MAX_CPU_USAGE = 40.0 // 40% CPU usage limit
        private const val MAX_MEMORY_USAGE = 100 * 1024 * 1024 // 100MB memory limit
        private const val MAX_INFERENCE_TIME = 16L // 16ms inference time limit
    }
    
    /**
     * Start monitoring an operation
     */
    fun startOperation() {
        startTime = System.currentTimeMillis()
    }
    
    /**
     * End monitoring and record metrics
     */
    fun endOperation(operationName: String) {
        val duration = System.currentTimeMillis() - startTime
        
        when (operationName) {
            "inference" -> {
                inferenceCount++
                totalInferenceTime += duration
                Log.d(TAG, "Inference #$inferenceCount completed in ${duration}ms")
            }
            "flow_execution" -> {
                Log.d(TAG, "Flow execution completed in ${duration}ms")
            }
            "memory_operation" -> {
                Log.d(TAG, "Memory operation completed in ${duration}ms")
            }
        }
    }
    
    /**
     * Get current performance metrics
     */
    fun getPerformanceMetrics(): Flow<PerformanceMetrics> = flow {
        val memoryInfo = getMemoryInfo()
        val cpuInfo = getCpuInfo()
        val averageInferenceTime = if (inferenceCount > 0) totalInferenceTime / inferenceCount else 0L
        
        val metrics = PerformanceMetrics(
            memoryUsage = memoryInfo.usedMemory,
            memoryLimit = memoryInfo.maxMemory,
            cpuUsage = cpuInfo,
            averageInferenceTime = averageInferenceTime,
            inferenceCount = inferenceCount,
            isHealthy = isSystemHealthy(memoryInfo, cpuInfo, averageInferenceTime)
        )
        
        emit(metrics)
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get memory information
     */
    private fun getMemoryInfo(): MemoryInfo {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        return MemoryInfo(
            usedMemory = usedMemory,
            maxMemory = maxMemory
        )
    }
    
    /**
     * Get CPU usage information
     */
    private fun getCpuInfo(): Double {
        // Simplified CPU usage calculation
        // In production, use more sophisticated methods
        return try {
            val pid = android.os.Process.myPid()
            val reader = java.io.BufferedReader(
                java.io.FileReader("/proc/$pid/stat")
            )
            val line = reader.readLine()
            reader.close()
            
            val fields = line.split(" ")
            val utime = fields[13].toLong()
            val stime = fields[14].toLong()
            val totalTime = utime + stime
            
            // Calculate CPU usage percentage
            (totalTime / 1000.0) % 100.0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get CPU info", e)
            0.0
        }
    }
    
    /**
     * Check if system is healthy
     */
    private fun isSystemHealthy(
        memoryInfo: MemoryInfo,
        cpuUsage: Double,
        averageInferenceTime: Long
    ): Boolean {
        return memoryInfo.usedMemory < MAX_MEMORY_USAGE &&
               cpuUsage < MAX_CPU_USAGE &&
               averageInferenceTime < MAX_INFERENCE_TIME
    }
    
    /**
     * Reset performance counters
     */
    fun resetCounters() {
        inferenceCount = 0
        totalInferenceTime = 0
        Log.d(TAG, "Performance counters reset")
    }
    
    /**
     * Get optimization recommendations
     */
    fun getOptimizationRecommendations(): Flow<List<OptimizationRecommendation>> = flow {
        val recommendations = mutableListOf<OptimizationRecommendation>()
        
        val memoryInfo = getMemoryInfo()
        val cpuUsage = getCpuInfo()
        val averageInferenceTime = if (inferenceCount > 0) totalInferenceTime / inferenceCount else 0L
        
        if (memoryInfo.usedMemory > MAX_MEMORY_USAGE) {
            recommendations.add(
                OptimizationRecommendation(
                    type = RecommendationType.MEMORY,
                    message = "Memory usage is high. Consider reducing model size or implementing memory optimization.",
                    priority = Priority.HIGH
                )
            )
        }
        
        if (cpuUsage > MAX_CPU_USAGE) {
            recommendations.add(
                OptimizationRecommendation(
                    type = RecommendationType.CPU,
                    message = "CPU usage is high. Consider using quantized models or reducing inference frequency.",
                    priority = Priority.HIGH
                )
            )
        }
        
        if (averageInferenceTime > MAX_INFERENCE_TIME) {
            recommendations.add(
                OptimizationRecommendation(
                    type = RecommendationType.PERFORMANCE,
                    message = "Inference time is slow. Consider using smaller models or hardware acceleration.",
                    priority = Priority.MEDIUM
                )
            )
        }
        
        emit(recommendations)
    }.flowOn(Dispatchers.IO)
}

/**
 * Performance metrics data class
 */
data class PerformanceMetrics(
    val memoryUsage: Long,
    val memoryLimit: Long,
    val cpuUsage: Double,
    val averageInferenceTime: Long,
    val inferenceCount: Int,
    val isHealthy: Boolean
)

/**
 * Memory information data class
 */
data class MemoryInfo(
    val usedMemory: Long,
    val maxMemory: Long
)

/**
 * Optimization recommendation data class
 */
data class OptimizationRecommendation(
    val type: RecommendationType,
    val message: String,
    val priority: Priority
)

/**
 * Recommendation types
 */
enum class RecommendationType {
    MEMORY, CPU, PERFORMANCE, BATTERY
}

/**
 * Priority levels
 */
enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
