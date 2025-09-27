package com.pandora.core.ui.designkit.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pandora Design Tokens
 * Centralized design system tokens for consistent UI across the app
 */
@Immutable
object PandoraTokens {
    
    object Spacing {
        val xs = 4.dp
        val sm = 8.dp
        val md = 12.dp
        val lg = 16.dp
        val xl = 24.dp
        val xxl = 32.dp
    }
    
    object Corner {
        val chip = 12.dp
        val card = 16.dp
        val sheet = 20.dp
    }
    
    object Elevation {
        val chip = 2.dp
        val card = 6.dp
        val sheet = 12.dp
    }
    
    object Typography {
        val labelSize = 13.sp
        val bodySize = 15.sp
        val captionSize = 12.sp
        val headlineSize = 20.sp
    }
    
    object Icon {
        val size = 20.dp
        val small = 16.dp
        val large = 24.dp
    }
    
    object Animation {
        val fastDuration = 150.milliseconds
        val normalDuration = 300.milliseconds
        val slowDuration = 500.milliseconds
        
        val fastEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val emphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    }
}
