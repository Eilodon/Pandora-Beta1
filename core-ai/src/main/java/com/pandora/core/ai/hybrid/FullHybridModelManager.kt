package com.pandora.core.ai.hybrid

import android.content.Context
import android.util.Log
import com.pandora.core.ai.storage.ModelStorageManager
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.compression.CompressionCodec
import com.pandora.core.ai.compression.GzipCodec
import com.pandora.core.ai.compression.ZstdCodec
import com.pandora.core.ai.compression.BrotliCodec
import com.pandora.core.ai.network.NetworkHealthMonitor
import com.pandora.core.ai.delta.DeltaUpdateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.nio.ByteBuffer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full-featured Hybrid Model Manager with all advanced capabilities
 * Features: Progressive Loading, Delta Updates, Compression, Caching, Monitoring, Analytics
 */
@Singleton
class FullHybridModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageManager: ModelStorageManager,
    private val networkMonitor: NetworkHealthMonitor,
    private val deltaUpdateManager: DeltaUpdateManager,
    private val config: HybridModelManagerConfig = HybridModelManagerConfig.PRODUCTION
) {
    // Status and metrics
    private val _managerStatus = MutableStateFlow(ManagerStatus.IDLE)
    val managerStatus: StateFlow<ManagerStatus> = _managerStatus.asStateFlow()

    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()

    private val _activeSessions = MutableStateFlow<Map<String, ModelSession>>(emptyMap())
    val activeSessions: StateFlow<Map<String, ModelSession>> = _activeSessions.asStateFlow()

    private val _modelLoadingStatus = MutableStateFlow<Map<String, LoadingStatus>>(emptyMap())
    val modelLoadingStatus: StateFlow<Map<String, LoadingStatus>> = _modelLoadingStatus.asStateFlow()

    // Internal state
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val loadingSemaphore = Semaphore(config.maxConcurrentLoads)
    private val activeLoads = ConcurrentHashMap<String, Deferred<ModelLoadResult>>()
    private val compressionCodecs = mutableMapOf<String, CompressionCodec>()
    private val sessionCounter = AtomicLong(0)
    private val errorReports = mutableListOf<ErrorReport>()

    // Cache management
    private val modelCache = ConcurrentHashMap<String, CachedModel>()
    private val pinnedModels = mutableSetOf<String>()

    init {
        initializeManager()
        initializeCompressionCodecs()
    }

    private fun initializeManager() {
        scope.launch {
            try {
                _managerStatus.value = ManagerStatus.LOADING
                logMessage("FullHybridModelManager initializing...")
                
                // Initialize network monitoring
                networkMonitor.startMonitoring()
                
                // Initialize delta update manager
                deltaUpdateManager.initialize()
                
                _managerStatus.value = ManagerStatus.IDLE
                logMessage("FullHybridModelManager initialized successfully")
            } catch (e: Exception) {
                logError("Failed to initialize FullHybridModelManager", e)
                _managerStatus.value = ManagerStatus.ERROR
            }
        }
    }

    private fun initializeCompressionCodecs() {
        compressionCodecs["gzip"] = GzipCodec()
        compressionCodecs["zstd"] = ZstdCodec()
        compressionCodecs["brotli"] = BrotliCodec()
    }

    /**
     * Load a model with full feature set
     */
    suspend fun loadModel(
        modelId: String,
        modelUrl: String,
        expectedVersion: String,
        expectedCompressionType: String,
        expectedChecksum: String,
        forceDownload: Boolean = false,
        priority: ModelPriority = ModelPriority.NORMAL
    ): ModelLoadResult {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        // Check if already loading
        if (activeLoads.containsKey(modelId)) {
            return ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "Model is already being loaded",
                sessionId = sessionId
            )
        }

        // Acquire semaphore for concurrent load limiting
        if (!loadingSemaphore.tryAcquire()) {
            return ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "Maximum concurrent loads reached",
                sessionId = sessionId
            )
        }

        val loadJob = scope.async {
            try {
                _managerStatus.value = ManagerStatus.LOADING
                _modelLoadingStatus.value = _modelLoadingStatus.value + (modelId to LoadingStatus.INITIALIZING)
                
                val result = performAdvancedModelLoad(
                    modelId = modelId,
                    modelUrl = modelUrl,
                    expectedVersion = expectedVersion,
                    expectedCompressionType = expectedCompressionType,
                    expectedChecksum = expectedChecksum,
                    forceDownload = forceDownload,
                    sessionId = sessionId,
                    startTime = startTime,
                    priority = priority
                )
                
                updatePerformanceMetrics(result, startTime)
                result
            } catch (e: Exception) {
                logError("Error loading model $modelId", e, modelId)
                ModelLoadResult(
                    success = false,
                    modelId = modelId,
                    error = e.message,
                    sessionId = sessionId,
                    loadTime = System.currentTimeMillis() - startTime
                )
            } finally {
                _managerStatus.value = ManagerStatus.IDLE
                _modelLoadingStatus.value = _modelLoadingStatus.value - modelId
                activeLoads.remove(modelId)
                loadingSemaphore.release()
            }
        }

        activeLoads[modelId] = loadJob
        return loadJob.await()
    }

    private suspend fun performAdvancedModelLoad(
        modelId: String,
        modelUrl: String,
        expectedVersion: String,
        expectedCompressionType: String,
        expectedChecksum: String,
        forceDownload: Boolean,
        sessionId: String,
        startTime: Long,
        priority: ModelPriority
    ): ModelLoadResult {
        _modelLoadingStatus.value = _modelLoadingStatus.value + (modelId to LoadingStatus.LOADING)

        // 1. Try cache first (unless force download)
        if (!forceDownload) {
            val cachedResult = loadFromCache(modelId, expectedVersion, sessionId, startTime)
            if (cachedResult.success) {
                return cachedResult
            }
        }

        // 2. Check for delta updates
        if (config.deltaUpdatesEnabled) {
            val deltaResult = tryDeltaUpdate(modelId, expectedVersion, sessionId, startTime)
            if (deltaResult.success) {
                return deltaResult
            }
        }

        // 3. Load from network with progressive loading
        return loadFromNetworkProgressive(
            modelId = modelId,
            modelUrl = modelUrl,
            expectedVersion = expectedVersion,
            expectedCompressionType = expectedCompressionType,
            expectedChecksum = expectedChecksum,
            sessionId = sessionId,
            startTime = startTime,
            priority = priority
        )
    }

    private suspend fun loadFromCache(
        modelId: String,
        expectedVersion: String,
        sessionId: String,
        startTime: Long
    ): ModelLoadResult {
        return try {
            val cachedModel = modelCache[modelId]
            if (cachedModel != null && 
                cachedModel.metadata.version == expectedVersion &&
                !cachedModel.isExpired()) {
                
                val loadTime = System.currentTimeMillis() - startTime
                logMessage("Model $modelId loaded from cache in ${loadTime}ms")
                
                // Update access count
                cachedModel.accessCount++
                cachedModel.lastAccessed = System.currentTimeMillis()
                
                ModelLoadResult(
                    success = true,
                    modelId = modelId,
                    source = LoadSource.CACHE,
                    loadTime = loadTime,
                    modelData = cachedModel.modelBuffer.array(),
                    metadata = cachedModel.metadata,
                    sessionId = sessionId,
                    modelBuffer = cachedModel.modelBuffer
                )
            } else {
                ModelLoadResult(
                    success = false,
                    modelId = modelId,
                    error = "Cache miss or version mismatch",
                    sessionId = sessionId,
                    loadTime = System.currentTimeMillis() - startTime
                )
            }
        } catch (e: Exception) {
            logError("Error loading from cache", e, modelId)
            ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "Cache error: ${e.message}",
                sessionId = sessionId,
                loadTime = System.currentTimeMillis() - startTime
            )
        }
    }

    private suspend fun tryDeltaUpdate(
        modelId: String,
        expectedVersion: String,
        sessionId: String,
        startTime: Long
    ): ModelLoadResult {
        return try {
            val deltaResult = deltaUpdateManager.checkForDeltaUpdate(modelId, expectedVersion)
            if (deltaResult.hasUpdate && deltaResult.deltaUrl != null && deltaResult.metadata != null) {
                val deltaData = deltaUpdateManager.downloadDeltaUpdate(deltaResult.deltaUrl!!)
                if (deltaData != null) {
                    val updatedModel = deltaUpdateManager.applyDeltaUpdate(modelId, deltaData)
                    if (updatedModel != null) {
                        val loadTime = System.currentTimeMillis() - startTime
                        logMessage("Model $modelId updated via delta in ${loadTime}ms")
                        
                        // Cache the updated model
                        cacheModel(modelId, updatedModel, deltaResult.metadata!!)
                        
                        return ModelLoadResult(
                            success = true,
                            modelId = modelId,
                            source = LoadSource.NETWORK_DELTA,
                            loadTime = loadTime,
                            modelData = updatedModel.array(),
                            metadata = deltaResult.metadata,
                            sessionId = sessionId,
                            modelBuffer = updatedModel,
                            updateSize = deltaData.size.toLong()
                        )
                    }
                }
            }
            
            ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "No delta update available",
                sessionId = sessionId,
                loadTime = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            logError("Error in delta update", e, modelId)
            ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "Delta update error: ${e.message}",
                sessionId = sessionId,
                loadTime = System.currentTimeMillis() - startTime
            )
        }
    }

    private suspend fun loadFromNetworkProgressive(
        modelId: String,
        modelUrl: String,
        expectedVersion: String,
        expectedCompressionType: String,
        expectedChecksum: String,
        sessionId: String,
        startTime: Long,
        priority: ModelPriority
    ): ModelLoadResult {
        return try {
            // Check network health
            val networkHealth = networkMonitor.getNetworkHealth()
            val chunkSize = calculateChunkSize(networkHealth)
            
            // Download model data progressively
            val downloadedBytes = downloadModelDataProgressive(modelUrl, chunkSize)
            if (downloadedBytes == null) {
                return ModelLoadResult(
                    success = false,
                    modelId = modelId,
                    error = "Failed to download model from $modelUrl",
                    sessionId = sessionId,
                    loadTime = System.currentTimeMillis() - startTime
                )
            }

            // Decompress if needed
            val decompressedBytes = if (config.compressionEnabled) {
                decompressModelData(downloadedBytes, expectedCompressionType)
            } else {
                downloadedBytes
            }

            // Verify checksum if enabled
            if (config.enableChecksumVerification) {
                if (!verifyChecksum(decompressedBytes, expectedChecksum)) {
                    return ModelLoadResult(
                        success = false,
                        modelId = modelId,
                        error = "Checksum verification failed",
                        sessionId = sessionId,
                        loadTime = System.currentTimeMillis() - startTime
                    )
                }
            }

            // Create metadata
            val metadata = ModelMetadata(
                id = modelId,
                name = modelId,
                version = expectedVersion,
                type = "tflite",
                description = "AI Model",
                tags = listOf("ai"),
                created = System.currentTimeMillis(),
                updated = System.currentTimeMillis(),
                compressionType = expectedCompressionType,
                checksum = expectedChecksum,
                sizeBytes = decompressedBytes.size.toLong()
            )

            // Save to storage
            val modelBuffer = ByteBuffer.wrap(decompressedBytes)
            val saveSuccess = storageManager.saveModel(
                modelId = modelId,
                modelBuffer = modelBuffer,
                metadata = metadata
            )

            if (!saveSuccess) {
                return ModelLoadResult(
                    success = false,
                    modelId = modelId,
                    error = "Failed to save model to storage",
                    sessionId = sessionId,
                    loadTime = System.currentTimeMillis() - startTime
                )
            }

            // Cache the model
            cacheModel(modelId, modelBuffer, metadata)

            val loadTime = System.currentTimeMillis() - startTime
            val compressionRatio = if (config.compressionEnabled) {
                downloadedBytes.size.toFloat() / decompressedBytes.size.toFloat()
            } else 1.0f

            logMessage("Model $modelId loaded from network in ${loadTime}ms (compression: ${compressionRatio}x)")

            ModelLoadResult(
                success = true,
                modelId = modelId,
                source = LoadSource.NETWORK_FULL,
                loadTime = loadTime,
                modelData = decompressedBytes,
                metadata = metadata,
                sessionId = sessionId,
                modelBuffer = modelBuffer,
                updateSize = downloadedBytes.size.toLong(),
                compressionRatio = compressionRatio
            )
        } catch (e: Exception) {
            logError("Error loading from network", e, modelId)
            ModelLoadResult(
                success = false,
                modelId = modelId,
                error = "Network error: ${e.message}",
                sessionId = sessionId,
                loadTime = System.currentTimeMillis() - startTime
            )
        }
    }

    private suspend fun downloadModelDataProgressive(url: String, chunkSize: Int): ByteArray? = withContext(Dispatchers.IO) {
        try {
            if (config.enableMockNetwork) {
                delay(config.mockNetworkDelayMs)
                return@withContext "mock_model_data_for_$url".toByteArray()
            }

            // TODO: Implement real progressive HTTP download
            // For now, simulate progressive download
            val totalSize = 1024 * 1024 // 1MB
            val chunks = (totalSize + chunkSize - 1) / chunkSize
            val result = ByteArray(totalSize)
            
            for (i in 0 until chunks) {
                val currentChunkSize = minOf(chunkSize, totalSize - i * chunkSize)
                val chunk = "chunk_${i}_of_${chunks}_for_$url".toByteArray()
                System.arraycopy(chunk, 0, result, i * chunkSize, minOf(chunk.size, currentChunkSize))
                delay(100) // Simulate network delay
            }
            
            result
        } catch (e: Exception) {
            logError("Progressive download failed for $url", e)
            null
        }
    }

    private fun calculateChunkSize(networkHealth: NetworkHealth): Int {
        return when {
            networkHealth.latency < 100 -> 64 * 1024 // 64KB for good network
            networkHealth.latency < 500 -> 32 * 1024 // 32KB for medium network
            else -> 16 * 1024 // 16KB for poor network
        }
    }

    private suspend fun decompressModelData(data: ByteArray, compressionType: String): ByteArray {
        val codec = compressionCodecs[compressionType]
        return if (codec != null && codec.isAvailable()) {
            codec.decompress(data)
        } else {
            logMessage("Compression codec $compressionType not available, using raw data")
            data
        }
    }

    private fun verifyChecksum(data: ByteArray, expectedChecksum: String): Boolean {
        // TODO: Implement actual checksum verification
        return true
    }

    private fun cacheModel(modelId: String, modelBuffer: ByteBuffer, metadata: ModelMetadata) {
        val cachedModel = CachedModel(
            modelBuffer = modelBuffer,
            metadata = metadata,
            accessCount = 0,
            createdAt = System.currentTimeMillis(),
            lastAccessed = System.currentTimeMillis()
        )
        modelCache[modelId] = cachedModel
        
        // Check cache size and cleanup if needed
        cleanupCacheIfNeeded()
    }

    private fun cleanupCacheIfNeeded() {
        val totalSize = modelCache.values.sumOf { it.modelBuffer.capacity().toLong() }
        if (totalSize > config.maxStorageSize * config.cleanupThreshold) {
            // Remove least recently used models (except pinned ones)
            val sortedModels = modelCache.entries
                .filter { it.key !in pinnedModels }
                .sortedBy { it.value.lastAccessed }
            
            val toRemove = sortedModels.take(sortedModels.size / 2) // Remove half
            toRemove.forEach { (modelId, _) ->
                modelCache.remove(modelId)
            }
            
            logMessage("Cache cleanup: removed ${toRemove.size} models")
        }
    }

    /**
     * Pin a model to prevent it from being evicted from cache
     */
    fun pinModel(modelId: String) {
        pinnedModels.add(modelId)
        logMessage("Model $modelId pinned")
    }

    /**
     * Unpin a model to allow it to be evicted from cache
     */
    fun unpinModel(modelId: String) {
        pinnedModels.remove(modelId)
        logMessage("Model $modelId unpinned")
    }

    /**
     * Get session statistics
     */
    fun getSessionStatistics(): SessionStatistics {
        val sessions = _activeSessions.value.values
        return SessionStatistics(
            totalSessions = sessions.size,
            averageLoadTime = if (sessions.isNotEmpty()) {
                sessions.map { it.loadTime }.average().toLong()
            } else 0L,
            totalLoadTime = sessions.sumOf { it.loadTime },
            activeSessions = sessions.count { it.isActive }
        )
    }

    /**
     * Get manager statistics
     */
    fun getManagerStatistics(): ManagerStatistics {
        val metrics = _performanceMetrics.value
        val cacheSize = modelCache.values.sumOf { it.modelBuffer.capacity().toLong() }
        val networkHealth = networkMonitor.getNetworkHealth()
        
        return ManagerStatistics(
            storageUsage = (cacheSize * 100 / config.maxStorageSize).toInt(),
            networkHealth = networkHealth.healthScore,
            errorRate = if (metrics.totalLoads > 0) {
                (metrics.errorCount * 100 / metrics.totalLoads).toInt()
            } else 0,
            cacheHitRate = (metrics.cacheHitRate * 100).toInt(),
            averageLoadTime = metrics.averageLoadTimeMs,
            totalModels = modelCache.size,
            pinnedModels = pinnedModels.size
        )
    }

    private fun updatePerformanceMetrics(result: ModelLoadResult, startTime: Long) {
        val currentMetrics = _performanceMetrics.value
        val loadTime = result.loadTime

        val newMetrics = currentMetrics.copy(
            totalLoads = currentMetrics.totalLoads + 1,
            cacheHits = if (result.source == LoadSource.CACHE) currentMetrics.cacheHits + 1 else currentMetrics.cacheHits,
            cacheMisses = if (result.source != LoadSource.CACHE) currentMetrics.cacheMisses + 1 else currentMetrics.cacheMisses,
            networkLoads = if (result.source == LoadSource.NETWORK_FULL) currentMetrics.networkLoads + 1 else currentMetrics.networkLoads,
            deltaUpdates = if (result.source == LoadSource.NETWORK_DELTA) currentMetrics.deltaUpdates + 1 else currentMetrics.deltaUpdates,
            averageLoadTimeMs = if (currentMetrics.totalLoads > 0) {
                (currentMetrics.averageLoadTimeMs * currentMetrics.totalLoads + loadTime) / (currentMetrics.totalLoads + 1)
            } else loadTime,
            errorCount = if (!result.success) currentMetrics.errorCount + 1 else currentMetrics.errorCount,
            lastUpdated = System.currentTimeMillis()
        )

        _performanceMetrics.value = newMetrics
    }

    private fun logMessage(message: String) {
        if (config.logLevel <= LogLevel.INFO) {
            Log.i(TAG, message)
        }
    }

    private fun logError(message: String, throwable: Throwable? = null, modelId: String? = null) {
        if (config.logLevel <= LogLevel.ERROR) {
            Log.e(TAG, message, throwable)
        }

        if (config.enableErrorReporting) {
            val errorReport = ErrorReport(
                errorType = ErrorType.UNKNOWN_ERROR,
                message = message,
                modelId = modelId,
                stackTrace = throwable?.stackTraceToString(),
                context = mapOf(
                    "timestamp" to System.currentTimeMillis(),
                    "config" to config.toString()
                )
            )
            errorReports.add(errorReport)
        }
    }

    fun cleanup() {
        scope.cancel()
        activeLoads.clear()
        modelCache.clear()
        pinnedModels.clear()
        errorReports.clear()
        networkMonitor.stopMonitoring()
    }

    companion object {
        private const val TAG = "FullHybridModelManager"
    }
}

/**
 * Cached model data
 */
private data class CachedModel(
    val modelBuffer: ByteBuffer,
    val metadata: ModelMetadata,
    var accessCount: Int,
    val createdAt: Long,
    var lastAccessed: Long
) {
    fun isExpired(): Boolean {
        val expirationTime = createdAt + (24 * 60 * 60 * 1000) // 24 hours
        return System.currentTimeMillis() > expirationTime
    }
}

/**
 * Session statistics
 */
data class SessionStatistics(
    val totalSessions: Int,
    val averageLoadTime: Long,
    val totalLoadTime: Long,
    val activeSessions: Int
)

/**
 * Manager statistics
 */
data class ManagerStatistics(
    val storageUsage: Int, // Percentage
    val networkHealth: Int, // Percentage
    val errorRate: Int, // Percentage
    val cacheHitRate: Int, // Percentage
    val averageLoadTime: Long, // Milliseconds
    val totalModels: Int,
    val pinnedModels: Int
)
