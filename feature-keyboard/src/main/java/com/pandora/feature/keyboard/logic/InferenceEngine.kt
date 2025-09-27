// feature-keyboard/src/main/java/com/pandora/feature/keyboard/logic/InferenceEngine.kt
package com.pandora.feature.keyboard.logic

import javax.inject.Inject
import javax.inject.Singleton

// Regex để nhận dạng các mẫu thời gian và hành động đơn giản
private val MEETING_REGEX = """(họp|gặp|meeting).*(\d{1,2})(h|am|pm)""".toRegex(RegexOption.IGNORE_CASE)

@Singleton
class InferenceEngine @Inject constructor() {

    fun inferActionFromText(text: String): InferredAction? {
        // Giai đoạn này chỉ hiện thực logic đơn giản nhất: nhận dạng lịch hẹn
        val match = MEETING_REGEX.find(text)
        if (match != null) {
            val time = match.groupValues[2].toLongOrNull() ?: return null
            // Logic chuyển đổi thời gian (ví dụ 3pm) thành timestamp sẽ được hoàn thiện sau
            val calendarTime = System.currentTimeMillis() + (time * 60 * 60 * 1000)

            return InferredAction(
                sourceText = text,
                action = PandoraAction.AddToCalendar(
                    title = "Cuộc họp", // Tiêu đề tạm thời
                    startTime = calendarTime
                ),
                confidence = 0.8f
            )
        }
        return null
    }
}
