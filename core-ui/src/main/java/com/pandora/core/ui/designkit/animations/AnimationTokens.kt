package com.pandora.core.ui.designkit.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.Immutable
import kotlin.time.Duration.Companion.milliseconds

/**
 * Animation Tokens for Pandora Design System
 * Centralized animation definitions for consistent motion design
 */
@Immutable
object AnimationTokens {
    
    /**
     * Duration tokens
     */
    object Duration {
        val instant = 0.milliseconds
        val fast = 150.milliseconds
        val normal = 300.milliseconds
        val slow = 500.milliseconds
        val slower = 750.milliseconds
        val slowest = 1000.milliseconds
    }
    
    /**
     * Easing curves
     */
    object Easing {
        // Standard easing curves
        val standard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
        val decelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
        val accelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
        val sharp = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
        
        // Emphasized easing for important animations
        val emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
        val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
        val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
        
        // Bounce and spring effects
        val bounce = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
        val spring = SpringSpec<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    }
    
    /**
     * Animation specs for common use cases
     */
    object Specs {
        // Button press animation
        val buttonPress = tween<Float>(
            durationMillis = Duration.fast.inWholeMilliseconds.toInt(),
            easing = Easing.standard
        )
        
        // Card hover animation
        val cardHover = tween<Float>(
            durationMillis = Duration.normal.inWholeMilliseconds.toInt(),
            easing = Easing.emphasized
        )
        
        // Modal enter/exit
        val modalEnter = tween<Float>(
            durationMillis = Duration.slow.inWholeMilliseconds.toInt(),
            easing = Easing.emphasizedDecelerate
        )
        
        val modalExit = tween<Float>(
            durationMillis = Duration.normal.inWholeMilliseconds.toInt(),
            easing = Easing.emphasizedAccelerate
        )
        
        // Keyboard key press
        val keyPress = spring<Float>(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        )
        
        // Loading spinner
        val loading = infiniteRepeatable<Float>(
            animation = tween(
                durationMillis = Duration.slowest.inWholeMilliseconds.toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
        
        // Shimmer effect
        val shimmer = infiniteRepeatable<Float>(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
        
        // Typing animation
        val typing = tween<Float>(
            durationMillis = 50,
            easing = Easing.standard
        )
    }
    
    /**
     * Stagger delays for list animations
     */
    object Stagger {
        val short = 50L
        val medium = 100L
        val long = 150L
    }
    
    /**
     * Scale values for different interaction states
     */
    object Scale {
        val pressed = 0.95f
        val hover = 1.02f
        val focus = 1.05f
        val disabled = 0.8f
    }
    
    /**
     * Alpha values for different states
     */
    object Alpha {
        val disabled = 0.38f
        val hover = 0.87f
        val pressed = 0.6f
        val loading = 0.5f
    }
    
    /**
     * Translation values for slide animations
     */
    object Translation {
        val slideUp = -50f
        val slideDown = 50f
        val slideLeft = -50f
        val slideRight = 50f
        val fadeIn = 0f
    }
}
