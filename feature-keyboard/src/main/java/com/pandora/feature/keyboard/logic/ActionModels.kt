// feature-keyboard/src/main/java/com/pandora/feature/keyboard/logic/ActionModels.kt
package com.pandora.feature.keyboard.logic

import android.content.Intent

// Định nghĩa các loại hành động mà Pandora có thể đề xuất
sealed class PandoraAction {
    data class AddToCalendar(val title: String, val startTime: Long) : PandoraAction()
    data class SendLocation(val query: String) : PandoraAction()
    data class SetReminder(val task: String, val time: Long) : PandoraAction()
    
    // New action types for enhanced functionality
    data class CreateReminder(val title: String, val message: String) : PandoraAction()
    data class SendMessage(val recipient: String, val message: String) : PandoraAction()
    data class CreateNote(val title: String, val content: String) : PandoraAction()
    data class Calculate(val expression: String) : PandoraAction()
    data class Search(val query: String) : PandoraAction()
    object Unknown : PandoraAction()
}

// Lớp chứa kết quả phân tích: văn bản gốc và hành động được đề xuất
data class InferredAction(
    val sourceText: String,
    val action: PandoraAction,
    val confidence: Float // Mức độ tự tin của AI
)
