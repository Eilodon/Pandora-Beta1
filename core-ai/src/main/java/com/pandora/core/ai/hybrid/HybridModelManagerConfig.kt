package com.pandora.core.ai.hybrid

/**
 * Configuration class for Hybrid Model Manager
 * Provides production-ready settings and constants
 */
data class HybridModelManagerConfig(
    // Storage settings
    val maxStorageSize: Long = 500 * 1024 * 1024L, // 500MB
    val maxModelCount: Int = 50,
    val cleanupThreshold: Float = 0.8f, // Start cleanup at 80% capacity
    
    // Network settings
    val connectionTimeoutMs: Long = 30000L, // 30 seconds
    val readTimeoutMs: Long = 60000L, // 60 seconds
    val maxRetryAttempts: Int = 3,
    val retryDelayMs: Long = 1000L, // 1 second
    
    // Performance settings
    val maxConcurrentLoads: Int = 3,
    val cacheExpirationHours: Long = 24L, // 24 hours
    val compressionEnabled: Boolean = true,
    val deltaUpdatesEnabled: Boolean = true,
    
    // Monitoring settings
    val enablePerformanceMonitoring: Boolean = true,
    val enableErrorReporting: Boolean = true,
    val logLevel: LogLevel = LogLevel.INFO,
    
    // Security settings
    val enableChecksumVerification: Boolean = true,
    val enableSecureStorage: Boolean = true,
    val allowedCompressionTypes: Set<String> = setOf("gzip", "zstd", "brotli", "none"),
    
    // Development settings
    val enableDebugMode: Boolean = false,
    val enableMockNetwork: Boolean = false,
    val mockNetworkDelayMs: Long = 1000L
) {
    companion object {
        /**
         * Default production configuration
         */
        val PRODUCTION = HybridModelManagerConfig(
            maxStorageSize = 500 * 1024 * 1024L,
            maxModelCount = 50,
            cleanupThreshold = 0.8f,
            connectionTimeoutMs = 30000L,
            readTimeoutMs = 60000L,
            maxRetryAttempts = 3,
            retryDelayMs = 1000L,
            maxConcurrentLoads = 3,
            cacheExpirationHours = 24L,
            compressionEnabled = true,
            deltaUpdatesEnabled = true,
            enablePerformanceMonitoring = true,
            enableErrorReporting = true,
            logLevel = LogLevel.INFO,
            enableChecksumVerification = true,
            enableSecureStorage = true,
            allowedCompressionTypes = setOf("gzip", "zstd", "brotli", "none"),
            enableDebugMode = false,
            enableMockNetwork = false,
            mockNetworkDelayMs = 1000L
        )
        
        /**
         * Development configuration with relaxed settings
         */
        val DEVELOPMENT = HybridModelManagerConfig(
            maxStorageSize = 100 * 1024 * 1024L, // 100MB
            maxModelCount = 10,
            cleanupThreshold = 0.9f,
            connectionTimeoutMs = 10000L, // 10 seconds
            readTimeoutMs = 30000L, // 30 seconds
            maxRetryAttempts = 2,
            retryDelayMs = 500L,
            maxConcurrentLoads = 2,
            cacheExpirationHours = 1L, // 1 hour
            compressionEnabled = true,
            deltaUpdatesEnabled = false, // Disabled for development
            enablePerformanceMonitoring = true,
            enableErrorReporting = false,
            logLevel = LogLevel.DEBUG,
            enableChecksumVerification = false, // Disabled for development
            enableSecureStorage = false,
            allowedCompressionTypes = setOf("gzip", "none"),
            enableDebugMode = true,
            enableMockNetwork = true,
            mockNetworkDelayMs = 100L
        )
        
        /**
         * Testing configuration with minimal resources
         */
        val TESTING = HybridModelManagerConfig(
            maxStorageSize = 10 * 1024 * 1024L, // 10MB
            maxModelCount = 5,
            cleanupThreshold = 0.7f,
            connectionTimeoutMs = 5000L, // 5 seconds
            readTimeoutMs = 10000L, // 10 seconds
            maxRetryAttempts = 1,
            retryDelayMs = 100L,
            maxConcurrentLoads = 1,
            cacheExpirationHours = 0L, // No expiration for testing
            compressionEnabled = false,
            deltaUpdatesEnabled = false,
            enablePerformanceMonitoring = false,
            enableErrorReporting = false,
            logLevel = LogLevel.VERBOSE,
            enableChecksumVerification = false,
            enableSecureStorage = false,
            allowedCompressionTypes = setOf("none"),
            enableDebugMode = true,
            enableMockNetwork = true,
            mockNetworkDelayMs = 10L
        )
    }
}

/**
 * Log levels for Hybrid Model Manager
 */
enum class LogLevel {
    VERBOSE, DEBUG, INFO, WARNING, ERROR
}

/**
 * Performance metrics for monitoring
 */
data class PerformanceMetrics(
    val totalLoads: Long = 0,
    val cacheHits: Long = 0,
    val cacheMisses: Long = 0,
    val networkLoads: Long = 0,
    val deltaUpdates: Long = 0,
    val averageLoadTimeMs: Long = 0,
    val averageCacheLoadTimeMs: Long = 0,
    val averageNetworkLoadTimeMs: Long = 0,
    val totalStorageUsed: Long = 0,
    val compressionRatio: Float = 1.0f,
    val errorCount: Long = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val cacheHitRate: Float
        get() = if (totalLoads > 0) cacheHits.toFloat() / totalLoads.toFloat() else 0f
    
    val averageLoadTimeSeconds: Float
        get() = averageLoadTimeMs / 1000f
}

/**
 * Error types for reporting
 */
enum class ErrorType {
    NETWORK_ERROR,
    STORAGE_ERROR,
    COMPRESSION_ERROR,
    DECOMPRESSION_ERROR,
    CHECKSUM_ERROR,
    TIMEOUT_ERROR,
    CONFIGURATION_ERROR,
    UNKNOWN_ERROR
}

/**
 * Error report for monitoring
 */
data class ErrorReport(
    val errorType: ErrorType,
    val message: String,
    val modelId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val stackTrace: String? = null,
    val context: Map<String, Any> = emptyMap()
)
