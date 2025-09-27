package com.pandora.core.ui.designkit.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.icons.PandoraIcons
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Snackbar Kind for different message types
 */
enum class SnackbarKind { Success, Warning, Error }

/**
 * Enhanced Pandora Snackbar
 * Custom snackbar with different types and animations
 */
@Composable
fun PandoraSnackbar(
    kind: SnackbarKind = SnackbarKind.Success,
    message: String,
    onUndo: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalPandoraColors.current
    var visible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val backgroundColor = when (kind) {
        SnackbarKind.Success -> colors.success
        SnackbarKind.Warning -> colors.warning
        SnackbarKind.Error -> colors.error
    }
    
    val contentColor = when (kind) {
        SnackbarKind.Success -> colors.onSuccess
        SnackbarKind.Warning -> colors.onWarning
        SnackbarKind.Error -> colors.onError
    }
    
    val icon = when (kind) {
        SnackbarKind.Success -> PandoraIcons.Check
        SnackbarKind.Warning -> PandoraIcons.Warning
        SnackbarKind.Error -> PandoraIcons.Error
    }
    
    LaunchedEffect(Unit) {
        visible = true
        delay(3500) // Show for 3.5 seconds
        visible = false
        delay(200) // Wait for exit animation
        onDismiss()
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = PandoraTokens.Animation.fastDuration.inWholeMilliseconds.toInt(),
                easing = PandoraTokens.Animation.emphasizedEasing
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = PandoraTokens.Animation.normalDuration.inWholeMilliseconds.toInt()
            )
        ) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = PandoraTokens.Spacing.lg)
                .widthIn(min = 280.dp, max = 560.dp),
            shape = RoundedCornerShape(PandoraTokens.Corner.chip),
            color = backgroundColor.copy(alpha = 0.9f),
            shadowElevation = PandoraTokens.Elevation.card
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = PandoraTokens.Spacing.lg,
                    vertical = PandoraTokens.Spacing.md
                ),
                horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor
                )
                
                Text(
                    text = message,
                    style = TextStyle(
                        fontSize = PandoraTokens.Typography.bodySize,
                        color = contentColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                
                onUndo?.let {
                    TextButton(
                        onClick = {
                            visible = false
                            scope.launch {
                                delay(100)
                                it()
                            }
                        }
                    ) {
                        Text(
                            "Undo",
                            color = contentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
