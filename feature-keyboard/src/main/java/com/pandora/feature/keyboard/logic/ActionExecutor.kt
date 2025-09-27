// feature-keyboard/src/main/java/com/pandora/feature/keyboard/logic/ActionExecutor.kt
package com.pandora.feature.keyboard.logic

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun execute(action: PandoraAction) {
        val intent = when (action) {
            is PandoraAction.AddToCalendar -> createCalendarIntent(action)
            // Các hành động khác sẽ được xử lý ở đây
            else -> null
        }

        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Cần thiết khi gọi từ Service
            context.startActivity(it)
        }
    }

    private fun createCalendarIntent(action: PandoraAction.AddToCalendar): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, action.title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, action.startTime)
        }
    }
}
