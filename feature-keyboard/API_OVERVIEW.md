# API Overview - feature-keyboard

Phiên bản: v0.1.0-chimera

`feature-keyboard` hiện thực Neural Keyboard (service + UI + logic) và tích hợp với `core-ai` để phân tích ngữ cảnh, gợi ý hành động, và chạy mini-flows.

## Kiến trúc module
- `NeuralKeyboardService` — IME service (entrypoint của bàn phím)
- `PandoraKeyboardView` — Compose UI của bàn phím
- `KeyboardViewModel` — State & intent xử lý từ UI
- `logic/` — các khối nghiệp vụ
  - `InferenceEngine` — gọi `core-ai` để phân tích văn bản
  - `ActionExecutor` — chuyển kết quả suy luận thành hành động cụ thể
  - `MiniFlows` — lịch, maps, camera, keep, spotify (tích hợp `core-ai.flows`)
  - `ActionModels` — model dữ liệu hành động

## Luồng xử lý
1) Người dùng nhập văn bản → `KeyboardViewModel`
2) Gọi `InferenceEngine` để phân tích (sử dụng `core-ai`)
3) Nhận lại gợi ý hành động → render Smart Context Bar ở `PandoraKeyboardView`
4) Người dùng chọn gợi ý → `ActionExecutor`/`MiniFlows` thực thi qua `FlowScheduler`

## Tích hợp với core-ai
- Phân tích (gợi ý intent, entity, sentiment): `AdvancedModelManager.analyzeTextAdvanced`
- Mini-Flows (nền background): `core-ai.flows.FlowScheduler` + `FlowWorker`

## Ví dụ sử dụng (rút gọn)
```kotlin
// Trong ViewModel
fun onTextChanged(text: String) {
    viewModelScope.launch {
        val ctx = TextContext(appPackage = currentAppPackage, recentTexts = listOf(text))
        advancedModelManager.analyzeTextAdvanced(text, ctx).collect { result ->
            _uiState.update { it.copy(suggestions = mapResultToSuggestions(result)) }
        }
    }
}

fun onSuggestionClick(action: PandoraAction) {
    viewModelScope.launch {
        miniFlows.executeFlow(action).collect { success ->
            _uiState.update { it.copy(lastActionSuccess = success) }
        }
    }
}
```

## Hợp đồng API chính
- `InferenceEngine.infer(text): Flow<AnalysisResult>`
- `MiniFlows.executeFlow(action: PandoraAction): Flow<Boolean>`
- `ActionExecutor.execute(action: PandoraAction)`

## UX Guidelines (Smart Context Bar)
- Hiển thị tối đa 3–5 gợi ý ưu tiên (score cao)
- Icon + nhãn ngắn gọn, có mô tả phụ khi cần
- Trạng thái “đang xử lý”: sử dụng ProgressIndicator nhỏ trong chip
- Thông điệp lỗi ngắn gọn, có nút thử lại (Retry)

## Thông điệp lỗi & Feedback
- Lỗi phân tích: "Không thể phân tích. Thử lại?"
- Lỗi quyền: "Thiếu quyền Camera/Location. Mở cài đặt?"
- Kết quả thành công: SnackBar ngắn gọn (ví dụ: "Đã tạo sự kiện lịch")
- Retry: `Retry` trong chip hoặc biểu tượng refresh

## Accessibility
- Touch target ≥ 48dp
- ContentDescription cho icon gợi ý
- Hỗ trợ font scaling và high-contrast

## Kiểm thử
- Unit test logic: `logic/*Test.kt`
- E2E/Instrumented (khuyến nghị): typing flow → suggestion → execute

---
Tài liệu này hỗ trợ tích hợp nhanh và chuẩn hoá UX cho Smart Context Bar.
