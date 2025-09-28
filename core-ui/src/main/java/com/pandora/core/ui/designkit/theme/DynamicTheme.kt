package com.pandora.core.ui.designkit.theme

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dynamic Theme Manager
 * Handles theme switching and custom color schemes
 */
@Singleton
class DynamicThemeManager @Inject constructor() {
    private val _currentTheme = MutableStateFlow(ThemeType.DARK)
    val currentTheme: StateFlow<ThemeType> = _currentTheme.asStateFlow()
    
    private val _customColors = MutableStateFlow<Map<String, Color>>(emptyMap())
    val customColors: StateFlow<Map<String, Color>> = _customColors.asStateFlow()
    
    /**
     * Theme types
     */
    enum class ThemeType {
        LIGHT, DARK, AUTO, CUSTOM
    }
    
    /**
     * Switch theme
     */
    fun switchTheme(themeType: ThemeType) {
        _currentTheme.value = themeType
    }
    
    /**
     * Set custom colors
     */
    fun setCustomColors(colors: Map<String, Color>) {
        _customColors.value = colors
    }
    
    /**
     * Get current theme colors
     */
    fun getCurrentColors(): PandoraColors {
        return when (_currentTheme.value) {
            ThemeType.LIGHT -> LightPandoraColors
            ThemeType.DARK -> DarkPandoraColors
            ThemeType.AUTO -> {
                // This would check system theme
                DarkPandoraColors
            }
            ThemeType.CUSTOM -> {
                createCustomColors(_customColors.value)
            }
        }
    }
    
    /**
     * Create custom colors from map
     */
    private fun createCustomColors(colorMap: Map<String, Color>): PandoraColors {
        return PandoraColors(
            surface = colorMap["surface"] ?: DarkPandoraColors.surface,
            onSurface = colorMap["onSurface"] ?: DarkPandoraColors.onSurface,
            surfaceVariant = colorMap["surfaceVariant"] ?: DarkPandoraColors.surfaceVariant,
            onSurfaceVariant = colorMap["onSurfaceVariant"] ?: DarkPandoraColors.onSurfaceVariant,
            primary = colorMap["primary"] ?: DarkPandoraColors.primary,
            onPrimary = colorMap["onPrimary"] ?: DarkPandoraColors.onPrimary,
            success = colorMap["success"] ?: DarkPandoraColors.success,
            onSuccess = colorMap["onSuccess"] ?: DarkPandoraColors.onSuccess,
            warning = colorMap["warning"] ?: DarkPandoraColors.warning,
            onWarning = colorMap["onWarning"] ?: DarkPandoraColors.onWarning,
            error = colorMap["error"] ?: DarkPandoraColors.error,
            onError = colorMap["onError"] ?: DarkPandoraColors.onError,
            glassSurface = colorMap["glassSurface"] ?: DarkPandoraColors.glassSurface
        )
    }
}

/**
 * Composable to observe theme changes
 */
@Composable
fun rememberThemeState(): State<DynamicThemeManager.ThemeType> {
    val themeManager = remember { DynamicThemeManager() }
    val theme by themeManager.currentTheme.collectAsState()
    return remember { mutableStateOf(theme) }
}

/**
 * Composable to get current theme colors
 */
@Composable
fun rememberThemeColors(): PandoraColors {
    val themeManager = remember { DynamicThemeManager() }
    val theme by themeManager.currentTheme.collectAsState()
    val customColors by themeManager.customColors.collectAsState()
    
    return remember(theme, customColors) {
        themeManager.getCurrentColors()
    }
}

/**
 * Theme-aware Material3 ColorScheme
 */
@Composable
fun createMaterial3ColorScheme(
    pandoraColors: PandoraColors = rememberThemeColors()
): ColorScheme {
    return ColorScheme(
        primary = pandoraColors.primary,
        onPrimary = pandoraColors.onPrimary,
        primaryContainer = pandoraColors.primary.copy(alpha = 0.1f),
        onPrimaryContainer = pandoraColors.onPrimary,
        inversePrimary = pandoraColors.primary.copy(alpha = 0.8f),
        secondary = pandoraColors.primary.copy(alpha = 0.8f),
        onSecondary = pandoraColors.onPrimary,
        secondaryContainer = pandoraColors.primary.copy(alpha = 0.1f),
        onSecondaryContainer = pandoraColors.onPrimary,
        tertiary = pandoraColors.success,
        onTertiary = pandoraColors.onSuccess,
        tertiaryContainer = pandoraColors.success.copy(alpha = 0.1f),
        onTertiaryContainer = pandoraColors.onSuccess,
        error = pandoraColors.error,
        onError = pandoraColors.onError,
        errorContainer = pandoraColors.error.copy(alpha = 0.1f),
        onErrorContainer = pandoraColors.onError,
        background = pandoraColors.surface,
        onBackground = pandoraColors.onSurface,
        surface = pandoraColors.surface,
        onSurface = pandoraColors.onSurface,
        surfaceVariant = pandoraColors.surfaceVariant,
        onSurfaceVariant = pandoraColors.onSurfaceVariant,
        surfaceTint = pandoraColors.primary.copy(alpha = 0.1f),
        inverseSurface = pandoraColors.onSurface,
        inverseOnSurface = pandoraColors.surface,
        outline = pandoraColors.onSurfaceVariant.copy(alpha = 0.5f),
        outlineVariant = pandoraColors.onSurfaceVariant.copy(alpha = 0.3f),
        scrim = Color.Black.copy(alpha = 0.5f)
    )
}

/**
 * Predefined color schemes
 */
object ColorSchemes {
    val blue = mapOf(
        "primary" to Color(0xFF2196F3),
        "onPrimary" to Color.White,
        "surface" to Color(0xFF121212),
        "onSurface" to Color(0xFFE0E0E0)
    )
    
    val green = mapOf(
        "primary" to Color(0xFF4CAF50),
        "onPrimary" to Color.White,
        "surface" to Color(0xFF121212),
        "onSurface" to Color(0xFFE0E0E0)
    )
    
    val purple = mapOf(
        "primary" to Color(0xFF9C27B0),
        "onPrimary" to Color.White,
        "surface" to Color(0xFF121212),
        "onSurface" to Color(0xFFE0E0E0)
    )
    
    val orange = mapOf(
        "primary" to Color(0xFFFF9800),
        "onPrimary" to Color.White,
        "surface" to Color(0xFF121212),
        "onSurface" to Color(0xFFE0E0E0)
    )
    
    val red = mapOf(
        "primary" to Color(0xFFF44336),
        "onPrimary" to Color.White,
        "surface" to Color(0xFF121212),
        "onSurface" to Color(0xFFE0E0E0)
    )
}
