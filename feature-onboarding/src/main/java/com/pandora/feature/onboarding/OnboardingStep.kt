package com.pandora.feature.onboarding

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a single step in the onboarding flow
 */
@Immutable
data class OnboardingStep(
    val id: String,
    val title: String,
    val description: String,
    val type: StepType,
    val content: StepContent,
    val validation: StepValidation? = null,
    val isRequired: Boolean = true,
    val estimatedTimeSeconds: Int = 30,
    val order: Int = 0
)

/**
 * Types of onboarding steps
 */
enum class StepType {
    WELCOME,           // Welcome screen
    INFO,              // Information display
    INTERACTIVE,       // User interaction required
    DEMO,              // Feature demonstration
    QUIZ,              // Knowledge check
    PERMISSION,        // Permission request
    SETUP,             // Configuration setup
    COMPLETION         // Completion screen
}

/**
 * Content for each step
 */
@Immutable
sealed class StepContent {
    data class TextContent(
        val text: String,
        val highlightText: String? = null
    ) : StepContent()

    data class ImageContent(
        val imageUrl: String? = null,
        val imageVector: ImageVector? = null,
        val contentDescription: String? = null
    ) : StepContent()

    data class InteractiveContent(
        val instructions: String,
        val targetElement: String? = null,
        val actionType: ActionType,
        val successMessage: String? = null
    ) : StepContent()

    data class DemoContent(
        val featureName: String,
        val demoSteps: List<String>,
        val expectedOutcome: String
    ) : StepContent()

    data class QuizContent(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int,
        val explanation: String? = null
    ) : StepContent()
}

/**
 * Types of interactive actions
 */
enum class ActionType {
    TAP,               // Tap on element
    SWIPE,             // Swipe gesture
    LONG_PRESS,        // Long press
    TYPE,              // Type text
    SELECT,            // Select option
    NAVIGATE,          // Navigate to screen
    ENABLE,            // Enable setting
    GRANT_PERMISSION   // Grant permission
}

/**
 * Validation for step completion
 */
@Immutable
data class StepValidation(
    val validationType: ValidationType,
    val condition: String,
    val errorMessage: String? = null
)

/**
 * Types of validation
 */
enum class ValidationType {
    PERMISSION_GRANTED,    // Permission is granted
    SETTING_ENABLED,       // Setting is enabled
    ACTION_COMPLETED,      // Action was completed
    TEXT_ENTERED,          // Text was entered
    ELEMENT_TAPPED,        // Element was tapped
    QUIZ_CORRECT,          // Quiz answered correctly
    CUSTOM                 // Custom validation
}

/**
 * Onboarding progress tracking
 */
@Immutable
data class OnboardingProgress(
    val currentStepId: String? = null,
    val completedSteps: Set<String> = emptySet(),
    val skippedSteps: Set<String> = emptySet(),
    val startTime: Long = System.currentTimeMillis(),
    val lastUpdateTime: Long = System.currentTimeMillis(),
    val totalTimeSpent: Long = 0L,
    val isCompleted: Boolean = false
)

/**
 * Onboarding analytics data
 */
@Immutable
data class OnboardingAnalyticsData(
    val stepId: String,
    val stepType: StepType,
    val startTime: Long,
    val endTime: Long? = null,
    val timeSpent: Long? = null,
    val wasSkipped: Boolean = false,
    val wasCompleted: Boolean = false,
    val validationPassed: Boolean? = null,
    val userInteractions: List<UserInteraction> = emptyList()
)

/**
 * User interaction tracking
 */
@Immutable
data class UserInteraction(
    val interactionType: String,
    val timestamp: Long,
    val elementId: String? = null,
    val value: String? = null
)

/**
 * Onboarding configuration
 */
@Immutable
data class OnboardingConfig(
    val flowId: String,
    val version: String,
    val steps: List<OnboardingStep>,
    val allowSkip: Boolean = true,
    val showProgress: Boolean = true,
    val enableAnalytics: Boolean = true,
    val autoAdvance: Boolean = false,
    val autoAdvanceDelayMs: Long = 3000L
)

/**
 * Onboarding result
 */
@Immutable
data class OnboardingResult(
    val isCompleted: Boolean,
    val completedSteps: Int,
    val totalSteps: Int,
    val timeSpent: Long,
    val skippedSteps: Int,
    val completionRate: Float,
    val analytics: List<OnboardingAnalyticsData>
)
