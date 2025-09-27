package com.pandora.core.ui.designkit.modifiers

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.theme.LocalPandoraColors

/**
 * Modifier Extensions for Pandora Design System
 * Custom modifiers for shimmer effects, glass effects, etc.
 */

/**
 * Shimmer effect modifier for loading states
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    ).value
    
    val shimmerColorStops = arrayOf(
        0.0f to LocalPandoraColors.current.surfaceVariant.copy(alpha = alpha),
        0.5f to LocalPandoraColors.current.onSurfaceVariant.copy(alpha = alpha * 0.3f),
        1.0f to LocalPandoraColors.current.surfaceVariant.copy(alpha = alpha)
    )
    
    background(
        brush = Brush.horizontalGradient(
            colorStops = shimmerColorStops
        )
    )
}

/**
 * Glass effect modifier for translucent surfaces
 */
fun Modifier.glassEffect(isDark: Boolean = false): Modifier = composed {
    val colors = LocalPandoraColors.current
    val glassColor = if (isDark) colors.glassSurface else colors.glassSurface
    
    this
        .background(glassColor)
        .blur(radius = 8.dp)
}
