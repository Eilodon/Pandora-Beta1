package com.pandora.core.ui.designkit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Chip State for different visual states
 */
enum class ChipState { Default, Armed, Active }

/**
 * Enhanced Pandora Chip Component
 * Customizable chip with different states and haptic feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandoraChip(
    label: String,
    state: ChipState = ChipState.Default,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    val colors = LocalPandoraColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            ChipState.Default -> colors.surfaceVariant
            ChipState.Armed -> colors.surfaceVariant.copy(alpha = 0.6f)
            ChipState.Active -> colors.primary
        },
        animationSpec = tween(PandoraTokens.Animation.fastDuration.inWholeMilliseconds.toInt()),
        label = "chipBackground"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when (state) {
            ChipState.Default -> colors.onSurfaceVariant
            ChipState.Armed -> colors.onSurfaceVariant.copy(alpha = 0.9f)
            ChipState.Active -> colors.onPrimary
        },
        animationSpec = tween(PandoraTokens.Animation.fastDuration.inWholeMilliseconds.toInt()),
        label = "chipContent"
    )
    
    Surface(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick?.invoke()
        },
        modifier = modifier
            .heightIn(min = 48.dp)
            .shadow(
                elevation = PandoraTokens.Elevation.chip,
                shape = RoundedCornerShape(PandoraTokens.Corner.chip)
            )
            .semantics { role = Role.Button },
        enabled = enabled,
        shape = RoundedCornerShape(PandoraTokens.Corner.chip),
        color = backgroundColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PandoraTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let { 
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    it()
                }
            }
            Text(
                text = label,
                style = TextStyle(
                    fontSize = PandoraTokens.Typography.labelSize,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
