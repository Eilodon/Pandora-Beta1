// feature-keyboard/src/main/java/com/pandora/feature/keyboard/logic/MiniFlows.kt
package com.pandora.feature.keyboard.logic

import android.content.Context
import android.content.Intent
import android.util.Log

// Flow 1: Mở Lịch khi gõ "họp" hoặc "meeting"
fun checkAndTriggerCalendarFlow(context: Context, text: String) {
    if (text.contains("họp", ignoreCase = true) || 
        text.contains("meeting", ignoreCase = true) ||
        text.contains("lịch", ignoreCase = true)) {
        Log.d("MiniFlows", "Triggering Calendar flow for: $text")
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

// Flow 3: Mở Maps khi gõ "đi" hoặc "đường"
fun checkAndTriggerMapsFlow(context: Context, text: String) {
    if (text.contains("đi", ignoreCase = true) || 
        text.contains("đường", ignoreCase = true) ||
        text.contains("maps", ignoreCase = true) ||
        text.contains("địa chỉ", ignoreCase = true)) {
        Log.d("MiniFlows", "Triggering Maps flow for: $text")
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

// Flow 4: Mở Camera khi gõ "chụp" hoặc "photo"
fun checkAndTriggerCameraFlow(context: Context, text: String) {
    if (text.contains("chụp", ignoreCase = true) || 
        text.contains("photo", ignoreCase = true) ||
        text.contains("ảnh", ignoreCase = true) ||
        text.contains("camera", ignoreCase = true)) {
        Log.d("MiniFlows", "Triggering Camera flow for: $text")
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

// Flow 5: Mở Keep khi gõ "note" hoặc "ghi chú"
fun checkAndTriggerNoteFlow(context: Context, text: String) {
    if (text.contains("note", ignoreCase = true) || 
        text.contains("ghi chú", ignoreCase = true) ||
        text.contains("nhớ", ignoreCase = true) ||
        text.contains("keep", ignoreCase = true)) {
        Log.d("MiniFlows", "Triggering Keep flow for: $text")
        val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.keep")
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }
}

// Hàm tổng hợp để kiểm tra tất cả flows
fun checkAllMiniFlows(context: Context, text: String) {
    checkAndTriggerCalendarFlow(context, text)
    checkAndTriggerMapsFlow(context, text)
    checkAndTriggerCameraFlow(context, text)
    checkAndTriggerNoteFlow(context, text)
}
