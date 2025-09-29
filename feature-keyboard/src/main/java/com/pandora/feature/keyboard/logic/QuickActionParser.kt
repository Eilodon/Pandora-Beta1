package com.pandora.feature.keyboard.logic

import android.content.Context
import com.pandora.core.cac.db.CACDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.regex.Pattern
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Quick Action Parser
 * Parses natural language text to extract Quick Actions and parameters
 */
@Singleton
class QuickActionParser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "QuickActionParser"
        private const val MIN_CONFIDENCE_THRESHOLD = 0.3f
        private const val MAX_SUGGESTIONS = 3
        
        // Regex patterns for parameter extraction
        private val TIME_PATTERNS = listOf(
            Pattern.compile("(\\d{1,2}):(\\d{2})\\s*(am|pm)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d{1,2})\\s*(am|pm)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(sáng|chiều|tối|đêm|morning|afternoon|evening|night)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(hôm nay|today|ngày mai|tomorrow|tuần sau|next week)", Pattern.CASE_INSENSITIVE)
        )
        
        private val CONTACT_PATTERNS = listOf(
            Pattern.compile("(gửi|send|nhắn|message)\\s+(cho|to)\\s+([a-zA-ZÀ-ỹ\\s]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([a-zA-ZÀ-ỹ\\s]+)\\s+(\\d{10,11})", Pattern.CASE_INSENSITIVE)
        )
        
        private val LOCATION_PATTERNS = listOf(
            Pattern.compile("(ở|at|tại)\\s+([a-zA-ZÀ-ỹ\\s,.-]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(địa chỉ|address|location)\\s+([a-zA-ZÀ-ỹ\\s,.-]+)", Pattern.CASE_INSENSITIVE)
        )
        
        private val AMOUNT_PATTERNS = listOf(
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(%|percent|phần trăm)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(USD|VND|EUR|GBP|JPY)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(km|m|cm|kg|g|l|ml)", Pattern.CASE_INSENSITIVE)
        )
    }
    
    /**
     * Parse text and extract Quick Action suggestions
     */
    fun parseText(text: String): Flow<List<QuickActionSuggestion>> = flow {
        try {
            val suggestions = mutableListOf<QuickActionSuggestion>()
            
            // 1. Direct action type detection
            val directAction = QuickActionType.fromText(text)
            if (directAction != null) {
                val suggestion = createSuggestion(directAction, text, 0.8f)
                suggestions.add(suggestion)
            }
            
            // 2. Context-based suggestions
            val contextSuggestions = getContextBasedSuggestions(text)
            suggestions.addAll(contextSuggestions)
            
            // 3. Pattern-based suggestions
            val patternSuggestions = getPatternBasedSuggestions(text)
            suggestions.addAll(patternSuggestions)
            
            // 4. Sort by confidence and limit results
            val sortedSuggestions = suggestions
                .distinctBy { it.actionType }
                .sortedByDescending { it.confidence }
                .take(MAX_SUGGESTIONS)
                .filter { it.confidence >= MIN_CONFIDENCE_THRESHOLD }
            
            emit(sortedSuggestions)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error parsing text: $text", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Create Quick Action Request from text
     */
    fun createRequest(text: String, actionType: QuickActionType): QuickActionRequest {
        val parameters = extractParameters(text, actionType)
        val confidence = calculateConfidence(text, actionType)
        
        return QuickActionRequest(
            text = text,
            actionType = actionType,
            parameters = parameters,
            confidence = confidence
        )
    }
    
    /**
     * Extract parameters from text based on action type
     */
    private fun extractParameters(text: String, actionType: QuickActionType): Map<String, String> {
        val parameters = mutableMapOf<String, String>()
        
        when (actionType) {
            QuickActionType.CALENDAR -> {
                extractTimeParameters(text, parameters)
                extractLocationParameters(text, parameters)
                parameters["title"] = extractTitle(text)
            }
            QuickActionType.REMIND -> {
                extractTimeParameters(text, parameters)
                parameters["message"] = extractMessage(text)
                parameters["title"] = extractTitle(text)
            }
            QuickActionType.SEND -> {
                extractContactParameters(text, parameters)
                extractLocationParameters(text, parameters)
                parameters["message"] = extractMessage(text)
            }
            QuickActionType.NOTE -> {
                parameters["content"] = extractContent(text)
                parameters["title"] = extractTitle(text)
            }
            QuickActionType.MATH -> {
                parameters["expression"] = extractMathExpression(text)
            }
            QuickActionType.CONVERSION -> {
                extractAmountParameters(text, parameters)
            }
            QuickActionType.SEARCH -> {
                parameters["query"] = extractSearchQuery(text)
            }
            QuickActionType.TEMPLATE -> {
                parameters["templateType"] = extractTemplateType(text)
            }
            QuickActionType.EXTRACT -> {
                parameters["extractType"] = extractExtractType(text)
                parameters["content"] = extractContent(text)
            }
            QuickActionType.TOGGLE -> {
                parameters["setting"] = extractSetting(text)
            }
        }
        
        return parameters
    }
    
    /**
     * Create suggestion from action type
     */
    private fun createSuggestion(
        actionType: QuickActionType,
        text: String,
        confidence: Float
    ): QuickActionSuggestion {
        val parameters = extractParameters(text, actionType)
        val displayText = generateDisplayText(actionType, parameters)
        
        return QuickActionSuggestion(
            actionType = actionType,
            displayText = displayText,
            confidence = confidence,
            parameters = parameters
        )
    }
    
    /**
     * Get context-based suggestions
     */
    private suspend fun getContextBasedSuggestions(text: String): List<QuickActionSuggestion> {
        val suggestions = mutableListOf<QuickActionSuggestion>()
        
        // Check recent memories for context
        try {
            val recentMemories = cacDao.getMemoriesBySource("keyboard", 5)
            recentMemories.forEach { memory ->
                if (memory.content.contains("calendar", ignoreCase = true)) {
                    suggestions.add(createSuggestion(QuickActionType.CALENDAR, text, 0.6f))
                }
                if (memory.content.contains("remind", ignoreCase = true)) {
                    suggestions.add(createSuggestion(QuickActionType.REMIND, text, 0.6f))
                }
            }
        } catch (e: Exception) {
            android.util.Log.w(TAG, "Error getting context suggestions", e)
        }
        
        return suggestions
    }
    
    /**
     * Get pattern-based suggestions
     */
    private fun getPatternBasedSuggestions(text: String): List<QuickActionSuggestion> {
        val suggestions = mutableListOf<QuickActionSuggestion>()
        
        // Time-based patterns
        if (TIME_PATTERNS.any { it.matcher(text).find() }) {
            suggestions.add(createSuggestion(QuickActionType.CALENDAR, text, 0.7f))
            suggestions.add(createSuggestion(QuickActionType.REMIND, text, 0.6f))
        }
        
        // Contact-based patterns
        if (CONTACT_PATTERNS.any { it.matcher(text).find() }) {
            suggestions.add(createSuggestion(QuickActionType.SEND, text, 0.7f))
        }
        
        // Location-based patterns
        if (LOCATION_PATTERNS.any { it.matcher(text).find() }) {
            suggestions.add(createSuggestion(QuickActionType.SEND, text, 0.6f))
        }
        
        // Math patterns
        if (text.contains(Regex("[+\\-*/=]")) || text.contains("tính", ignoreCase = true)) {
            suggestions.add(createSuggestion(QuickActionType.MATH, text, 0.8f))
        }
        
        return suggestions
    }
    
    /**
     * Extract time parameters
     */
    private fun extractTimeParameters(text: String, parameters: MutableMap<String, String>) {
        TIME_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val timeStr = matcher.group(0) ?: ""
                parameters["time"] = timeStr
                
                // Parse to specific time format
                val parsedTime = parseTime(timeStr)
                if (parsedTime != null) {
                    parameters["parsedTime"] = parsedTime
                }
            }
        }
    }
    
    /**
     * Extract contact parameters
     */
    private fun extractContactParameters(text: String, parameters: MutableMap<String, String>) {
        CONTACT_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val contact = matcher.group(3) ?: matcher.group(1)
                parameters["contact"] = contact?.trim() ?: ""
            }
        }
    }
    
    /**
     * Extract location parameters
     */
    private fun extractLocationParameters(text: String, parameters: MutableMap<String, String>) {
        LOCATION_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val location = matcher.group(2)
                parameters["location"] = location?.trim() ?: ""
            }
        }
    }
    
    /**
     * Extract amount parameters
     */
    private fun extractAmountParameters(text: String, parameters: MutableMap<String, String>) {
        AMOUNT_PATTERNS.forEach { pattern ->
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val amount = matcher.group(1)
                val unit = matcher.group(2)
                parameters["amount"] = amount ?: ""
                parameters["unit"] = unit ?: ""
            }
        }
    }
    
    /**
     * Extract title from text
     */
    private fun extractTitle(text: String): String {
        // Simple title extraction - first meaningful phrase
        val words = text.split(" ")
        return words.take(3).joinToString(" ")
    }
    
    /**
     * Extract message content
     */
    private fun extractMessage(text: String): String {
        // Remove action keywords and extract message
        val actionKeywords = listOf("gửi", "send", "nhắn", "message", "nhắc", "remind")
        var message = text
        actionKeywords.forEach { keyword ->
            message = message.replace(Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE), "")
        }
        return message.trim()
    }
    
    /**
     * Extract content for notes
     */
    private fun extractContent(text: String): String {
        val noteKeywords = listOf("note", "ghi chú", "memo", "ghi nhớ")
        var content = text
        noteKeywords.forEach { keyword ->
            content = content.replace(Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE), "")
        }
        return content.trim()
    }
    
    /**
     * Extract math expression
     */
    private fun extractMathExpression(text: String): String {
        // Extract mathematical expression
        val mathPattern = Pattern.compile("(\\d+(?:\\.\\d+)?\\s*[+\\-*/]\\s*\\d+(?:\\.\\d+)?)")
        val matcher = mathPattern.matcher(text)
        return if (matcher.find()) matcher.group(1) ?: text else text
    }
    
    /**
     * Extract search query
     */
    private fun extractSearchQuery(text: String): String {
        val searchKeywords = listOf("tìm", "search", "lookup", "tìm kiếm")
        var query = text
        searchKeywords.forEach { keyword ->
            query = query.replace(Regex("\\b$keyword\\b", RegexOption.IGNORE_CASE), "")
        }
        return query.trim()
    }
    
    /**
     * Extract template type
     */
    private fun extractTemplateType(text: String): String {
        val templateKeywords = listOf("email", "thư", "message", "tin nhắn", "report", "báo cáo")
        return templateKeywords.find { text.contains(it, ignoreCase = true) } ?: "general"
    }
    
    /**
     * Extract extract type
     */
    private fun extractExtractType(text: String): String {
        val extractKeywords = listOf("phone", "số điện thoại", "email", "thư", "url", "link")
        return extractKeywords.find { text.contains(it, ignoreCase = true) } ?: "text"
    }
    
    /**
     * Extract setting for toggle
     */
    private fun extractSetting(text: String): String {
        val settingKeywords = listOf("wifi", "bluetooth", "location", "vị trí", "sound", "âm thanh")
        return settingKeywords.find { text.contains(it, ignoreCase = true) } ?: "general"
    }
    
    /**
     * Parse time string to specific format
     */
    private fun parseTime(timeStr: String): String? {
        return try {
            
            when {
                timeStr.contains("sáng", ignoreCase = true) -> "08:00"
                timeStr.contains("chiều", ignoreCase = true) -> "14:00"
                timeStr.contains("tối", ignoreCase = true) -> "19:00"
                timeStr.contains("đêm", ignoreCase = true) -> "22:00"
                timeStr.contains("hôm nay", ignoreCase = true) -> "today"
                timeStr.contains("ngày mai", ignoreCase = true) -> "tomorrow"
                else -> timeStr
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Generate display text for suggestion
     */
    private fun generateDisplayText(actionType: QuickActionType, parameters: Map<String, String>): String {
        return when (actionType) {
            QuickActionType.CALENDAR -> {
                val title = parameters["title"] ?: "Sự kiện"
                val time = parameters["time"] ?: ""
                "Thêm vào lịch: $title $time"
            }
            QuickActionType.REMIND -> {
                val message = parameters["message"] ?: "Nhắc nhở"
                val time = parameters["time"] ?: ""
                "Nhắc nhở: $message $time"
            }
            QuickActionType.SEND -> {
                val contact = parameters["contact"] ?: "ai đó"
                "Gửi tin nhắn cho $contact"
            }
            QuickActionType.NOTE -> {
                val content = parameters["content"] ?: "Ghi chú"
                "Tạo ghi chú: $content"
            }
            QuickActionType.MATH -> {
                val expression = parameters["expression"] ?: "Tính toán"
                "Tính: $expression"
            }
            QuickActionType.CONVERSION -> {
                val amount = parameters["amount"] ?: ""
                val unit = parameters["unit"] ?: ""
                "Chuyển đổi: $amount $unit"
            }
            QuickActionType.SEARCH -> {
                val query = parameters["query"] ?: "Tìm kiếm"
                "Tìm: $query"
            }
            QuickActionType.TEMPLATE -> {
                val templateType = parameters["templateType"] ?: "template"
                "Sử dụng mẫu: $templateType"
            }
            QuickActionType.EXTRACT -> {
                val extractType = parameters["extractType"] ?: "thông tin"
                "Trích xuất: $extractType"
            }
            QuickActionType.TOGGLE -> {
                val setting = parameters["setting"] ?: "cài đặt"
                "Bật/tắt: $setting"
            }
        }
    }
    
    /**
     * Calculate confidence score
     */
    private fun calculateConfidence(text: String, actionType: QuickActionType): Float {
        var confidence = 0.0f
        
        // Base confidence from trigger words
        val triggerCount = actionType.triggers.count { text.contains(it, ignoreCase = true) }
        confidence += triggerCount * 0.3f
        
        // Boost confidence for specific patterns
        when (actionType) {
            QuickActionType.CALENDAR -> {
                if (TIME_PATTERNS.any { it.matcher(text).find() }) confidence += 0.3f
            }
            QuickActionType.REMIND -> {
                if (text.contains("nhắc", ignoreCase = true)) confidence += 0.2f
            }
            QuickActionType.SEND -> {
                if (CONTACT_PATTERNS.any { it.matcher(text).find() }) confidence += 0.3f
            }
            QuickActionType.MATH -> {
                if (text.contains(Regex("[+\\-*/=]"))) confidence += 0.4f
            }
            QuickActionType.NOTE -> {
                if (text.contains("ghi chú", ignoreCase = true)) confidence += 0.2f
            }
            QuickActionType.CONVERSION -> {
                if (AMOUNT_PATTERNS.any { it.matcher(text).find() }) confidence += 0.3f
            }
            QuickActionType.SEARCH -> {
                if (text.contains("tìm", ignoreCase = true)) confidence += 0.2f
            }
            QuickActionType.TEMPLATE -> {
                if (text.contains("template", ignoreCase = true)) confidence += 0.2f
            }
            QuickActionType.EXTRACT -> {
                if (text.contains("trích xuất", ignoreCase = true)) confidence += 0.2f
            }
            QuickActionType.TOGGLE -> {
                if (text.contains("bật", ignoreCase = true) || text.contains("tắt", ignoreCase = true)) confidence += 0.2f
            }
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
}
