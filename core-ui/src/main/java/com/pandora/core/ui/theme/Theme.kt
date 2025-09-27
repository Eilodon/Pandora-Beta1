package com.pandora.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PandoraBlue,
    background = DarkGray,
    surface = LightGray,
    onPrimary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun PandoraOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        // Typography và Shapes sẽ được định nghĩa sau
        content = content
    )
}
