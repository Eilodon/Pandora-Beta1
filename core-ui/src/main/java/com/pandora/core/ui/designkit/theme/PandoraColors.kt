package com.pandora.core.ui.designkit.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Pandora Color System
 * Comprehensive color palette with light and dark themes
 */
@Immutable
data class PandoraColors(
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val primary: Color,
    val onPrimary: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val error: Color,
    val onError: Color,
    val glassSurface: Color
)

val LightPandoraColors = PandoraColors(
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF8F9FA),
    onSurfaceVariant = Color(0xFF64748B),
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFFFFFFFF),
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFFFFFFFF),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    glassSurface = Color(0xF5FFFFFF)
)

val DarkPandoraColors = PandoraColors(
    surface = Color(0xFF0B0B0C),
    onSurface = Color(0xFFEDEDED),
    surfaceVariant = Color(0xFF161719),
    onSurfaceVariant = Color(0xFF64748B),
    primary = Color(0xFF3B82F6),
    onPrimary = Color(0xFF000000),
    success = Color(0xFF16A34A),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF000000),
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF),
    glassSurface = Color(0xEA161719)
)

val LocalPandoraColors = staticCompositionLocalOf { LightPandoraColors }
