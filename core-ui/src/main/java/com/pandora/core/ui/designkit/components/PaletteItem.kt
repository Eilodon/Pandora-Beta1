package com.pandora.core.ui.designkit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Palette List Item Component
 * Used in command palettes and selection lists
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaletteItem(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalPandoraColors.current
    val haptic = LocalHapticFeedback.current
    
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(PandoraTokens.Animation.fastDuration.inWholeMilliseconds.toInt()),
        label = "paletteBackground"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primary.copy(alpha = 0.3f) else colors.onSurfaceVariant.copy(alpha = 0.2f),
        animationSpec = tween(PandoraTokens.Animation.fastDuration.inWholeMilliseconds.toInt()),
        label = "paletteBorder"
    )
    
    Surface(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick?.invoke()
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp),
        shape = RoundedCornerShape(PandoraTokens.Corner.chip),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(PandoraTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = colors.surfaceVariant
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    tint = colors.onSurfaceVariant
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = PandoraTokens.Typography.bodySize,
                        fontWeight = FontWeight.Medium,
                        color = colors.onSurface
                    )
                )
                Text(
                    text = description,
                    style = TextStyle(
                        fontSize = PandoraTokens.Typography.labelSize,
                        color = colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}
