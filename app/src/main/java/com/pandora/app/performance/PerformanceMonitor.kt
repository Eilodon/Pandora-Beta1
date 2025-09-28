package com.pandora.app.performance

import android.content.Context
import android.util.Log
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Performance Monitor
 * Monitors app performance with Firebase Performance integration
 * Tracks startup time, memory usage, CPU usage, and user interactions
 */
@Singleton
class PerformanceMonitor(
    @ApplicationContext context: Context
) {
    private val firebasePerformance = FirebasePerformance.getInstance()
    private val _performanceData = MutableStateFlow<PerformanceData>(PerformanceData())
    val performanceData: StateFlow<PerformanceData> = _performanceData.asStateFlow()
    
    private val _activeTraces = mutableMapOf<String, Trace>()
    private val _performanceMetrics = mutableMapOf<String, PerformanceMetric>()
    
    private val monitorScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("PerformanceMonitor")
    )
    
    /**
     * Start monitoring app performance
     */
    fun startMonitoring() {
        monitorScope.launch {
            while (isActive) {
                updatePerformanceData()
                delay(5000) // Update every 5 seconds
            }
        }
        Log.d("PerformanceMonitor", "Performance monitoring started")
    }
    
    /**
     * Start a performance trace
     */
    fun startTrace(traceName: String): Trace? {
        return try {
            val trace = firebasePerformance.newTrace(traceName)
            trace.start()
            _activeTraces[traceName] = trace
            Log.d("PerformanceMonitor", "Started trace: $traceName")
            trace
        } catch (e: Exception) {
            Log.e("PerformanceMonitor", "Failed to start trace: $traceName", e)
            null
        }
    }
    
    /**
     * Stop a performance trace
     */
    fun stopTrace(traceName: String) {
        _activeTraces[traceName]?.let { trace ->
            try {
                trace.stop()
                _activeTraces.remove(traceName)
                Log.d("PerformanceMonitor", "Stopped trace: $traceName")
            } catch (e: Exception) {
                Log.e("PerformanceMonitor", "Failed to stop trace: $traceName", e)
            }
        }
    }
    
    /**
     * Add custom metric to trace
     */
    fun addMetric(traceName: String, metricName: String, value: Long) {
        _activeTraces[traceName]?.let { trace ->
            try {
                trace.putMetric(metricName, value)
                Log.d("PerformanceMonitor", "Added metric $metricName: $value to trace $traceName")
            } catch (e: Exception) {
                Log.e("PerformanceMonitor", "Failed to add metric: $metricName", e)
            }
        }
    }
    
    /**
     * Add custom attribute to trace
     */
    fun addAttribute(traceName: String, attributeName: String, value: String) {
        _activeTraces[traceName]?.let { trace ->
            try {
                trace.putAttribute(attributeName, value)
                Log.d("PerformanceMonitor", "Added attribute $attributeName: $value to trace $traceName")
            } catch (e: Exception) {
                Log.e("PerformanceMonitor", "Failed to add attribute: $attributeName", e)
            }
        }
    }
    
    /**
     * Record user interaction
     */
    fun recordUserInteraction(interactionType: String, duration: Long) {
        val metric = PerformanceMetric(
            name = "user_interaction_$interactionType",
            value = duration,
            timestamp = System.currentTimeMillis(),
            type = MetricType.USER_INTERACTION
        )
        
        _performanceMetrics[metric.name] = metric
        Log.d("PerformanceMonitor", "Recorded user interaction: $interactionType - ${duration}ms")
    }
    
    /**
     * Record screen load time
     */
    fun recordScreenLoadTime(screenName: String, loadTime: Long) {
        val metric = PerformanceMetric(
            name = "screen_load_$screenName",
            value = loadTime,
            timestamp = System.currentTimeMillis(),
            type = MetricType.SCREEN_LOAD
        )
        
        _performanceMetrics[metric.name] = metric
        Log.d("PerformanceMonitor", "Recorded screen load time: $screenName - ${loadTime}ms")
    }
    
    /**
     * Record API call performance
     */
    fun recordAPICall(apiName: String, responseTime: Long, success: Boolean) {
        val metric = PerformanceMetric(
            name = "api_call_$apiName",
            value = responseTime,
            timestamp = System.currentTimeMillis(),
            type = MetricType.API_CALL,
            success = success
        )
        
        _performanceMetrics[metric.name] = metric
        Log.d("PerformanceMonitor", "Recorded API call: $apiName - ${responseTime}ms (success: $success)")
    }
    
    /**
     * Record memory usage
     */
    fun recordMemoryUsage(usage: Long) {
        val metric = PerformanceMetric(
            name = "memory_usage",
            value = usage,
            timestamp = System.currentTimeMillis(),
            type = MetricType.MEMORY_USAGE
        )
        
        _performanceMetrics[metric.name] = metric
    }
    
    /**
     * Record CPU usage
     */
    fun recordCPUUsage(usage: Int) {
        val metric = PerformanceMetric(
            name = "cpu_usage",
            value = usage.toLong(),
            timestamp = System.currentTimeMillis(),
            type = MetricType.CPU_USAGE
        )
        
        _performanceMetrics[metric.name] = metric
    }
    
    /**
     * Update performance data
     */
    private suspend fun updatePerformanceData() {
        val currentData = _performanceData.value
        val newData = currentData.copy(
            timestamp = System.currentTimeMillis(),
            activeTraces = _activeTraces.size,
            totalMetrics = _performanceMetrics.size,
            memoryUsage = getMemoryUsage(),
            cpuUsage = getCPUUsage()
        )
        
        _performanceData.value = newData
    }
    
    /**
     * Get memory usage
     */
    private fun getMemoryUsage(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    /**
     * Get CPU usage
     */
    private fun getCPUUsage(): Int {
        // Simplified CPU usage calculation
        return (Thread.activeCount() * 10).coerceAtMost(100)
    }
    
    /**
     * Get performance report
     */
    fun getPerformanceReport(): PerformanceReport {
        val metrics = _performanceMetrics.values.toList()
        val traces = _activeTraces.keys.toList()
        
        val averageResponseTime = metrics
            .filter { it.type == MetricType.API_CALL }
            .map { it.value }
            .average()
            .toLong()
        
        val successRate = metrics
            .filter { it.type == MetricType.API_CALL }
            .let { apiCalls ->
                if (apiCalls.isEmpty()) 0.0
                else apiCalls.count { it.success == true }.toDouble() / apiCalls.size
            }
        
        val memoryUsage = metrics
            .filter { it.type == MetricType.MEMORY_USAGE }
            .lastOrNull()?.value ?: 0L
        
        val cpuUsage = metrics
            .filter { it.type == MetricType.CPU_USAGE }
            .lastOrNull()?.value?.toInt() ?: 0
        
        return PerformanceReport(
            totalMetrics = metrics.size,
            activeTraces = traces.size,
            averageResponseTime = averageResponseTime,
            successRate = successRate,
            memoryUsage = memoryUsage,
            cpuUsage = cpuUsage,
            metrics = metrics,
            traces = traces
        )
    }
    
    /**
     * Get performance recommendations
     */
    fun getPerformanceRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val report = getPerformanceReport()
        
        if (report.averageResponseTime > 2000) {
            recommendations.add("API response time is slow (${report.averageResponseTime}ms) - consider optimizing backend or caching")
        }
        
        if (report.successRate < 0.95) {
            recommendations.add("API success rate is low (${(report.successRate * 100).toInt()}%) - review error handling")
        }
        
        if (report.memoryUsage > 100 * 1024 * 1024) { // 100MB
            recommendations.add("Memory usage is high (${report.memoryUsage / 1024 / 1024}MB) - consider memory optimization")
        }
        
        if (report.cpuUsage > 80) {
            recommendations.add("CPU usage is high (${report.cpuUsage}%) - consider reducing task concurrency")
        }
        
        return recommendations
    }
    
    /**
     * Clear performance data
     */
    fun clearPerformanceData() {
        _performanceMetrics.clear()
        _activeTraces.clear()
        Log.d("PerformanceMonitor", "Performance data cleared")
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        monitorScope.cancel()
        _activeTraces.values.forEach { trace ->
            try {
                trace.stop()
            } catch (e: Exception) {
                Log.e("PerformanceMonitor", "Failed to stop trace", e)
            }
        }
        _activeTraces.clear()
        Log.d("PerformanceMonitor", "Performance monitoring stopped")
    }
}

/**
 * Data class for performance data
 */
data class PerformanceData(
    val timestamp: Long = System.currentTimeMillis(),
    val activeTraces: Int = 0,
    val totalMetrics: Int = 0,
    val memoryUsage: Long = 0,
    val cpuUsage: Int = 0
)

/**
 * Data class for performance metric
 */
data class PerformanceMetric(
    val name: String,
    val value: Long,
    val timestamp: Long,
    val type: MetricType,
    val success: Boolean? = null
)

/**
 * Enum for metric types
 */
enum class MetricType {
    USER_INTERACTION,
    SCREEN_LOAD,
    API_CALL,
    MEMORY_USAGE,
    CPU_USAGE,
    CUSTOM
}

/**
 * Data class for performance report
 */
data class PerformanceReport(
    val totalMetrics: Int,
    val activeTraces: Int,
    val averageResponseTime: Long,
    val successRate: Double,
    val memoryUsage: Long,
    val cpuUsage: Int,
    val metrics: List<PerformanceMetric>,
    val traces: List<String>
)
