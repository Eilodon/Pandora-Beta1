package com.pandora.core.ai.hybrid

/**
 * Network health information
 */
data class NetworkHealth(
    val latency: Long, // in milliseconds
    val bandwidth: Long, // in bytes per second
    val errorRate: Float, // 0.0 to 1.0
    val healthScore: Int, // 0 to 100
    val isConnected: Boolean = true,
    val connectionType: String = "unknown"
) {
    val isHealthy: Boolean
        get() = healthScore > 50 && isConnected && errorRate < 0.1f
}
