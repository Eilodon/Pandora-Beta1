# API Overview - core-ai

Phiên bản: v0.1.0-chimera

Mô-đun `core-ai` cung cấp các khối AI/Automation lõi cho PandoraOS: quản lý model, suy luận ngữ cảnh, cá nhân hoá, workflow automation, giám sát mạng và lưu trữ model.

## Kiến trúc gói
- `com.pandora.core.ai.ml`:
  - `AdvancedModelManager` (entrypoint phân tích nâng cao)
  - `TextContext`, `AnalysisResult`
- `com.pandora.core.ai.hybrid`:
  - `SimpleHybridModelManager` (quản lý tải model cơ bản)
  - `ModelLoadResult`, `LoadSource`, `ManagerStatus`
- `com.pandora.core.ai.storage`:
  - `ModelStorageManager` (LRU/quota, JSON index, nén/giải nén)
  - `IModelStorageManager`, `ModelMetadata`, `LoadResult`, `StorageStatistics`
- `com.pandora.core.ai.compression`:
  - `CompressionCodec`, `GzipCodec`, `ZstdCodec`, `BrotliCodec`
- `com.pandora.core.ai.network`:
  - `NetworkHealthMonitor`, `NetworkHealth`
- `com.pandora.core.ai.automation`:
  - `WorkflowEngine`, `WorkflowExecutor`, `TriggerManager`, `ConditionEvaluator`

## Khởi tạo (Hilt)
Các lớp chính có `@Inject` constructor hoặc được cung cấp qua `AIModule`. Ví dụ:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var advancedModelManager: AdvancedModelManager
}
```

## Sử dụng nhanh
### Phân tích văn bản (AdvancedModelManager)
```kotlin
val context = TextContext(
    timestamp = System.currentTimeMillis(),
    location = null,
    appPackage = packageName,
    userActivity = null,
    recentTexts = listOf(input)
)
advancedModelManager
    .analyzeTextAdvanced(input, context)
    .collect { result -> /* handle result */ }
```

### Tải model (SimpleHybridModelManager)
```kotlin
val result = simpleHybridModelManager.loadModel(
    modelId = "intent_model",
    modelUrl = "https://cdn/model.tflite",
    expectedVersion = "1.0.0",
    expectedCompressionType = "none",
    expectedChecksum = "<checksum>",
    forceDownload = false
)
if (result.success) { /* dùng result.modelBuffer */ }
```

### Lưu trữ model (ModelStorageManager)
```kotlin
val saved = modelStorageManager.saveModel(
    modelId = meta.id,
    modelBuffer = buffer,
    metadata = meta
)
```

## Hợp đồng API quan trọng
- `AdvancedModelManager.analyzeTextAdvanced(text, context): Flow<AnalysisResult>`
- `SimpleHybridModelManager.loadModel(...): ModelLoadResult`
- `IModelStorageManager.loadModel(id): LoadResult`
- `CompressionCodec.compress/decompress(data): ByteArray`
- `NetworkHealthMonitor.networkStatus: StateFlow<NetworkHealth>`
- `WorkflowEngine.executeWorkflow(id, ctx): Flow<WorkflowResult>`

## Kiểm thử & Benchmark
- `ModelLoadPerformanceTest` đo thời gian load model (network vs cache).
- Các `*Test.kt` khác minh hoạ sử dụng API.

## Bảo mật & hiệu năng
- R8 minify + shrinkResources đã bật ở bản release.
- Ưu tiên `zstd` nếu có, fallback `gzip`.
- Kiểm tra checksum trước khi lưu model mới.

Tham khảo thêm: `README_HYBRID_MODEL_MANAGER.md`.
