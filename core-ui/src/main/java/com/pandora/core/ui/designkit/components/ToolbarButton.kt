package com.pandora.core.ui.designkit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Enhanced Toolbar Button Component
 * Customizable toolbar button with icon and label
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalPandoraColors.current
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    
    OutlinedCard(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick?.invoke()
        },
        modifier = modifier
            .heightIn(min = 48.dp)
            .widthIn(min = 48.dp)
            .alpha(if (enabled) 1f else 0.3f),
        enabled = enabled,
        shape = RoundedCornerShape(PandoraTokens.Corner.chip),
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.Transparent,
            contentColor = colors.onSurface
        ),
        border = BorderStroke(1.dp, colors.onSurfaceVariant.copy(alpha = 0.3f)),
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(PandoraTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(PandoraTokens.Icon.size)
            )
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
