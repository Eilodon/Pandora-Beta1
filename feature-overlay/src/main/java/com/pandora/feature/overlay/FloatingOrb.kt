package com.pandora.feature.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.theme.PandoraOSTheme
import com.pandora.core.ui.theme.PandoraBlue

@Composable
fun FloatingOrb() {
    PandoraOSTheme {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PandoraBlue.copy(alpha = 0.8f))
        ) {
            // Thêm icon hoặc animation sau
        }
    }
}
