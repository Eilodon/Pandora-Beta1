package com.pandora.feature.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandora.core.ui.theme.PandoraOSTheme
import com.pandora.feature.keyboard.logic.PandoraAction
import com.pandora.feature.keyboard.logic.InferredAction

@Composable
fun PandoraKeyboardView(inputManager: InputManager, viewModel: KeyboardViewModel) {
    PandoraOSTheme { // Bọc UI trong Theme của chúng ta
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp) 
                .background(MaterialTheme.colorScheme.background) // Sử dụng màu từ Theme
        ) {
            SmartContextBar(viewModel) // Tách thành một Composable riêng
            KeyboardLayout(inputManager, viewModel)  // Tách thành một Composable riêng
        }
    }
}

@Composable
fun SmartContextBar(viewModel: KeyboardViewModel) {
    val inferredAction by viewModel.inferredAction.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.CenterStart // Căn sang trái
    ) {
        // Chỉ hiển thị khi có hành động được gợi ý
        inferredAction?.let { action ->
            SuggestionChip(action = action, onClick = {
                viewModel.executeAction(action)
            })
        } ?: run {
            // Hiển thị trạng thái mặc định khi không có gợi ý
            Text(
                text = "Đang lắng nghe...",
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// Tạo một Composable mới cho chip gợi ý
@Composable
fun SuggestionChip(action: InferredAction, onClick: () -> Unit) {
    val text = when (action.action) {
        is PandoraAction.AddToCalendar -> "📅 Thêm vào Lịch?"
        is PandoraAction.SendLocation -> "📍 Gửi Vị trí?"
        is PandoraAction.SetReminder -> "⏰ Đặt Lời nhắc?"
    }

    Button(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Text(text, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
    }
}

@Composable
fun KeyboardLayout(inputManager: InputManager, viewModel: KeyboardViewModel) {
     Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Nhấn để gõ 'A' và ghi nhớ", 
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.clickable { 
                val text = "A"
                inputManager.sendText(text)
                viewModel.recordTypingMemory(text) // Ghi nhớ hành động gõ phím
            }
        )
    }
}
