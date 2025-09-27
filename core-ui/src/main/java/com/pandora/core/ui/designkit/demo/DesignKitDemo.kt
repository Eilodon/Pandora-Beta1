package com.pandora.core.ui.designkit.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pandora.core.ui.designkit.components.*
import com.pandora.core.ui.designkit.theme.LocalPandoraColors
import com.pandora.core.ui.designkit.theme.SecurityMode
import com.pandora.core.ui.designkit.tokens.PandoraTokens

/**
 * Design Kit Demo
 * Showcase all available components in the Pandora Design System
 */
@Composable
fun DesignKitDemo() {
    val colors = LocalPandoraColors.current
    var snackbarMessage by remember { mutableStateOf<Pair<SnackbarKind, String>?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface),
        contentPadding = PaddingValues(PandoraTokens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.xl)
    ) {
        item {
            Text(
                text = "Pandora Design System",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.onSurface
            )
        }
        
        item {
            Text(
                text = "Design Tokens",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )
        }
        
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(PandoraTokens.Corner.card),
                color = colors.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(PandoraTokens.Spacing.lg)
                ) {
                    Text(
                        text = "Spacing: ${PandoraTokens.Spacing.xs} - ${PandoraTokens.Spacing.xxl}",
                        style = TextStyle(fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "Corners: ${PandoraTokens.Corner.chip} - ${PandoraTokens.Corner.sheet}",
                        style = TextStyle(fontFamily = FontFamily.Monospace)
                    )
                    Text(
                        text = "Typography: ${PandoraTokens.Typography.captionSize} - ${PandoraTokens.Typography.headlineSize}",
                        style = TextStyle(fontFamily = FontFamily.Monospace)
                    )
                }
            }
        }
        
        item {
            Text(
                text = "Components",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface
            )
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.sm)
            ) {
                PandoraChip(
                    label = "Gợi ý",
                    state = ChipState.Default,
                    leadingIcon = {
                        Text("💡", fontSize = 14.sp)
                    }
                )
                PandoraChip(
                    label = "Sẵn sàng",
                    state = ChipState.Armed
                )
                PandoraChip(
                    label = "Đang hoạt động",
                    state = ChipState.Active
                )
            }
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.sm)
            ) {
                ToolbarButton(
                    icon = com.pandora.core.ui.designkit.icons.PandoraIcons.Bulb,
                    label = "Rewrite"
                )
                ToolbarButton(
                    icon = com.pandora.core.ui.designkit.icons.PandoraIcons.Search,
                    label = "Search"
                )
                ToolbarButton(
                    icon = com.pandora.core.ui.designkit.icons.PandoraIcons.Copy,
                    label = "Copy",
                    enabled = false
                )
            }
        }
        
        item {
            PaletteItem(
                icon = com.pandora.core.ui.designkit.icons.PandoraIcons.Bulb,
                title = "Viết lại lịch sử",
                description = "Tóng trang nhã, ngắn gọn",
                selected = true
            )
        }
        
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(PandoraTokens.Spacing.sm)
            ) {
                SecurityIndicator(mode = SecurityMode.OnDevice)
                SecurityIndicator(mode = SecurityMode.Hybrid)
                SecurityIndicator(mode = SecurityMode.Cloud)
            }
        }
        
        item {
            SearchField(
                query = "",
                onQueryChange = { },
                placeholder = "Tìm kiếm lệnh...",
                isLoading = false
            )
        }
    }
    
    // Snackbar
    snackbarMessage?.let { (kind, message) ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(PandoraTokens.Spacing.lg),
            contentAlignment = Alignment.BottomCenter
        ) {
            PandoraSnackbar(
                kind = kind,
                message = message,
                onUndo = {
                    snackbarMessage = null
                },
                onDismiss = {
                    snackbarMessage = null
                }
            )
        }
    }
}
