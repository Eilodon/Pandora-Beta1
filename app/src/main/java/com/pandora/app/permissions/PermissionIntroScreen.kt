package com.pandora.app.permissions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// FIXED: Compose screen
@Composable
fun PermissionIntroScreen(
    state: PermissionState,
    onGrant: () -> Unit,
    onLearnMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(text = "Quyền Bluetooth/NFC", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Chúng tôi sử dụng BLE/NFC để kết nối thiết bị lân cận. Quyền này giúp app scan và giao tiếp an toàn.",
            style = MaterialTheme.typography.bodyMedium
        )
        if (state.showRationale) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Hãy cấp quyền để tính năng hoạt động ổn định. Bạn có thể tắt/bật trong cài đặt.",
                color = Color(0xFFAA0000)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row {
            Button(onClick = onGrant) { Text("Cho phép") }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = onLearnMore) { Text("Tìm hiểu thêm") }
        }
    }
}
