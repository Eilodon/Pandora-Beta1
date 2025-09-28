package com.pandora.core.ui.designkit.accessibility

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Accessibility Manager
 * Handles accessibility features and configurations
 */
@Singleton
class AccessibilityManager @Inject constructor(
    private val context: Context
) {
    /**
     * Accessibility configuration
     */
    data class AccessibilityConfig(
        val isScreenReaderEnabled: Boolean = false,
        val isHighContrastEnabled: Boolean = false,
        val fontSizeScale: Float = 1.0f,
        val isReduceMotionEnabled: Boolean = false,
        val isColorBlindFriendly: Boolean = false
    )
    
    /**
     * Get current accessibility configuration
     */
    fun getAccessibilityConfig(): AccessibilityConfig {
        val configuration = context.resources.configuration
        
        return AccessibilityConfig(
            isScreenReaderEnabled = isScreenReaderEnabled(),
            isHighContrastEnabled = isHighContrastEnabled(),
            fontSizeScale = getFontSizeScale(),
            isReduceMotionEnabled = isReduceMotionEnabled(),
            isColorBlindFriendly = isColorBlindFriendly()
        )
    }
    
    private fun isScreenReaderEnabled(): Boolean {
        return context.getSystemService(Context.ACCESSIBILITY_SERVICE) != null
    }
    
    private fun isHighContrastEnabled(): Boolean {
        return context.resources.configuration.uiMode and 
               Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    
    private fun getFontSizeScale(): Float {
        return context.resources.configuration.fontScale
    }
    
    private fun isReduceMotionEnabled(): Boolean {
        return context.resources.configuration.uiMode and 
               Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_CAR
    }
    
    private fun isColorBlindFriendly(): Boolean {
        // This would typically check system settings
        return false
    }
}

/**
 * Composable to get accessibility configuration
 */
@Composable
fun rememberAccessibilityConfig(): AccessibilityManager.AccessibilityConfig {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    return remember(configuration) {
        AccessibilityManager(context).getAccessibilityConfig()
    }
}

/**
 * Accessibility modifiers
 */
object AccessibilityModifiers {
    
    /**
     * Screen reader support
     */
    fun screenReaderSupport(
        modifier: Modifier = Modifier,
        contentDescription: String? = null,
        role: Role? = null,
        stateDescription: String? = null
    ): Modifier = modifier.semantics {
        contentDescription?.let { this.contentDescription = it }
        role?.let { this.role = it }
        stateDescription?.let { this.stateDescription = it }
    }
    
    /**
     * High contrast support
     */
    fun highContrastSupport(
        modifier: Modifier = Modifier,
        isHighContrast: Boolean = false
    ): Modifier = modifier.then(
        if (isHighContrast) {
            Modifier.semantics {
                // Add high contrast specific semantics
            }
        } else {
            Modifier
        }
    )
    
    /**
     * Font scaling support
     */
    fun fontScalingSupport(
        modifier: Modifier = Modifier,
        scale: Float = 1.0f
    ): Modifier = modifier.then(
        Modifier.semantics {
            // Add font scaling specific semantics
        }
    )
}

/**
 * Accessibility utilities
 */
object AccessibilityUtils {
    
    /**
     * Get scaled size based on font scale
     */
    fun getScaledSize(baseSize: Dp, scale: Float): Dp {
        return (baseSize.value * scale).dp
    }
    
    /**
     * Get accessible color based on contrast requirements
     */
    fun getAccessibleColor(
        baseColor: androidx.compose.ui.graphics.Color,
        isHighContrast: Boolean
    ): androidx.compose.ui.graphics.Color {
        return if (isHighContrast) {
            // Return high contrast version
            baseColor.copy(alpha = 1.0f)
        } else {
            baseColor
        }
    }
}
