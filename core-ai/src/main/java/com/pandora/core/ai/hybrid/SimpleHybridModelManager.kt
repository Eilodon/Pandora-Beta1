package com.pandora.core.ai.hybrid

import android.content.Context
import android.util.Log
import com.pandora.core.ai.storage.ModelMetadata
import com.pandora.core.ai.storage.IModelStorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A simplified Hybrid Model Manager for initial integration and testing.
 *
 * Quản lý vòng đời model ở mức cơ bản, ưu tiên lấy từ cache; nếu không có,
 * giả lập tải mạng (~1s) rồi giải nén và lưu trữ cục bộ.
 *
 * Dùng cho benchmark, demo và tích hợp nhanh trước khi dùng bản đầy đủ.
 */
@Singleton
class SimpleHybridModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageManager: IModelStorageManager
) {
    private val _managerStatus = MutableStateFlow<ManagerStatus>(ManagerStatus.IDLE)
    val managerStatus: StateFlow<ManagerStatus> = _managerStatus.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Khởi tạo manager (non-blocking).
     * Chuyển trạng thái về [ManagerStatus.IDLE].
     */
    fun initialize() {
        scope.launch {
            _managerStatus.value = ManagerStatus.IDLE
            Log.d(TAG, "SimpleHybridModelManager initialized.")
        }
    }

    /**
     * Tải model theo `modelId`.
     *
     * @param modelId ID model duy nhất trong cache/storage
     * @param modelUrl URL nguồn tải model (dùng khi không có cache hoặc `forceDownload=true`)
     * @param expectedVersion Phiên bản mong đợi, để xác thực cache
     * @param expectedCompressionType Kiểu nén ("none"/"gzip"/"zstd"/"brotli"), dùng cho giải nén
     * @param expectedChecksum Checksum xác thực toàn vẹn dữ liệu
     * @param forceDownload Bỏ qua cache và buộc tải mới
     * @return [ModelLoadResult] gồm buffer, metadata, thời gian tải, nguồn tải
     */
    suspend fun loadModel(
        modelId: String,
        modelUrl: String,
        expectedVersion: String,
        expectedCompressionType: String,
        expectedChecksum: String,
        forceDownload: Boolean = false
    ): ModelLoadResult {
        val sessionId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()

        _managerStatus.value = ManagerStatus.LOADING

        return try {
            // 1. Try to load from cache first
            if (!forceDownload) {
                val cachedResult = storageManager.loadModel(modelId)
                if (cachedResult.success && cachedResult.metadata?.version == expectedVersion &&
                    cachedResult.modelBuffer != null) {
                    Log.d(TAG, "Model $modelId loaded from cache.")
                    val loadTime = System.currentTimeMillis() - startTime
                    _managerStatus.value = ManagerStatus.IDLE
                    return ModelLoadResult(
                        success = true,
                        modelId = modelId,
                        source = LoadSource.CACHE,
                        loadTime = loadTime,
                        modelData = cachedResult.modelBuffer.array(),
                        metadata = cachedResult.metadata,
                        sessionId = sessionId,
                        modelBuffer = cachedResult.modelBuffer
                    )
                }
            }

            // 2. Simulate network check (simplified)
            Log.d(TAG, "Simulating network check for model $modelId")

            // 3. Full download (simplified, no delta updates in this version)
            val downloadedBytes = downloadFile(modelUrl) ?: run {
                val errorMsg = "Failed to download full model $modelId"
                Log.e(TAG, errorMsg)
                return ModelLoadResult(success = false, modelId = modelId, source = LoadSource.NETWORK_FULL, loadTime = 0L, error = errorMsg)
            }

            val decompressedBytes = storageManager.decompressModelData(downloadedBytes, expectedCompressionType)
            val decompressedBuffer = ByteBuffer.wrap(decompressedBytes)

            // 4. Store the new model
            val newMetadata = ModelMetadata(
                id = modelId,
                version = expectedVersion,
                compressionType = expectedCompressionType,
                checksum = expectedChecksum,
                sizeBytes = decompressedBytes.size.toLong(),
                name = modelId,
                type = "tflite",
                description = "AI Model",
                tags = listOf("ai"),
                created = System.currentTimeMillis(),
                updated = System.currentTimeMillis()
            )
            val saveSuccess = storageManager.saveModel(modelId, ByteBuffer.wrap(decompressedBytes), newMetadata)

            if (!saveSuccess) {
                val errorMsg = "Failed to save model $modelId after download."
                Log.e(TAG, errorMsg)
                _managerStatus.value = ManagerStatus.ERROR
                return ModelLoadResult(success = false, modelId = modelId, source = null, loadTime = 0L, error = errorMsg)
            }

            val loadTime = System.currentTimeMillis() - startTime
            _managerStatus.value = ManagerStatus.IDLE
            return ModelLoadResult(
                success = true,
                modelId = modelId,
                source = LoadSource.NETWORK_FULL,
                loadTime = loadTime,
                modelData = decompressedBytes,
                metadata = newMetadata,
                sessionId = sessionId,
                modelBuffer = decompressedBuffer,
                updateSize = downloadedBytes.size.toLong(),
                compressionRatio = downloadedBytes.size.toFloat() / decompressedBytes.size.toFloat()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model $modelId: ${e.message}", e)
            _managerStatus.value = ManagerStatus.ERROR
            return ModelLoadResult(
                success = false,
                modelId = modelId,
                source = null,
                loadTime = 0L,
                error = e.message
            )
        }
    }

    /**
     * Gỡ model khỏi storage.
     * @param modelId ID model cần xoá
     * @return true nếu xoá thành công
     */
    suspend fun unloadModel(modelId: String): Boolean {
        return storageManager.deleteModel(modelId)
    }

    private suspend fun downloadFile(url: String): ByteArray? = withContext(Dispatchers.IO) {
        Log.d(TAG, "Simulating download from $url")
        delay(1000) // Simulate download time
        "dummy_model_content_for_$url".toByteArray()
    }

    companion object {
        private const val TAG = "SimpleHybridModelManager"
    }
}