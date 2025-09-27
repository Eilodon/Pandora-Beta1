package com.pandora.core.ui.designkit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.icons.PandoraIcons
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.theme.SecurityMode
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Security Indicator Component
 * Shows the current security mode for data processing
 */
@Composable
fun SecurityIndicator(
    mode: SecurityMode,
    modifier: Modifier = Modifier
) {
    val colors = LocalPandoraColors.current
    val modeColor = mode.color()
    
    Surface(
        modifier = modifier
            .semantics { contentDescription = "Privacy: ${mode.displayName}" },
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, modeColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(modeColor, CircleShape)
            )
            Text(
                text = mode.displayName,
                style = TextStyle(
                    fontSize = PandoraTokens.Typography.captionSize,
                    fontWeight = FontWeight.Medium,
                    color = modeColor
                )
            )
            Icon(
                imageVector = PandoraIcons.Lock,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = modeColor
            )
        }
    }
}
