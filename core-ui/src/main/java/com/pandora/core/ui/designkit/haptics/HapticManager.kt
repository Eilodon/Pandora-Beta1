package com.pandora.core.ui.designkit.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic Feedback Manager
 * Provides contextual haptic feedback for different UI interactions
 */
@Singleton
class HapticManager @Inject constructor(
    private val context: Context
) {
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Haptic feedback types with custom patterns
     */
    enum class HapticType(val pattern: LongArray) {
        // Keyboard interactions
        KEY_PRESS(longArrayOf(0, 10)),
        KEY_RELEASE(longArrayOf(0, 5)),
        KEY_LONG_PRESS(longArrayOf(0, 50)),
        
        // Button interactions
        BUTTON_PRESS(longArrayOf(0, 15)),
        BUTTON_SUCCESS(longArrayOf(0, 20, 50, 20)),
        BUTTON_ERROR(longArrayOf(0, 100, 50, 100)),
        
        // Navigation
        NAVIGATION_UP(longArrayOf(0, 25)),
        NAVIGATION_DOWN(longArrayOf(0, 25)),
        NAVIGATION_LEFT(longArrayOf(0, 25)),
        NAVIGATION_RIGHT(longArrayOf(0, 25)),
        
        // AI interactions
        AI_THINKING(longArrayOf(0, 30, 100, 30, 100, 30)),
        AI_RESPONSE(longArrayOf(0, 40, 50, 40)),
        AI_ERROR(longArrayOf(0, 80, 100, 80, 100, 80)),
        
        // Gestures
        SWIPE_LEFT(longArrayOf(0, 20)),
        SWIPE_RIGHT(longArrayOf(0, 20)),
        SWIPE_UP(longArrayOf(0, 20)),
        SWIPE_DOWN(longArrayOf(0, 20)),
        
        // Notifications
        NOTIFICATION_LIGHT(longArrayOf(0, 15)),
        NOTIFICATION_MEDIUM(longArrayOf(0, 30)),
        NOTIFICATION_HEAVY(longArrayOf(0, 50)),
        
        // System feedback
        SYSTEM_SUCCESS(longArrayOf(0, 25, 50, 25)),
        SYSTEM_WARNING(longArrayOf(0, 50, 100, 50)),
        SYSTEM_ERROR(longArrayOf(0, 100, 200, 100, 200, 100))
    }
    
    /**
     * Trigger haptic feedback
     */
    fun triggerHaptic(type: HapticType) {
        if (!vibrator.hasVibrator()) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(type.pattern, -1)
                @Suppress("MissingPermission")
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION", "MissingPermission")
                vibrator.vibrate(type.pattern, -1)
            }
        } catch (e: Exception) {
            // Handle vibration errors gracefully
        }
    }
    
    /**
     * Trigger custom haptic pattern
     */
    fun triggerCustomHaptic(pattern: LongArray, repeat: Int = -1) {
        if (!vibrator.hasVibrator()) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, repeat)
                @Suppress("MissingPermission")
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION", "MissingPermission")
                vibrator.vibrate(pattern, repeat)
            }
        } catch (e: Exception) {
            // Handle vibration errors gracefully
        }
    }
    
    /**
     * Stop all vibrations
     */
    fun stopHaptic() {
        try {
            @Suppress("MissingPermission")
            vibrator.cancel()
        } catch (e: Exception) {
            // Handle cancellation errors gracefully
        }
    }
}

/**
 * Composable function to get HapticManager
 */
@Composable
fun rememberHapticManager(): HapticManager {
    val context = LocalContext.current
    return remember { HapticManager(context) }
}

/**
 * Extension functions for easy haptic feedback
 */
@Composable
fun triggerHaptic(type: HapticManager.HapticType) {
    val hapticManager = rememberHapticManager()
    hapticManager.triggerHaptic(type)
}

/**
 * Haptic feedback for keyboard interactions
 */
object KeyboardHaptics {
    @Composable
    fun onKeyPress() {
        triggerHaptic(HapticManager.HapticType.KEY_PRESS)
    }
    
    @Composable
    fun onKeyRelease() {
        triggerHaptic(HapticManager.HapticType.KEY_RELEASE)
    }
    
    @Composable
    fun onKeyLongPress() {
        triggerHaptic(HapticManager.HapticType.KEY_LONG_PRESS)
    }
}

/**
 * Haptic feedback for button interactions
 */
object ButtonHaptics {
    @Composable
    fun onPress() {
        triggerHaptic(HapticManager.HapticType.BUTTON_PRESS)
    }
    
    @Composable
    fun onSuccess() {
        triggerHaptic(HapticManager.HapticType.BUTTON_SUCCESS)
    }
    
    @Composable
    fun onError() {
        triggerHaptic(HapticManager.HapticType.BUTTON_ERROR)
    }
}

/**
 * Haptic feedback for AI interactions
 */
object AIHaptics {
    @Composable
    fun onThinking() {
        triggerHaptic(HapticManager.HapticType.AI_THINKING)
    }
    
    @Composable
    fun onResponse() {
        triggerHaptic(HapticManager.HapticType.AI_RESPONSE)
    }
    
    @Composable
    fun onError() {
        triggerHaptic(HapticManager.HapticType.AI_ERROR)
    }
}

/**
 * Haptic feedback for navigation
 */
object NavigationHaptics {
    @Composable
    fun onNavigateUp() {
        triggerHaptic(HapticManager.HapticType.NAVIGATION_UP)
    }
    
    @Composable
    fun onNavigateDown() {
        triggerHaptic(HapticManager.HapticType.NAVIGATION_DOWN)
    }
    
    @Composable
    fun onNavigateLeft() {
        triggerHaptic(HapticManager.HapticType.NAVIGATION_LEFT)
    }
    
    @Composable
    fun onNavigateRight() {
        triggerHaptic(HapticManager.HapticType.NAVIGATION_RIGHT)
    }
}

/**
 * Haptic feedback for gestures
 */
object GestureHaptics {
    @Composable
    fun onSwipeLeft() {
        triggerHaptic(HapticManager.HapticType.SWIPE_LEFT)
    }
    
    @Composable
    fun onSwipeRight() {
        triggerHaptic(HapticManager.HapticType.SWIPE_RIGHT)
    }
    
    @Composable
    fun onSwipeUp() {
        triggerHaptic(HapticManager.HapticType.SWIPE_UP)
    }
    
    @Composable
    fun onSwipeDown() {
        triggerHaptic(HapticManager.HapticType.SWIPE_DOWN)
    }
}
