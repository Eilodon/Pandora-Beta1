package com.pandora.core.ai.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network Health Monitor with Sliding Window
 * Monitors network performance, latency, and error rates
 * Features: Sliding window monitoring, network profiling, adaptive strategies
 */
@Singleton
class NetworkHealthMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _healthMetrics = MutableStateFlow<NetworkHealthMetrics>(NetworkHealthMetrics())
    val healthMetrics: StateFlow<NetworkHealthMetrics> = _healthMetrics.asStateFlow()
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkCallback = createNetworkCallback()
    
    // Sliding window for latency monitoring (last 100 measurements)
    private val latencyWindow = ConcurrentLinkedQueue<LatencyMeasurement>()
    private val errorWindow = ConcurrentLinkedQueue<ErrorMeasurement>()
    
    // Configuration
    private val maxWindowSize = 100
    private val healthCheckInterval = 30000L // 30 seconds
    private val latencyThreshold = 2000L // 2 seconds
    private val errorRateThreshold = 0.1f // 10%
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    init {
        startNetworkMonitoring()
        startHealthChecks()
    }
    
    /**
     * Start network monitoring
     */
    private fun startNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        
        Log.d("NetworkHealthMonitor", "Network monitoring started")
    }
    
    /**
     * Create network callback
     */
    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            scope.launch {
                updateNetworkStatus(true, getNetworkType(network))
                Log.d("NetworkHealthMonitor", "Network available: ${getNetworkType(network)}")
            }
        }
        
        override fun onLost(network: Network) {
            scope.launch {
                updateNetworkStatus(false, NetworkType.UNKNOWN)
                Log.d("NetworkHealthMonitor", "Network lost")
            }
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            scope.launch {
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val networkType = getNetworkType(network)
                updateNetworkStatus(isConnected, networkType)
            }
        }
    }
    
    /**
     * Get network type from network capabilities
     */
    private fun getNetworkType(network: Network): NetworkType {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
    }
    
    /**
     * Update network status
     */
    private fun updateNetworkStatus(isConnected: Boolean, networkType: NetworkType) {
        val currentStatus = _networkStatus.value
        _networkStatus.value = currentStatus.copy(
            isConnected = isConnected,
            networkType = networkType,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Public method to update network status
     */
    fun updateNetworkStatus() {
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        val isConnected = capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
        
        val networkType = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
        }
        
        updateNetworkStatus(isConnected, networkType)
    }
    
    /**
     * Record latency for a network operation
     */
    fun recordLatency(latencyMs: Long) {
        recordLatencyMeasurement(latencyMs, true)
        updateHealthMetrics()
    }
    
    /**
     * Record error for a network operation
     */
    fun recordError(isError: Boolean) {
        if (isError) {
            recordErrorMeasurement("Network operation failed")
        }
        updateHealthMetrics()
    }
    
    /**
     * Start periodic health checks
     */
    private fun startHealthChecks() {
        scope.launch {
            while (isActive) {
                if (_networkStatus.value.isConnected) {
                    performHealthCheck()
                }
                delay(healthCheckInterval)
            }
        }
    }
    
    /**
     * Perform network health check
     */
    private suspend fun performHealthCheck() {
        try {
            val startTime = System.currentTimeMillis()
            val success = testNetworkConnectivity()
            val latency = System.currentTimeMillis() - startTime
            
            // Record latency measurement
            recordLatencyMeasurement(latency, success)
            
            // Record error if failed
            if (!success) {
                recordErrorMeasurement("Connectivity test failed")
            }
            
            // Update health metrics
            updateHealthMetrics()
            
        } catch (e: Exception) {
            Log.e("NetworkHealthMonitor", "Health check failed", e)
            recordErrorMeasurement("Health check exception: ${e.message}")
        }
    }
    
    /**
     * Test network connectivity
     */
    private suspend fun testNetworkConnectivity(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.google.com")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                connection.disconnect()
                
                responseCode in 200..299
            } catch (e: Exception) {
                false
            }
        }
    }
    
    /**
     * Record latency measurement
     */
    private fun recordLatencyMeasurement(latency: Long, success: Boolean) {
        val measurement = LatencyMeasurement(
            timestamp = System.currentTimeMillis(),
            latency = latency,
            success = success
        )
        
        latencyWindow.offer(measurement)
        
        // Maintain sliding window size
        while (latencyWindow.size > maxWindowSize) {
            latencyWindow.poll()
        }
    }
    
    /**
     * Record error measurement
     */
    private fun recordErrorMeasurement(error: String) {
        val measurement = ErrorMeasurement(
            timestamp = System.currentTimeMillis(),
            error = error
        )
        
        errorWindow.offer(measurement)
        
        // Maintain sliding window size
        while (errorWindow.size > maxWindowSize) {
            errorWindow.poll()
        }
    }
    
    /**
     * Update health metrics
     */
    private fun updateHealthMetrics() {
        val latencyMeasurements = latencyWindow.toList()
        val errorMeasurements = errorWindow.toList()
        
        val averageLatency = if (latencyMeasurements.isNotEmpty()) {
            latencyMeasurements.map { it.latency }.average()
        } else 0.0
        
        val successRate = if (latencyMeasurements.isNotEmpty()) {
            latencyMeasurements.count { it.success }.toFloat() / latencyMeasurements.size
        } else 1.0f
        
        val errorRate = 1.0f - successRate
        
        val recentLatency = if (latencyMeasurements.isNotEmpty()) {
            latencyMeasurements.takeLast(10).map { it.latency }.average()
        } else 0.0
        
        val healthScore = calculateHealthScore(averageLatency, successRate)
        
        _healthMetrics.value = NetworkHealthMetrics(
            averageLatency = averageLatency,
            recentLatency = recentLatency,
            successRate = successRate,
            errorRate = errorRate,
            healthScore = healthScore,
            totalMeasurements = latencyMeasurements.size,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Calculate network health score (0-100)
     */
    private fun calculateHealthScore(averageLatency: Double, successRate: Float): Int {
        val latencyScore = when {
            averageLatency < 500 -> 100
            averageLatency < 1000 -> 80
            averageLatency < 2000 -> 60
            averageLatency < 5000 -> 40
            else -> 20
        }
        
        val successScore = (successRate * 100).toInt()
        
        return (latencyScore * 0.6 + successScore * 0.4).toInt()
    }
    
    /**
     * Get network recommendations
     */
    fun getNetworkRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val metrics = _healthMetrics.value
        val status = _networkStatus.value
        
        if (!status.isConnected) {
            recommendations.add("No network connection available")
            return recommendations
        }
        
        if (metrics.averageLatency > latencyThreshold) {
            recommendations.add("High latency detected (${metrics.averageLatency.toInt()}ms) - consider switching networks")
        }
        
        if (metrics.errorRate > errorRateThreshold) {
            recommendations.add("High error rate detected (${(metrics.errorRate * 100).toInt()}%) - check network stability")
        }
        
        if (metrics.healthScore < 50) {
            recommendations.add("Poor network health - consider using offline mode or cached data")
        }
        
        if (status.networkType == NetworkType.CELLULAR && metrics.averageLatency > 1000) {
            recommendations.add("Cellular network is slow - consider waiting for WiFi")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Network is healthy and performing well")
        }
        
        return recommendations
    }
    
    /**
     * Check if network is suitable for model downloads
     */
    fun isNetworkSuitableForDownloads(): Boolean {
        val metrics = _healthMetrics.value
        val status = _networkStatus.value
        
        return status.isConnected && 
               metrics.healthScore > 70 && 
               metrics.averageLatency < latencyThreshold &&
               metrics.errorRate < errorRateThreshold
    }
    
    /**
     * Get optimal download strategy based on network conditions
     */
    fun getOptimalDownloadStrategy(): DownloadStrategy {
        val metrics = _healthMetrics.value
        val status = _networkStatus.value
        
        return when {
            !status.isConnected -> DownloadStrategy.OFFLINE
            metrics.healthScore > 80 && status.networkType == NetworkType.WIFI -> DownloadStrategy.FULL_DOWNLOAD
            metrics.healthScore > 60 -> DownloadStrategy.DELTA_UPDATE
            metrics.healthScore > 40 -> DownloadStrategy.PROGRESSIVE_DOWNLOAD
            else -> DownloadStrategy.CACHED_ONLY
        }
    }
    
    /**
     * Record custom network operation
     */
    fun recordNetworkOperation(
        operation: String,
        duration: Long,
        success: Boolean,
        error: String? = null
    ) {
        recordLatencyMeasurement(duration, success)
        
        if (!success && error != null) {
            recordErrorMeasurement("$operation: $error")
        }
        
        updateHealthMetrics()
    }
    
    /**
     * Get network statistics
     */
    fun getNetworkStatistics(): NetworkStatistics {
        val status = _networkStatus.value
        val metrics = _healthMetrics.value
        
        return NetworkStatistics(
            isConnected = status.isConnected,
            networkType = status.networkType,
            averageLatency = metrics.averageLatency,
            successRate = metrics.successRate,
            healthScore = metrics.healthScore,
            totalMeasurements = metrics.totalMeasurements,
            lastUpdated = metrics.lastUpdated
        )
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        scope.cancel()
    }
}

