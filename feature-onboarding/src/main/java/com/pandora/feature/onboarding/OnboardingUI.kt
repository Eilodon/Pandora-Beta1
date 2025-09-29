package com.pandora.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Main onboarding overlay UI
 */
@Composable
fun OnboardingOverlay(
    onboardingManager: OnboardingManager,
    modifier: Modifier = Modifier
) {
    val isActive by onboardingManager.isOnboardingActive.collectAsState()
    val currentStep by onboardingManager.currentStep.collectAsState()
    val progress by onboardingManager.progress.collectAsState()
    val canGoNext by onboardingManager.canGoNext.collectAsState()
    val canGoPrevious by onboardingManager.canGoPrevious.collectAsState()
    val canSkip by onboardingManager.canSkip.collectAsState()
    val error by onboardingManager.error.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    if (isActive && currentStep != null) {
        Dialog(
            onDismissRequest = { /* Prevent dismissal */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            OnboardingStepContent(
                step = currentStep!!,
                progress = progress,
                canGoNext = canGoNext,
                canGoPrevious = canGoPrevious,
                canSkip = canSkip,
                onNext = { 
                    scope.launch { onboardingManager.goToNext() }
                },
                onPrevious = { 
                    scope.launch { onboardingManager.goToPrevious() }
                },
                onSkip = { 
                    scope.launch { onboardingManager.skipCurrentStep() }
                },
                onComplete = { 
                    scope.launch { onboardingManager.completeOnboarding() }
                },
                onTrackInteraction = { type, elementId, value ->
                    onboardingManager.trackInteraction(type, elementId, value)
                },
                modifier = modifier
            )
        }
    }
    
    // Show error if any
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            delay(3000) // Show for 3 seconds
        }
    }
}

/**
 * Content for individual onboarding step
 */
@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    progress: OnboardingProgress,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    canSkip: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    onTrackInteraction: (String, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    // Calculate progress percentage
    val totalSteps = progress.completedSteps.size + progress.skippedSteps.size + 1
    val currentProgress = if (totalSteps > 0) {
        (progress.completedSteps.size + progress.skippedSteps.size).toFloat() / totalSteps.toFloat()
    } else 0f
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
    ) {
        // Main content card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center)
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with progress
                OnboardingHeader(
                    step = step,
                    progress = currentProgress,
                    onSkip = if (canSkip) onSkip else null
                )
                
                // Step content
                OnboardingStepContent(
                    step = step,
                    onTrackInteraction = onTrackInteraction
                )
                
                // Navigation buttons
                OnboardingNavigation(
                    step = step,
                    canGoNext = canGoNext,
                    canGoPrevious = canGoPrevious,
                    canSkip = canSkip,
                    onNext = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTrackInteraction("button_click", "next", null)
                        onNext()
                    },
                    onPrevious = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTrackInteraction("button_click", "previous", null)
                        onPrevious()
                    },
                    onSkip = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTrackInteraction("button_click", "skip", null)
                        onSkip()
                    },
                    onComplete = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTrackInteraction("button_click", "complete", null)
                        onComplete()
                    }
                )
            }
        }
    }
}

/**
 * Onboarding header with progress
 */
@Composable
private fun OnboardingHeader(
    step: OnboardingStep,
    progress: Float,
    onSkip: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Progress indicator
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = step.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Text(
                text = "Bước ${step.order}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Skip button
        onSkip?.let {
            TextButton(onClick = it) {
                Text("Bỏ qua")
            }
        }
    }
}

/**
 * Step content based on type
 */
@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    when (step.content) {
        is StepContent.TextContent -> {
            OnboardingTextContent(
                content = step.content,
                onTrackInteraction = onTrackInteraction
            )
        }
        is StepContent.ImageContent -> {
            OnboardingImageContent(
                content = step.content,
                onTrackInteraction = onTrackInteraction
            )
        }
        is StepContent.InteractiveContent -> {
            OnboardingInteractiveContent(
                content = step.content,
                onTrackInteraction = onTrackInteraction
            )
        }
        is StepContent.DemoContent -> {
            OnboardingDemoContent(
                content = step.content,
                onTrackInteraction = onTrackInteraction
            )
        }
        is StepContent.QuizContent -> {
            OnboardingQuizContent(
                content = step.content,
                onTrackInteraction = onTrackInteraction
            )
        }
    }
}

/**
 * Text content
 */
@Composable
private fun OnboardingTextContent(
    content: StepContent.TextContent,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = content.text,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
        
        content.highlightText?.let { highlight ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = highlight,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Image content
 */
@Composable
private fun OnboardingImageContent(
    content: StepContent.ImageContent,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Placeholder for image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image: ${content.contentDescription ?: "Tutorial Image"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Interactive content
 */
@Composable
private fun OnboardingInteractiveContent(
    content: StepContent.InteractiveContent,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = content.instructions,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
        
        // Interactive element placeholder
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = "Tương tác: ${content.actionType.name}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        content.successMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Demo content
 */
@Composable
private fun OnboardingDemoContent(
    content: StepContent.DemoContent,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Demo: ${content.featureName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = content.expectedOutcome,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
        
        // Demo steps
        content.demoSteps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Quiz content
 */
@Composable
private fun OnboardingQuizContent(
    content: StepContent.QuizContent,
    onTrackInteraction: (String, String?, String?) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = content.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        content.options.forEachIndexed { index, option ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswer == index) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                onClick = {
                    selectedAnswer = index
                    onTrackInteraction("quiz_select", "option_$index", option)
                }
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        content.explanation?.let { explanation ->
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Navigation buttons
 */
@Composable
private fun OnboardingNavigation(
    step: OnboardingStep,
    canGoNext: Boolean,
    canGoPrevious: Boolean,
    canSkip: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        if (canGoPrevious) {
            OutlinedButton(onClick = onPrevious) {
                Text("Trước")
            }
        } else {
            Spacer(modifier = Modifier.width(80.dp))
        }
        
        // Skip button
        if (canSkip) {
            TextButton(onClick = onSkip) {
                Text("Bỏ qua")
            }
        }
        
        // Next/Complete button
        if (step.type == StepType.COMPLETION) {
            Button(onClick = onComplete) {
                Text("Hoàn thành")
            }
        } else if (canGoNext) {
            Button(onClick = onNext) {
                Text("Tiếp theo")
            }
        }
    }
}
