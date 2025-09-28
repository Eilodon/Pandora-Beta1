package com.pandora.core.ai.automation

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Workflow Executor
 * Executes individual workflow steps
 */
@Singleton
class WorkflowExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "WorkflowExecutor"
    }
    
    /**
     * Helper method to start activity from application context
     */
    private fun startActivitySafely(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to start activity", e)
        }
    }
    
    /**
     * Helper method to get package manager
     */
    private val packageManager: PackageManager
        get() = context.packageManager
    
    /**
     * Execute workflow step
     */
    suspend fun executeStep(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): StepExecutionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            val result = when (step.type) {
                StepType.EXTRACT_ENTITIES -> executeExtractEntities(step, context)
                StepType.ANALYZE_TEXT -> executeAnalyzeText(step, context)
                StepType.CALENDAR_CREATE -> executeCalendarCreate(step, context)
                StepType.CALENDAR_UPDATE -> executeCalendarUpdate(step, context)
                StepType.NOTE_CREATE -> executeNoteCreate(step, context)
                StepType.NOTE_UPDATE -> executeNoteUpdate(step, context)
                StepType.MESSAGE_SEND -> executeMessageSend(step, context)
                StepType.NOTIFICATION_SEND -> executeNotificationSend(step, context)
                StepType.REMINDER_SET -> executeReminderSet(step, context)
                StepType.APP_LAUNCH -> executeAppLaunch(step, context)
                StepType.API_CALL -> executeApiCall(step, context)
                StepType.CUSTOM_ACTION -> executeCustomAction(step, context)
            }
            
            StepExecutionResult(
                stepId = step.id,
                isSuccess = true,
                output = result,
                duration = System.currentTimeMillis() - startTime
            )
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error executing step: ${step.id}", e)
            StepExecutionResult(
                stepId = step.id,
                isSuccess = false,
                error = e.message,
                duration = System.currentTimeMillis() - startTime
            )
        }
    }
    
    /**
     * Execute extract entities step
     */
    private suspend fun executeExtractEntities(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val text = context["input.text"] as? String ?: ""
        val entities = step.parameters["entities"] as? List<String> ?: emptyList()
        
        // Simulate entity extraction
        val extractedEntities = mutableMapOf<String, Any>()
        
        // Extract persons
        if (entities.contains("PERSON")) {
            val persons = extractPersons(text)
            extractedEntities["persons"] = persons
        }
        
        // Extract times
        if (entities.contains("TIME")) {
            val times = extractTimes(text)
            extractedEntities["times"] = times
        }
        
        // Extract locations
        if (entities.contains("LOCATION")) {
            val locations = extractLocations(text)
            extractedEntities["locations"] = locations
        }
        
        // Extract topics
        if (entities.contains("TOPIC")) {
            val topics = extractTopics(text)
            extractedEntities["topics"] = topics
        }
        
        // Extract phone numbers
        if (entities.contains("PHONE")) {
            val phones = extractPhones(text)
            extractedEntities["phones"] = phones
        }
        
        // Extract emails
        if (entities.contains("EMAIL")) {
            val emails = extractEmails(text)
            extractedEntities["emails"] = emails
        }
        
        return extractedEntities
    }
    
    /**
     * Execute analyze text step
     */
    private suspend fun executeAnalyzeText(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val text = context["input.text"] as? String ?: ""
        val analysisType = step.parameters["analysis_type"] as? String ?: "general"
        
        return when (analysisType) {
            "categorization" -> analyzeTextCategorization(text)
            "communication_method" -> analyzeCommunicationMethod(text)
            "sentiment" -> analyzeSentiment(text)
            "intent" -> analyzeIntent(text)
            else -> mapOf("analysis" to "general")
        }
    }
    
    /**
     * Execute calendar create step
     */
    private suspend fun executeCalendarCreate(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val title = resolveParameter(step.parameters["title"], context) as? String ?: "New Event"
        val time = resolveParameter(step.parameters["time"], context) as? String ?: ""
        val location = resolveParameter(step.parameters["location"], context) as? String ?: ""
        val description = resolveParameter(step.parameters["description"], context) as? String ?: ""
        
        // Create calendar intent
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            
            // Set start time (simplified)
            val startTime = System.currentTimeMillis() + 3600000 // 1 hour from now
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + 3600000) // 1 hour duration
        }
        
        startActivitySafely(intent)
        
        return mapOf(
            "title" to title,
            "time" to time,
            "location" to location,
            "created" to true
        )
    }
    
    /**
     * Execute calendar update step
     */
    private suspend fun executeCalendarUpdate(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        // Implementation for calendar update
        return mapOf("updated" to true)
    }
    
    /**
     * Execute note create step
     */
    private suspend fun executeNoteCreate(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val content = resolveParameter(step.parameters["content"], context) as? String ?: ""
        val category = resolveParameter(step.parameters["category"], context) as? String ?: "general"
        val tags = resolveParameter(step.parameters["tags"], context) as? List<String> ?: emptyList()
        
        // Create note intent (Google Keep)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_SUBJECT, "Note: $category")
            setPackage("com.google.android.keep")
        }
        
        if (intent.resolveActivity(packageManager) != null) {
            startActivitySafely(intent)
        }
        
        return mapOf(
            "content" to content,
            "category" to category,
            "tags" to tags,
            "created" to true
        )
    }
    
    /**
     * Execute note update step
     */
    private suspend fun executeNoteUpdate(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        // Implementation for note update
        return mapOf("updated" to true)
    }
    
    /**
     * Execute message send step
     */
    private suspend fun executeMessageSend(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val recipients = resolveParameter(step.parameters["recipients"], context) as? List<String> ?: emptyList()
        val message = resolveParameter(step.parameters["message"], context) as? String ?: ""
        val method = resolveParameter(step.parameters["method"], context) as? String ?: "sms"
        
        when (method) {
            "sms" -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = android.net.Uri.parse("smsto:${recipients.joinToString(",")}")
                    putExtra("sms_body", message)
                }
                startActivitySafely(intent)
            }
            "whatsapp" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
                startActivitySafely(intent)
            }
            "email" -> {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, recipients.toTypedArray())
                    putExtra(Intent.EXTRA_SUBJECT, "Message from PandoraOS")
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                startActivitySafely(Intent.createChooser(intent, "Send email"))
            }
        }
        
        return mapOf(
            "recipients" to recipients,
            "message" to message,
            "method" to method,
            "sent" to true
        )
    }
    
    /**
     * Execute notification send step
     */
    private suspend fun executeNotificationSend(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val title = resolveParameter(step.parameters["title"], context) as? String ?: "PandoraOS"
        val message = resolveParameter(step.parameters["message"], context) as? String ?: ""
        
        // Create notification (simplified)
        android.util.Log.d(TAG, "Notification: $title - $message")
        
        return mapOf(
            "title" to title,
            "message" to message,
            "sent" to true
        )
    }
    
    /**
     * Execute reminder set step
     */
    private suspend fun executeReminderSet(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val reminderText = resolveParameter(step.parameters["reminder_text"], context) as? String ?: ""
        val reminderTime = resolveParameter(step.parameters["reminder_time"], context) as? String ?: ""
        
        // Set reminder (simplified)
        android.util.Log.d(TAG, "Reminder set: $reminderText at $reminderTime")
        
        return mapOf(
            "reminder_text" to reminderText,
            "reminder_time" to reminderTime,
            "set" to true
        )
    }
    
    /**
     * Execute app launch step
     */
    private suspend fun executeAppLaunch(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val packageName = resolveParameter(step.parameters["package_name"], context) as? String ?: ""
        val action = resolveParameter(step.parameters["action"], context) as? String ?: ""
        
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            startActivitySafely(intent)
        }
        
        return mapOf(
            "package_name" to packageName,
            "action" to action,
            "launched" to true
        )
    }
    
    /**
     * Execute API call step
     */
    private suspend fun executeApiCall(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val url = resolveParameter(step.parameters["url"], context) as? String ?: ""
        val method = resolveParameter(step.parameters["method"], context) as? String ?: "GET"
        val headers = resolveParameter(step.parameters["headers"], context) as? Map<String, String> ?: emptyMap()
        val body = resolveParameter(step.parameters["body"], context) as? String ?: ""
        
        // Simulate API call
        delay(1000) // Simulate network delay
        
        return mapOf(
            "url" to url,
            "method" to method,
            "status_code" to 200,
            "response" to "API call successful"
        )
    }
    
    /**
     * Execute custom action step
     */
    private suspend fun executeCustomAction(
        step: WorkflowStep,
        context: MutableMap<String, Any>
    ): Map<String, Any> {
        val actionName = resolveParameter(step.parameters["action_name"], context) as? String ?: ""
        val parameters = resolveParameter(step.parameters["parameters"], context) as? Map<String, Any> ?: emptyMap()
        
        // Execute custom action
        android.util.Log.d(TAG, "Executing custom action: $actionName with parameters: $parameters")
        
        return mapOf(
            "action_name" to actionName,
            "parameters" to parameters,
            "executed" to true
        )
    }
    
    /**
     * Resolve parameter value
     */
    private fun resolveParameter(value: Any?, context: MutableMap<String, Any>): Any? {
        if (value is String && value.startsWith("{{") && value.endsWith("}}")) {
            val key = value.substring(2, value.length - 2)
            return context[key]
        }
        return value
    }
    
    // Entity extraction methods
    private fun extractPersons(text: String): List<String> {
        // Simple person extraction (in real implementation, use NLP)
        val personPattern = Regex("\\b[A-Z][a-z]+\\s+[A-Z][a-z]+\\b")
        return personPattern.findAll(text).map { it.value }.toList()
    }
    
    private fun extractTimes(text: String): List<String> {
        val timePattern = Regex("\\b(\\d{1,2}):?(\\d{2})?\\s*(am|pm|AM|PM)?\\b")
        return timePattern.findAll(text).map { it.value }.toList()
    }
    
    private fun extractLocations(text: String): List<String> {
        val locationKeywords = listOf("tại", "ở", "đến", "từ", "về")
        return locationKeywords.mapNotNull { keyword ->
            val index = text.indexOf(keyword)
            if (index != -1) {
                text.substring(index + keyword.length).trim().split(" ").take(3).joinToString(" ")
            } else null
        }
    }
    
    private fun extractTopics(text: String): List<String> {
        val topicKeywords = listOf("về", "chủ đề", "topic", "subject")
        return topicKeywords.mapNotNull { keyword ->
            val index = text.indexOf(keyword)
            if (index != -1) {
                text.substring(index + keyword.length).trim().split(" ").take(2).joinToString(" ")
            } else null
        }
    }
    
    private fun extractPhones(text: String): List<String> {
        val phonePattern = Regex("\\b\\d{10,11}\\b")
        return phonePattern.findAll(text).map { it.value }.toList()
    }
    
    private fun extractEmails(text: String): List<String> {
        val emailPattern = Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")
        return emailPattern.findAll(text).map { it.value }.toList()
    }
    
    // Text analysis methods
    private fun analyzeTextCategorization(text: String): Map<String, Any> {
        val category = when {
            text.contains("họp") || text.contains("meeting") -> "meeting"
            text.contains("ghi chú") || text.contains("note") -> "note"
            text.contains("gửi") || text.contains("send") -> "communication"
            text.contains("nhắc") || text.contains("remind") -> "reminder"
            else -> "general"
        }
        
        val tags = mutableListOf<String>()
        if (text.contains("khẩn cấp") || text.contains("urgent")) tags.add("urgent")
        if (text.contains("quan trọng") || text.contains("important")) tags.add("important")
        if (text.contains("công việc") || text.contains("work")) tags.add("work")
        
        return mapOf(
            "category" to category,
            "tags" to tags,
            "confidence" to 0.8f
        )
    }
    
    private fun analyzeCommunicationMethod(text: String): Map<String, Any> {
        val method = when {
            text.contains("sms") || text.contains("tin nhắn") -> "sms"
            text.contains("whatsapp") || text.contains("zalo") -> "whatsapp"
            text.contains("email") || text.contains("thư") -> "email"
            text.contains("gọi") || text.contains("call") -> "call"
            else -> "sms"
        }
        
        return mapOf(
            "method" to method,
            "confidence" to 0.7f
        )
    }
    
    private fun analyzeSentiment(text: String): Map<String, Any> {
        val positiveWords = listOf("tốt", "hay", "tuyệt", "great", "good", "excellent")
        val negativeWords = listOf("tệ", "xấu", "bad", "terrible", "awful")
        
        val positiveCount = positiveWords.count { text.contains(it, ignoreCase = true) }
        val negativeCount = negativeWords.count { text.contains(it, ignoreCase = true) }
        
        val sentiment = when {
            positiveCount > negativeCount -> "positive"
            negativeCount > positiveCount -> "negative"
            else -> "neutral"
        }
        
        return mapOf(
            "sentiment" to sentiment,
            "confidence" to 0.6f
        )
    }
    
    private fun analyzeIntent(text: String): Map<String, Any> {
        val intent = when {
            text.contains("họp") || text.contains("meeting") -> "schedule_meeting"
            text.contains("ghi chú") || text.contains("note") -> "create_note"
            text.contains("gửi") || text.contains("send") -> "send_message"
            text.contains("nhắc") || text.contains("remind") -> "set_reminder"
            text.contains("tìm") || text.contains("search") -> "search"
            else -> "unknown"
        }
        
        return mapOf(
            "intent" to intent,
            "confidence" to 0.7f
        )
    }
}
