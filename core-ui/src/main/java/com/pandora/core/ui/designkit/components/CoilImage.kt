package com.pandora.core.ui.designkit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Coil Image Component
 * Replaces the placeholder AsyncImage with actual Coil implementation
 */
@Composable
fun CoilImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Fallback placeholder
        Surface(
            modifier = modifier,
            color = LocalPandoraColors.current.surfaceVariant
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Image",
                    style = TextStyle(
                        color = LocalPandoraColors.current.onSurfaceVariant,
                        fontSize = PandoraTokens.Typography.labelSize
                    )
                )
            }
        }
    }
}
