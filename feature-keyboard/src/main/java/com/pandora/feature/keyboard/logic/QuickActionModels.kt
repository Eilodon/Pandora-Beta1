package com.pandora.feature.keyboard.logic

/**
 * Quick Actions Models for Neural Keyboard
 * Supports 10 core actions with natural language processing
 */

/**
 * Quick Action Types - 10 core actions from blueprint
 */
enum class QuickActionType(
    val displayName: String,
    val triggers: List<String>,
    val description: String
) {
    CALENDAR(
        displayName = "Calendar",
        triggers = listOf("họp", "meeting", "lịch", "calendar", "sự kiện"),
        description = "Add events to calendar"
    ),
    REMIND(
        displayName = "Reminder",
        triggers = listOf("nhắc", "remind", "nhớ", "nhắc nhở", "reminder"),
        description = "Create reminders and notifications"
    ),
    SEND(
        displayName = "Send",
        triggers = listOf("gửi", "send", "share", "chia sẻ", "gửi tin"),
        description = "Send messages, locations, or content"
    ),
    NOTE(
        displayName = "Note",
        triggers = listOf("ghi chú", "note", "memo", "ghi nhớ", "keep"),
        description = "Create and save notes"
    ),
    MATH(
        displayName = "Math",
        triggers = listOf("tính", "calculate", "math", "toán", "tính toán"),
        description = "Perform mathematical calculations"
    ),
    CONVERSION(
        displayName = "Conversion",
        triggers = listOf("đổi", "convert", "chuyển", "chuyển đổi", "conversion"),
        description = "Convert units, currencies, or formats"
    ),
    SEARCH(
        displayName = "Search",
        triggers = listOf("tìm", "search", "lookup", "tìm kiếm", "search"),
        description = "Search for information"
    ),
    TEMPLATE(
        displayName = "Template",
        triggers = listOf("template", "mẫu", "form", "biểu mẫu", "template"),
        description = "Use predefined templates"
    ),
    EXTRACT(
        displayName = "Extract",
        triggers = listOf("trích xuất", "extract", "lấy", "extract", "trích"),
        description = "Extract information from text"
    ),
    TOGGLE(
        displayName = "Toggle",
        triggers = listOf("bật", "tắt", "toggle", "switch", "chuyển"),
        description = "Toggle system settings"
    );

    companion object {
        fun fromText(text: String): QuickActionType? {
            val lowerText = text.lowercase()
            return values().find { actionType ->
                actionType.triggers.any { trigger ->
                    lowerText.contains(trigger)
                }
            }
        }
    }
}

/**
 * Quick Action Request
 */
data class QuickActionRequest(
    val text: String,
    val actionType: QuickActionType,
    val parameters: Map<String, String> = emptyMap(),
    val confidence: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Quick Action Response
 */
data class QuickActionResponse(
    val request: QuickActionRequest,
    val success: Boolean,
    val result: String? = null,
    val error: String? = null,
    val executionTime: Long = 0L
)

/**
 * Quick Action Parameter Types
 */
enum class ParameterType {
    TEXT,           // Plain text
    TIME,           // Time/date
    LOCATION,       // Location/address
    CONTACT,        // Contact name/number
    AMOUNT,         // Number/amount
    CURRENCY,       // Currency code
    UNIT,           // Unit of measurement
    URL,            // Web URL
    EMAIL,          // Email address
    PHONE           // Phone number
}

/**
 * Extracted Parameter
 */
data class ExtractedParameter(
    val type: ParameterType,
    val value: String,
    val confidence: Float = 0.0f,
    val startIndex: Int = 0,
    val endIndex: Int = 0
)

/**
 * Quick Action Suggestion
 */
data class QuickActionSuggestion(
    val actionType: QuickActionType,
    val displayText: String,
    val confidence: Float,
    val parameters: Map<String, String> = emptyMap(),
    val icon: String? = null
)

/**
 * Quick Action Execution Context
 */
data class QuickActionContext(
    val currentApp: String? = null,
    val currentTime: Long = System.currentTimeMillis(),
    val location: String? = null,
    val recentActions: List<QuickActionType> = emptyList(),
    val userPreferences: Map<String, Any> = emptyMap()
)

/**
 * Quick Action Learning Data
 */
data class QuickActionLearningData(
    val actionType: QuickActionType,
    val originalText: String,
    val success: Boolean,
    val executionTime: Long,
    val userFeedback: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Quick Action Statistics
 */
data class QuickActionStats(
    val totalExecutions: Int = 0,
    val successRate: Float = 0.0f,
    val averageExecutionTime: Long = 0L,
    val mostUsedActions: List<QuickActionType> = emptyList(),
    val userSatisfaction: Float = 0.0f
)