/**
 * Network types
 */
enum class NetworkType {
    WIFI, CELLULAR, ETHERNET, UNKNOWN
}

/**
 * Download strategies based on network conditions
 */
enum class DownloadStrategy {
    OFFLINE,           // No network
    CACHED_ONLY,       // Use only cached data
    PROGRESSIVE_DOWNLOAD, // Download in chunks
    DELTA_UPDATE,      // Download only changes
    FULL_DOWNLOAD      // Download complete model
}

/**
 * Data class for network status
 */
data class NetworkStatus(
    val isConnected: Boolean = false,
    val networkType: NetworkType = NetworkType.UNKNOWN,
    val lastUpdated: Long = 0L
)

/**
 * Data class for network health metrics
 */
data class NetworkHealthMetrics(
    val averageLatency: Double = 0.0,
    val recentLatency: Double = 0.0,
    val successRate: Float = 1.0f,
    val errorRate: Float = 0.0f,
    val healthScore: Int = 100,
    val totalMeasurements: Int = 0,
    val lastUpdated: Long = 0L
)

/**
 * Data class for latency measurement
 */
data class LatencyMeasurement(
    val timestamp: Long,
    val latency: Long,
    val success: Boolean
)

/**
 * Data class for error measurement
 */
data class ErrorMeasurement(
    val timestamp: Long,
    val error: String
)

/**
 * Data class for network statistics
 */
data class NetworkStatistics(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val averageLatency: Double,
    val successRate: Float,
    val healthScore: Int,
    val totalMeasurements: Int,
    val lastUpdated: Long
)
