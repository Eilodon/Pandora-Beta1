package com.pandora.core.ui.designkit.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Bottom Sheet for Mobile Palette
 * Custom bottom sheet with drag handle and proper styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandoraBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            shape = RoundedCornerShape(
                topStart = PandoraTokens.Corner.sheet,
                topEnd = PandoraTokens.Corner.sheet
            ),
            containerColor = LocalPandoraColors.current.surface,
            contentColor = LocalPandoraColors.current.onSurface,
            dragHandle = {
                Surface(
                    modifier = Modifier
                        .padding(vertical = PandoraTokens.Spacing.md)
                        .size(width = 48.dp, height = 4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = LocalPandoraColors.current.onSurfaceVariant.copy(alpha = 0.4f)
                ) {}
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PandoraTokens.Spacing.lg)
                    .navigationBarsPadding(),
                content = content
            )
        }
    }
}
