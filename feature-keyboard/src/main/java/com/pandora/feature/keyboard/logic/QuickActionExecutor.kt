package com.pandora.feature.keyboard.logic

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.provider.Settings
import android.telephony.SmsManager
import android.widget.Toast
import com.pandora.core.cac.db.CACDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Quick Action Executor
 * Executes the 10 core Quick Actions
 */
@Singleton
class QuickActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "QuickActionExecutor"
    }
    
    /**
     * Execute Quick Action Request
     */
    fun executeRequest(request: QuickActionRequest): Flow<QuickActionResponse> = flow {
        val startTime = System.currentTimeMillis()
        
        try {
            val result = when (request.actionType) {
                QuickActionType.CALENDAR -> executeCalendarAction(request)
                QuickActionType.REMIND -> executeRemindAction(request)
                QuickActionType.SEND -> executeSendAction(request)
                QuickActionType.NOTE -> executeNoteAction(request)
                QuickActionType.MATH -> executeMathAction(request)
                QuickActionType.CONVERSION -> executeConversionAction(request)
                QuickActionType.SEARCH -> executeSearchAction(request)
                QuickActionType.TEMPLATE -> executeTemplateAction(request)
                QuickActionType.EXTRACT -> executeExtractAction(request)
                QuickActionType.TOGGLE -> executeToggleAction(request)
            }
            
            val executionTime = System.currentTimeMillis() - startTime
            
            val response = QuickActionResponse(
                request = request,
                success = result.success,
                result = result.message,
                error = result.error,
                executionTime = executionTime
            )
            
            // Save learning data
            saveLearningData(request, response)
            
            emit(response)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error executing action: ${request.actionType}", e)
            
            val response = QuickActionResponse(
                request = request,
                success = false,
                error = e.message ?: "Unknown error",
                executionTime = System.currentTimeMillis() - startTime
            )
            
            emit(response)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Execute Calendar Action
     */
    private fun executeCalendarAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val title = request.parameters["title"] ?: "Sự kiện mới"
            val time = request.parameters["parsedTime"] ?: request.parameters["time"] ?: ""
            val location = request.parameters["location"] ?: ""
            
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            // Parse time if available
            if (time.isNotEmpty()) {
                val calendar = Calendar.getInstance()
                when (time) {
                    "today" -> {
                        calendar.add(Calendar.HOUR_OF_DAY, 1)
                    }
                    "tomorrow" -> {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 9)
                    }
                    else -> {
                        // Try to parse time string
                        val timePattern = Regex("(\\d{1,2}):(\\d{2})")
                        val match = timePattern.find(time)
                        if (match != null) {
                            val hour = match.groupValues[1].toInt()
                            val minute = match.groupValues[2].toInt()
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                        }
                    }
                }
                intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, calendar.timeInMillis)
            }
            
            context.startActivity(intent)
            
            ExecutionResult(
                success = true,
                message = "Đã mở Calendar để thêm sự kiện: $title"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể mở Calendar: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Remind Action
     */
    private fun executeRemindAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val message = request.parameters["message"] ?: "Nhắc nhở"
            val time = request.parameters["parsedTime"] ?: request.parameters["time"] ?: ""
            
            // Create Keep note as reminder
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "🔔 $message $time")
                .setPackage("com.google.android.keep")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                context.startActivity(intent)
                ExecutionResult(
                    success = true,
                    message = "Đã tạo nhắc nhở: $message"
                )
            } catch (e: Exception) {
                // Fallback to general note app
                val fallbackIntent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "🔔 $message $time")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                context.startActivity(fallbackIntent)
                ExecutionResult(
                    success = true,
                    message = "Đã tạo nhắc nhở: $message"
                )
            }
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể tạo nhắc nhở: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Send Action
     */
    private fun executeSendAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val contact = request.parameters["contact"] ?: ""
            val message = request.parameters["message"] ?: ""
            val location = request.parameters["location"] ?: ""
            
            val content = if (location.isNotEmpty()) {
                "$message\n📍 $location"
            } else {
                message
            }
            
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, content)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
            
            ExecutionResult(
                success = true,
                message = "Đã mở ứng dụng gửi tin nhắn cho $contact"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể gửi tin nhắn: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Note Action
     */
    private fun executeNoteAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val content = request.parameters["content"] ?: "Ghi chú mới"
            val title = request.parameters["title"] ?: "Ghi chú"
            
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
                .setPackage("com.google.android.keep")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            try {
                context.startActivity(intent)
                ExecutionResult(
                    success = true,
                    message = "Đã tạo ghi chú: $title"
                )
            } catch (e: Exception) {
                // Fallback to general note app
                val fallbackIntent = Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, "$title\n\n$content")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                
                context.startActivity(fallbackIntent)
                ExecutionResult(
                    success = true,
                    message = "Đã tạo ghi chú: $title"
                )
            }
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể tạo ghi chú: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Math Action
     */
    private fun executeMathAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val expression = request.parameters["expression"] ?: ""
            val result = evaluateMathExpression(expression)
            
            ExecutionResult(
                success = true,
                message = "$expression = $result"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể tính toán: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Conversion Action
     */
    private fun executeConversionAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val amount = request.parameters["amount"]?.toDoubleOrNull() ?: 0.0
            val unit = request.parameters["unit"] ?: ""
            
            val result = when (unit.uppercase()) {
                "USD" -> "$${amount} = ${amount * 24000} VND"
                "VND" -> "${amount} VND = $${amount / 24000} USD"
                "KM", "km" -> "${amount} km = ${amount * 1000} m"
                "M", "m" -> "${amount} m = ${amount / 1000} km"
                "KG", "kg" -> "${amount} kg = ${amount * 1000} g"
                "G", "g" -> "${amount} g = ${amount / 1000} kg"
                "%", "PERCENT", "phần trăm" -> "${amount}% = ${amount / 100}"
                else -> "Không hỗ trợ chuyển đổi: $unit"
            }
            
            ExecutionResult(
                success = true,
                message = result
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể chuyển đổi: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Search Action
     */
    private fun executeSearchAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val query = request.parameters["query"] ?: ""
            
            val intent = Intent(Intent.ACTION_WEB_SEARCH)
                .putExtra("query", query)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
            
            ExecutionResult(
                success = true,
                message = "Đã tìm kiếm: $query"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể tìm kiếm: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Template Action
     */
    private fun executeTemplateAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val templateType = request.parameters["templateType"] ?: "general"
            val template = getTemplate(templateType)
            
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, template)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(intent)
            
            ExecutionResult(
                success = true,
                message = "Đã sử dụng template: $templateType"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể sử dụng template: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Extract Action
     */
    private fun executeExtractAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val content = request.parameters["content"] ?: ""
            val extractType = request.parameters["extractType"] ?: "text"
            
            val extracted = when (extractType) {
                "phone" -> extractPhoneNumbers(content)
                "email" -> extractEmails(content)
                "url" -> extractUrls(content)
                else -> content
            }
            
            ExecutionResult(
                success = true,
                message = "Trích xuất $extractType: $extracted"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể trích xuất: ${e.message}"
            )
        }
    }
    
    /**
     * Execute Toggle Action
     */
    private fun executeToggleAction(request: QuickActionRequest): ExecutionResult {
        return try {
            val setting = request.parameters["setting"] ?: "general"
            
            when (setting.lowercase()) {
                "wifi" -> {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "bluetooth" -> {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                "location" -> {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                else -> {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            }
            
            ExecutionResult(
                success = true,
                message = "Đã mở cài đặt: $setting"
            )
        } catch (e: Exception) {
            ExecutionResult(
                success = false,
                error = "Không thể mở cài đặt: ${e.message}"
            )
        }
    }
    
    /**
     * Evaluate math expression
     */
    private fun evaluateMathExpression(expression: String): String {
        return try {
            // Simple math evaluation (in production, use a proper math parser)
            when {
                expression.contains("+") -> {
                    val parts = expression.split("+")
                    val result = parts.map { it.trim().toDouble() }.sum()
                    result.toString()
                }
                expression.contains("-") -> {
                    val parts = expression.split("-")
                    val result = parts[0].trim().toDouble() - parts[1].trim().toDouble()
                    result.toString()
                }
                expression.contains("*") -> {
                    val parts = expression.split("*")
                    val result = parts.map { it.trim().toDouble() }.reduce { a, b -> a * b }
                    result.toString()
                }
                expression.contains("/") -> {
                    val parts = expression.split("/")
                    val result = parts[0].trim().toDouble() / parts[1].trim().toDouble()
                    result.toString()
                }
                else -> expression
            }
        } catch (e: Exception) {
            "Lỗi tính toán"
        }
    }
    
    /**
     * Get template by type
     */
    private fun getTemplate(templateType: String): String {
        return when (templateType.lowercase()) {
            "email" -> "Chào bạn,\n\nTôi viết email này để...\n\nTrân trọng,\n[Tên]"
            "message" -> "Xin chào,\n\n[Tin nhắn]\n\nCảm ơn!"
            "report" -> "BÁO CÁO\n\nNgày: [Ngày]\nNội dung: [Nội dung]\n\nKết luận: [Kết luận]"
            else -> "Template: $templateType\n\n[Nội dung]"
        }
    }
    
    /**
     * Extract phone numbers
     */
    private fun extractPhoneNumbers(text: String): String {
        val phonePattern = Regex("\\b\\d{10,11}\\b")
        return phonePattern.findAll(text).map { it.value }.joinToString(", ")
    }
    
    /**
     * Extract emails
     */
    private fun extractEmails(text: String): String {
        val emailPattern = Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")
        return emailPattern.findAll(text).map { it.value }.joinToString(", ")
    }
    
    /**
     * Extract URLs
     */
    private fun extractUrls(text: String): String {
        val urlPattern = Regex("https?://[^\\s]+")
        return urlPattern.findAll(text).map { it.value }.joinToString(", ")
    }
    
    /**
     * Save learning data
     */
    private suspend fun saveLearningData(request: QuickActionRequest, response: QuickActionResponse) {
        try {
            
            // Save to memory for learning
            val memory = com.pandora.core.cac.db.MemoryEntry(
                id = "0", // Auto-generated
                content = "QuickAction: ${request.actionType}, Success: ${response.success}, Time: ${response.executionTime}ms",
                source = "quick_actions",
                timestamp = System.currentTimeMillis()
            )
            cacDao.insertMemory(memory)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving learning data", e)
        }
    }
}

/**
 * Execution Result
 */
data class ExecutionResult(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)
