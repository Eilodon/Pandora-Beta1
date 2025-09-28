package com.pandora.core.ai.storage

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model Storage Manager with LRU Eviction
 * Manages AI model storage with intelligent caching and quota management
 * Features: Pin/Unpin, LRU eviction, JSON indexing, quota management
 */
@Singleton
class ModelStorageManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IModelStorageManager {
    private val _storageStatus = MutableStateFlow<StorageStatus>(StorageStatus())
    val storageStatus: StateFlow<StorageStatus> = _storageStatus.asStateFlow()
    
    private val _cachedModels = MutableStateFlow<Map<String, CachedModel>>(emptyMap())
    val cachedModels: StateFlow<Map<String, CachedModel>> = _cachedModels.asStateFlow()
    
    private val storageDirectory = File(context.filesDir, "ai_models")
    private val indexFile = File(storageDirectory, "model_index.json")
    private val lruQueue = LinkedBlockingQueue<String>()
    private val pinnedModels = ConcurrentHashMap<String, Boolean>()
    
    // Storage configuration
    private val maxStorageSize = 500 * 1024 * 1024L // 500MB
    private val maxModelCount = 50
    private val cleanupThreshold = 0.8f // Start cleanup at 80% capacity
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    init {
        initializeStorage()
    }
    
    /**
     * Initialize storage directory and load index
     */
    private fun initializeStorage() {
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs()
        }
        
        loadModelIndex()
        startCleanupTask()
        
