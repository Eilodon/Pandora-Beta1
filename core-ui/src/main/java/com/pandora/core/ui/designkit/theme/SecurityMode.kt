package com.pandora.core.ui.designkit.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Security Mode for data processing
 * Indicates where and how data is processed
 */
enum class SecurityMode(
    val displayName: String, 
    val color: @Composable () -> Color
) {
    OnDevice("On-device", { LocalPandoraColors.current.success }),
    Hybrid("Hybrid", { LocalPandoraColors.current.primary }),
    Cloud("Cloud", { LocalPandoraColors.current.onSurfaceVariant })
}
