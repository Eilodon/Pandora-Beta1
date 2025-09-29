# Quick Actions Architecture

## 🏗️ Kiến trúc Quick Actions System

```
Quick Actions System
├── QuickActionParser.kt          # Natural language processing
├── QuickActionExecutor.kt        # Action execution engine
├── QuickActionModels.kt          # Data models & enums
├── QuickActionUI.kt              # UI components
└── QuickActionManager.kt         # Main coordinator

## Core Components:

### 1. QuickActionParser
- Parse natural language: "Remind me call Nam 7pm"
- Extract action type, parameters, entities
- Confidence scoring
- Fallback handling

### 2. QuickActionExecutor
- Execute 10 core actions
- Integration with existing systems
- Error handling & recovery
- Success/failure feedback

### 3. QuickActionModels
- ActionType enum (10 types)
- ActionRequest/Response models
- Parameter extraction models
- Execution result models

### 4. QuickActionUI
- Smart Context Bar integration
- Action suggestion chips
- Progress indicators
- Result feedback

### 5. QuickActionManager
- Main coordinator
- Integration with InferenceEngine
- Learning from user interactions
- Performance optimization
```

## 📊 Action Types & Examples

| Action | Trigger | Example | Implementation |
|--------|---------|---------|----------------|
| calendar | "họp", "meeting", "lịch" | "Họp team 3pm" | Google Calendar API |
| remind | "nhắc", "remind", "nhớ" | "Remind me call Nam 7pm" | Notification + Keep |
| send | "gửi", "send", "share" | "Send location to Nam" | Intent to messaging apps |
| note | "ghi chú", "note", "memo" | "Note: Project deadline" | Google Keep |
| math | "tính", "calculate", "math" | "Calculate 15% of 200" | Math expression evaluator |
| conversion | "đổi", "convert", "chuyển" | "Convert 100 USD to VND" | Currency/unit converter |
| search | "tìm", "search", "lookup" | "Search for restaurants" | Google Search |
| template | "template", "mẫu", "form" | "Use email template" | Template system |
| extract | "trích xuất", "extract" | "Extract phone numbers" | Text extraction |
| toggle | "bật/tắt", "toggle", "switch" | "Toggle WiFi" | System settings |

## 🔄 Integration Flow

1. **Text Input** → QuickActionParser
2. **Parse Result** → QuickActionExecutor
3. **Action Execution** → External APIs/Apps
4. **Result** → QuickActionUI feedback
5. **Learning** → PersonalizationEngine