        Log.d("ModelStorageManager", "Storage initialized: ${storageDirectory.absolutePath}")
    }
    
    /**
     * Store a model with compression
     */
    suspend fun storeModel(
        modelId: String,
        modelData: ByteArray,
        metadata: ModelMetadata,
        compressionType: String = "gzip"
    ): StorageResult {
        return withContext(Dispatchers.IO) {
            try {
                val modelFile = File(storageDirectory, "$modelId.model")
                val compressedData = compressModelData(modelData, compressionType)
                
                // Write compressed model to file
                FileOutputStream(modelFile).use { fos ->
                    fos.write(compressedData)
                }
                
                val cachedModel = CachedModel(
                    id = modelId,
                    filePath = modelFile.absolutePath,
                    originalSize = modelData.size,
                    compressedSize = compressedData.size,
                    compressionType = compressionType,
                    metadata = metadata,
                    lastAccessed = System.currentTimeMillis(),
                    accessCount = 0,
                    isPinned = false
                )
                
                // Update in-memory cache
                updateCachedModel(cachedModel)
                
                // Update index
                updateModelIndex(cachedModel)
                
                // Check if cleanup is needed
                checkAndCleanup()
                
                StorageResult(
                    success = true,
                    modelId = modelId,
                    storedSize = compressedData.size,
                    compressionRatio = compressedData.size.toFloat() / modelData.size
                )
                
            } catch (e: Exception) {
                Log.e("ModelStorageManager", "Failed to store model: $modelId", e)
                StorageResult(
                    success = false,
                    modelId = modelId,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Load a model from storage
     */
    override suspend fun loadModel(modelId: String): LoadResult {
        return withContext(Dispatchers.IO) {
            try {
                val cachedModel = _cachedModels.value[modelId]
                if (cachedModel == null) {
                    return@withContext LoadResult(
                        success = false,
                        modelId = modelId,
                        error = "Model not found in cache"
                    )
                }
                
                val modelFile = File(cachedModel.filePath)
                if (!modelFile.exists()) {
                    return@withContext LoadResult(
                        success = false,
                        modelId = modelId,
                        error = "Model file not found"
                    )
                }
                
                // Read compressed data
                val compressedData = modelFile.readBytes()
                
                // Decompress data
                val modelData = decompressModelData(compressedData, cachedModel.compressionType)
                
                // Update access statistics
                updateModelAccess(modelId)
                
                LoadResult(
                    success = true,
                    modelId = modelId,
                    modelData = modelData,
                    metadata = cachedModel.metadata
                )
                
            } catch (e: Exception) {
                Log.e("ModelStorageManager", "Failed to load model: $modelId", e)
                LoadResult(
                    success = false,
                    modelId = modelId,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Pin a model to prevent eviction
     */
    suspend fun pinModel(modelId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedModel = _cachedModels.value[modelId]
                if (cachedModel != null) {
                    val updatedModel = cachedModel.copy(isPinned = true)
                    updateCachedModel(updatedModel)
                    pinnedModels[modelId] = true
                    updateModelIndex(updatedModel)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ModelStorageManager", "Failed to pin model: $modelId", e)
                false
            }
        }
    }
    
    /**
     * Unpin a model to allow eviction
     */
    suspend fun unpinModel(modelId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedModel = _cachedModels.value[modelId]
                if (cachedModel != null) {
                    val updatedModel = cachedModel.copy(isPinned = false)
                    updateCachedModel(updatedModel)
                    pinnedModels.remove(modelId)
                    updateModelIndex(updatedModel)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ModelStorageManager", "Failed to unpin model: $modelId", e)
                false
            }
        }
    }
    
    /**
     * Remove a model from storage
     */
    suspend fun removeModel(modelId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cachedModel = _cachedModels.value[modelId]
                if (cachedModel != null) {
                    // Delete file
                    File(cachedModel.filePath).delete()
                    
                    // Remove from cache
                    val currentModels = _cachedModels.value.toMutableMap()
                    currentModels.remove(modelId)
                    _cachedModels.value = currentModels
                    
                    // Remove from LRU queue
                    lruQueue.remove(modelId)
                    
                    // Remove from pinned models
                    pinnedModels.remove(modelId)
                    
                    // Update index
                    updateModelIndex()
                    
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e("ModelStorageManager", "Failed to remove model: $modelId", e)
                false
            }
        }
    }
    
    /**
     * Get storage statistics
     */
    override fun getStorageStatistics(): StorageStatistics {
        val models = _cachedModels.value
        val totalSize = models.values.sumOf { it.compressedSize.toLong() }
        val totalModels = models.size
        val pinnedCount = pinnedModels.size
        
            return StorageStatistics(
                totalSize = totalSize,
                maxSize = maxStorageSize,
                usagePercentage = (totalSize.toFloat() / maxStorageSize * 100).toLong(),
                totalModels = totalModels,
                maxModels = maxModelCount,
                pinnedModels = pinnedCount,
                availableSpace = (maxStorageSize - totalSize).toLong()
            )
    }
    
    /**
     * Compress model data
     */
    private fun compressModelData(data: ByteArray, compressionType: String): ByteArray {
        return when (compressionType.lowercase()) {
            "gzip" -> {
                val outputStream = ByteArrayOutputStream()
                GZIPOutputStream(outputStream).use { gz ->
                    gz.write(data)
                }
                outputStream.toByteArray()
            }
            "none" -> data
            else -> data
        }
    }
    
    /**
     * Decompress model data
     */
    override fun decompressModelData(data: ByteArray, compressionType: String): ByteArray {
        return when (compressionType.lowercase()) {
            "gzip" -> {
                val inputStream = ByteArrayInputStream(data)
                val outputStream = ByteArrayOutputStream()
                GZIPInputStream(inputStream).use { gz ->
                    gz.copyTo(outputStream)
                }
                outputStream.toByteArray()
            }
            "none" -> data
            else -> data
        }
    }
    
    /**
     * Update cached model
     */
    private fun updateCachedModel(model: CachedModel) {
        val currentModels = _cachedModels.value.toMutableMap()
        currentModels[model.id] = model
        _cachedModels.value = currentModels
    }
    
    /**
     * Update model access statistics
     */
    private fun updateModelAccess(modelId: String) {
        val currentModels = _cachedModels.value.toMutableMap()
        val model = currentModels[modelId]
        if (model != null) {
            val updatedModel = model.copy(
                lastAccessed = System.currentTimeMillis(),
                accessCount = model.accessCount + 1
            )
            currentModels[modelId] = updatedModel
            _cachedModels.value = currentModels
            
            // Update LRU queue
            lruQueue.remove(modelId)
            lruQueue.offer(modelId)
        }
    }
    
    /**
     * Load model index from file
     */
    private fun loadModelIndex() {
        try {
            if (indexFile.exists()) {
                val indexContent = indexFile.readText()
                val indexJson = JSONObject(indexContent)
                
                val models = mutableMapOf<String, CachedModel>()
                val pinned = mutableMapOf<String, Boolean>()
                
                indexJson.keys().forEach { key ->
                    val modelJson = indexJson.getJSONObject(key)
                    val model = CachedModel.fromJson(modelJson)
                    models[key] = model
                    
                    if (model.isPinned) {
                        pinned[key] = true
                    }
                }
                
                _cachedModels.value = models
                pinnedModels.putAll(pinned)
                
                // Rebuild LRU queue
                lruQueue.clear()
                models.values
                    .sortedByDescending { it.lastAccessed }
                    .forEach { lruQueue.offer(it.id) }
            }
        } catch (e: Exception) {
            Log.e("ModelStorageManager", "Failed to load model index", e)
        }
    }
    
    /**
     * Update model index file
     */
    private fun updateModelIndex(model: CachedModel? = null) {
        try {
            val indexJson = JSONObject()
            val models = _cachedModels.value
            
            models.forEach { (id, cachedModel) ->
                indexJson.put(id, cachedModel.toJson())
            }
            
            indexFile.writeText(indexJson.toString())
        } catch (e: Exception) {
            Log.e("ModelStorageManager", "Failed to update model index", e)
        }
    }
    
    /**
     * Check if cleanup is needed and perform it
     */
    private fun checkAndCleanup() {
        val statistics = getStorageStatistics()
        val needsCleanup = statistics.usagePercentage > (cleanupThreshold * 100) || 
                          statistics.totalModels > maxModelCount
        
        if (needsCleanup) {
            scope.launch {
                performCleanup()
            }
        }
    }
    
    /**
     * Perform LRU cleanup
     */
    private suspend fun performCleanup() {
        withContext(Dispatchers.IO) {
            val currentModels = _cachedModels.value.toMutableMap()
            val modelsToRemove = mutableListOf<String>()
            
            // Remove unpinned models in LRU order
            while (lruQueue.isNotEmpty()) {
                val modelId = lruQueue.poll()
                val model = currentModels[modelId]
                
                if (model != null && !model.isPinned) {
                    modelsToRemove.add(modelId)
                    
                    // Check if we've freed enough space
                    val statistics = getStorageStatistics()
                    if (statistics.usagePercentage < (cleanupThreshold * 100) && 
                        statistics.totalModels < maxModelCount) {
                        break
                    }
                }
            }
            
            // Remove selected models
            modelsToRemove.forEach { modelId ->
                val model = currentModels[modelId]
                if (model != null) {
                    File(model.filePath).delete()
                    currentModels.remove(modelId)
                }
            }
            
            _cachedModels.value = currentModels
            updateModelIndex()
            
            Log.d("ModelStorageManager", "Cleanup completed: removed ${modelsToRemove.size} models")
        }
    }
    
    /**
     * Start periodic cleanup task
     */
    private fun startCleanupTask() {
        scope.launch {
            while (isActive) {
                delay(300000) // 5 minutes
                checkAndCleanup()
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        scope.cancel()
    }
    
    /**
     * Save model to storage
     */
    override suspend fun saveModel(modelId: String, modelBuffer: java.nio.ByteBuffer, metadata: ModelMetadata): Boolean {
        return try {
            val modelData = modelBuffer.array()
            val compressedData = compressModelData(modelData, metadata.compressionType)
            
            val modelFile = File(storageDirectory, "$modelId.${metadata.compressionType}")
            modelFile.writeBytes(compressedData)
            
            val cachedModel = CachedModel(
                id = modelId,
                metadata = metadata,
                filePath = modelFile.absolutePath,
                originalSize = modelData.size,
                compressedSize = compressedData.size,
                compressionType = metadata.compressionType,
                lastAccessed = System.currentTimeMillis(),
                accessCount = 0,
                isPinned = false
            )
            
            updateCachedModel(cachedModel)
            updateModelIndex(cachedModel)
            
            true
        } catch (e: Exception) {
            Log.e("ModelStorageManager", "Failed to save model $modelId", e)
            false
        }
    }
    
    /**
     * Delete model from storage
     */
    override suspend fun deleteModel(modelId: String): Boolean {
        return try {
            val cachedModel = _cachedModels.value[modelId]
            if (cachedModel != null) {
                val modelFile = File(cachedModel.filePath)
                if (modelFile.exists()) {
                    modelFile.delete()
                }
                
                _cachedModels.value = _cachedModels.value.toMutableMap().apply {
                    remove(modelId)
                }
                
                updateModelIndex(null)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("ModelStorageManager", "Failed to delete model $modelId", e)
            false
        }
    }
    
}

/**
 * Data class for cached model
 */
data class CachedModel(
    val id: String,
    val filePath: String,
    val originalSize: Int,
    val compressedSize: Int,
    val compressionType: String,
    val metadata: ModelMetadata,
    val lastAccessed: Long,
    val accessCount: Int,
    val isPinned: Boolean
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("filePath", filePath)
            put("originalSize", originalSize)
            put("compressedSize", compressedSize)
            put("compressionType", compressionType)
            put("metadata", metadata.toJson())
            put("lastAccessed", lastAccessed)
            put("accessCount", accessCount)
            put("isPinned", isPinned)
        }
    }
    
    companion object {
        fun fromJson(json: JSONObject): CachedModel {
            val metadataJson = json.getJSONObject("metadata")
            val metadata = ModelMetadata.fromJson(metadataJson)
            
            return CachedModel(
                id = json.getString("id"),
                filePath = json.getString("filePath"),
                originalSize = json.getInt("originalSize"),
                compressedSize = json.getInt("compressedSize"),
                compressionType = json.getString("compressionType"),
                metadata = metadata,
                lastAccessed = json.getLong("lastAccessed"),
                accessCount = json.getInt("accessCount"),
                isPinned = json.getBoolean("isPinned")
            )
        }
    }
}

/**
 * Data class for model metadata
 */
data class ModelMetadata(
    val id: String,
    val name: String,
    val version: String,
    val type: String,
    val description: String,
    val tags: List<String>,
    val created: Long,
    val updated: Long,
    val compressionType: String,
    val checksum: String,
    val sizeBytes: Long
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("version", version)
            put("type", type)
            put("description", description)
            put("tags", JSONObject().apply {
                tags.forEachIndexed { index, tag ->
                    put("tag_$index", tag)
                }
            })
            put("created", created)
            put("updated", updated)
            put("compressionType", compressionType)
            put("checksum", checksum)
            put("sizeBytes", sizeBytes)
        }
    }
    
    companion object {
        fun fromJson(json: JSONObject): ModelMetadata {
            val tagsJson = json.getJSONObject("tags")
            val tags = mutableListOf<String>()
            tagsJson.keys().forEach { key ->
                tags.add(tagsJson.getString(key))
            }
            
            return ModelMetadata(
                id = json.getString("id"),
                name = json.getString("name"),
                version = json.getString("version"),
                type = json.getString("type"),
                description = json.getString("description"),
                tags = tags,
                created = json.getLong("created"),
                updated = json.getLong("updated"),
                compressionType = json.getString("compressionType"),
                checksum = json.getString("checksum"),
                sizeBytes = json.getLong("sizeBytes")
            )
        }
    }
}

/**
 * Data class for storage status
 */
data class StorageStatus(
    val isInitialized: Boolean = false,
    val totalModels: Int = 0,
    val totalSize: Long = 0,
    val availableSpace: Long = 0,
    val lastCleanup: Long = 0
)

/**
 * Data class for storage result
 */
data class StorageResult(
    val success: Boolean,
    val modelId: String,
    val storedSize: Int = 0,
    val compressionRatio: Float = 1.0f,
    val error: String? = null
)

/**
 * Data class for load result
 */
data class LoadResult(
    val success: Boolean,
    val modelId: String,
    val modelData: ByteArray? = null,
    val modelBuffer: java.nio.ByteBuffer? = null,
    val metadata: ModelMetadata? = null,
    val error: String? = null
)

/**
 * Data class for storage statistics
 */
data class StorageStatistics(
    val totalSize: Long,
    val maxSize: Long,
    val usagePercentage: Long,
    val totalModels: Int,
    val maxModels: Int,
    val pinnedModels: Int,
    val availableSpace: Long
)
