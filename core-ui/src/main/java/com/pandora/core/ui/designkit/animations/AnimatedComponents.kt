package com.pandora.core.ui.designkit.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.platform.LocalContext
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Animated Button with press feedback
 */
@Composable
fun AnimatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> AnimationTokens.Scale.disabled
            isPressed -> AnimationTokens.Scale.pressed
            else -> 1f
        },
        animationSpec = AnimationTokens.Specs.buttonPress,
        label = "buttonScale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else AnimationTokens.Alpha.disabled,
        animationSpec = AnimationTokens.Specs.buttonPress,
        label = "buttonAlpha"
    )
    
    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .graphicsLayer {
                shadowElevation = if (isPressed) 2.dp.toPx() else 6.dp.toPx()
            },
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Animated Card with hover and press effects
 */
@Composable
fun AnimatedCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = when {
            !enabled -> AnimationTokens.Scale.disabled
            isPressed -> AnimationTokens.Scale.pressed
            isHovered -> AnimationTokens.Scale.hover
            else -> 1f
        },
        animationSpec = AnimationTokens.Specs.cardHover,
        label = "cardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            isHovered -> 8.dp
            else -> 4.dp
        },
        animationSpec = tween<Dp>(
            durationMillis = AnimationTokens.Duration.normal.inWholeMilliseconds.toInt(),
            easing = AnimationTokens.Easing.emphasized
        ),
        label = "cardElevation"
    )
    
    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick?.invoke()
        },
        modifier = modifier
            .scale(scale)
            .graphicsLayer {
                shadowElevation = elevation.toPx()
            },
        enabled = enabled
    ) {
        content()
    }
}

/**
 * Animated Loading Spinner
 */
@Composable
fun AnimatedLoadingSpinner(
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp,
    color: Color = LocalPandoraColors.current.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = AnimationTokens.Specs.loading,
        label = "rotation"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = 2.dp
        )
    }
}

/**
 * Animated Shimmer Effect
 */
@Composable
fun AnimatedShimmer(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = AnimationTokens.Specs.shimmer,
        label = "shimmerAlpha"
    )
    
    if (isLoading) {
        Box(
            modifier = modifier
                .background(
                    color = LocalPandoraColors.current.surfaceVariant.copy(alpha = alpha),
                    shape = RoundedCornerShape(PandoraTokens.Corner.chip)
                )
        )
    }
}

/**
 * Animated Typing Indicator
 */
@Composable
fun AnimatedTypingIndicator(
    modifier: Modifier = Modifier,
    isTyping: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )
    
    if (isTyping) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val alpha = when (index) {
                    0 -> dot1Alpha
                    1 -> dot2Alpha
                    else -> dot3Alpha
                }
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .alpha(alpha)
                        .background(
                            color = LocalPandoraColors.current.primary,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}

/**
 * Animated Counter with number changes
 */
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = androidx.compose.ui.text.TextStyle()
) {
    var previousCount by remember { mutableStateOf(count) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(
            durationMillis = 300,
            easing = AnimationTokens.Easing.emphasized
        ),
        label = "counter"
    )
    
    LaunchedEffect(count) {
        if (count != previousCount) {
            previousCount = count
        }
    }
    
    Text(
        text = animatedCount.toString(),
        modifier = modifier,
        style = style
    )
}

/**
 * Animated Visibility with custom transitions
 */
@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    enter: EnterTransition = slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween<IntOffset>(
            durationMillis = AnimationTokens.Duration.slow.inWholeMilliseconds.toInt(),
            easing = AnimationTokens.Easing.emphasizedDecelerate
        )
    ) + fadeIn(tween<Float>(
        durationMillis = AnimationTokens.Duration.slow.inWholeMilliseconds.toInt(),
        easing = AnimationTokens.Easing.emphasizedDecelerate
    )),
    exit: ExitTransition = slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween<IntOffset>(
            durationMillis = AnimationTokens.Duration.normal.inWholeMilliseconds.toInt(),
            easing = AnimationTokens.Easing.emphasizedAccelerate
        )
    ) + fadeOut(tween<Float>(
        durationMillis = AnimationTokens.Duration.normal.inWholeMilliseconds.toInt(),
        easing = AnimationTokens.Easing.emphasizedAccelerate
    )),
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit
    ) {
        content()
    }
}

/**
 * Animated Progress Bar
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalPandoraColors.current.primary,
    backgroundColor: Color = LocalPandoraColors.current.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 500,
            easing = AnimationTokens.Easing.emphasized
        ),
        label = "progress"
    )
    
    LinearProgressIndicator(
        progress = animatedProgress,
        modifier = modifier,
        color = color,
        trackColor = backgroundColor
    )
}
