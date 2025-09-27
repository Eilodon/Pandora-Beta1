package com.pandora.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.pandora.core.ui.designkit.theme.DarkPandoraColors
import com.pandora.core.ui.designkit.theme.LightPandoraColors
import com.pandora.core.ui.designkit.theme.LocalPandoraColors

private val DarkColorScheme = darkColorScheme(
    primary = PandoraBlue,
    background = DarkGray,
    surface = LightGray,
    onPrimary = Color.Black,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun PandoraOSTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val pandoraColors = if (isDarkTheme) DarkPandoraColors else LightPandoraColors
    
    CompositionLocalProvider(LocalPandoraColors provides pandoraColors) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            // Typography và Shapes sẽ được định nghĩa sau
            content = content
        )
    }
}
