package com.pandora.feature.onboarding

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main coordinator for onboarding flow management
 */
@Singleton
class OnboardingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: OnboardingStorage,
    private val analytics: OnboardingAnalytics
) {
    
    private val _currentConfig = MutableStateFlow<OnboardingConfig?>(null)
    val currentConfig: StateFlow<OnboardingConfig?> = _currentConfig.asStateFlow()
    
    private val _currentStep = MutableStateFlow<OnboardingStep?>(null)
    val currentStep: StateFlow<OnboardingStep?> = _currentStep.asStateFlow()
    
    private val _isOnboardingActive = MutableStateFlow(false)
    val isOnboardingActive: StateFlow<Boolean> = _isOnboardingActive.asStateFlow()
    
    private val _progress = MutableStateFlow(OnboardingProgress())
    val progress: StateFlow<OnboardingProgress> = _progress.asStateFlow()
    
    private val _canGoNext = MutableStateFlow(false)
    val canGoNext: StateFlow<Boolean> = _canGoNext.asStateFlow()
    
    private val _canGoPrevious = MutableStateFlow(false)
    val canGoPrevious: StateFlow<Boolean> = _canGoPrevious.asStateFlow()
    
    private val _canSkip = MutableStateFlow(false)
    val canSkip: StateFlow<Boolean> = _canSkip.asStateFlow()
    
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        // Load existing progress in coroutine scope
        // Note: This should be called from a coroutine context
    }
    
    /**
     * Start onboarding with specific flow
     */
    suspend fun startOnboarding(flowId: String) {
        try {
            val config = when (flowId) {
                "main_onboarding" -> OnboardingTutorials.getMainOnboardingFlow()
                "quick_start" -> OnboardingTutorials.getQuickStartFlow()
                "advanced_features" -> OnboardingTutorials.getAdvancedFeaturesFlow()
                else -> OnboardingTutorials.getMainOnboardingFlow()
            }
            
            _currentConfig.value = config
            _isOnboardingActive.value = true
            _isCompleted.value = false
            _error.value = null
            
            // Save flow configuration
            storage.saveFlowConfig(config.flowId, config.version)
            
            // Start with first step
            val firstStep = config.steps.minByOrNull { it.order }
            if (firstStep != null) {
                goToStep(firstStep.id)
            } else {
                _error.value = "No steps available in flow: $flowId"
            }
            
            Log.d("OnboardingManager", "Started onboarding flow: $flowId")
            
        } catch (e: Exception) {
            _error.value = "Failed to start onboarding: ${e.message}"
            Log.e("OnboardingManager", "Error starting onboarding", e)
        }
    }
    
    /**
     * Resume existing onboarding
     */
    suspend fun resumeOnboarding() {
        try {
            val progress = storage.getProgress()
            progress.collect { currentProgress ->
                if (!currentProgress.isCompleted && currentProgress.currentStepId != null) {
                    _progress.value = currentProgress
                    _isOnboardingActive.value = true
                    _isCompleted.value = false
                    
                    // Load current step
                    val step = OnboardingTutorials.getStepById(currentProgress.currentStepId)
                    if (step != null) {
                        _currentStep.value = step
                        updateNavigationState()
                        analytics.startStep(step.id, step.type)
                    }
                    
                    Log.d("OnboardingManager", "Resumed onboarding from step: ${currentProgress.currentStepId}")
                }
            }
        } catch (e: Exception) {
            _error.value = "Failed to resume onboarding: ${e.message}"
            Log.e("OnboardingManager", "Error resuming onboarding", e)
        }
    }
    
    /**
     * Go to specific step
     */
    suspend fun goToStep(stepId: String) {
        try {
            val config = _currentConfig.value ?: return
            val step = config.steps.find { it.id == stepId } ?: return
            
            // Complete current step if exists
            val currentStep = _currentStep.value
            if (currentStep != null) {
                analytics.completeStep(currentStep.id)
            }
            
            // Update progress
            val newProgress = _progress.value.copy(
                currentStepId = stepId,
                lastUpdateTime = System.currentTimeMillis()
            )
            _progress.value = newProgress
            storage.saveProgress(newProgress)
            
            // Update current step
            _currentStep.value = step
            updateNavigationState()
            
            // Start analytics for new step
            analytics.startStep(step.id, step.type)
            
            Log.d("OnboardingManager", "Navigated to step: $stepId")
            
        } catch (e: Exception) {
            _error.value = "Failed to navigate to step: ${e.message}"
            Log.e("OnboardingManager", "Error navigating to step", e)
        }
    }
    
    /**
     * Go to next step
     */
    suspend fun goToNext() {
        val config = _currentConfig.value ?: return
        val currentStep = _currentStep.value ?: return
        
        // Complete current step
        completeCurrentStep()
        
        // Find next step
        val nextStep = config.steps
            .filter { it.order > currentStep.order }
            .minByOrNull { it.order }
        
        if (nextStep != null) {
            goToStep(nextStep.id)
        } else {
            // No more steps, complete onboarding
            completeOnboarding()
        }
    }
    
    /**
     * Go to previous step
     */
    suspend fun goToPrevious() {
        val config = _currentConfig.value ?: return
        val currentStep = _currentStep.value ?: return
        
        // Find previous step
        val previousStep = config.steps
            .filter { it.order < currentStep.order }
            .maxByOrNull { it.order }
        
        if (previousStep != null) {
            goToStep(previousStep.id)
        }
    }
    
    /**
     * Skip current step
     */
    suspend fun skipCurrentStep() {
        val currentStep = _currentStep.value ?: return
        
        // Mark as skipped
        analytics.skipStep(currentStep.id)
        storage.markStepSkipped(currentStep.id)
        
        // Update progress
        val newProgress = _progress.value.copy(
            skippedSteps = _progress.value.skippedSteps + currentStep.id,
            lastUpdateTime = System.currentTimeMillis()
        )
        _progress.value = newProgress
        storage.saveProgress(newProgress)
        
        // Go to next step
        goToNext()
        
        Log.d("OnboardingManager", "Skipped step: ${currentStep.id}")
    }
    
    /**
     * Complete current step
     */
    suspend fun completeCurrentStep() {
        val currentStep = _currentStep.value ?: return
        
        // Mark as completed
        analytics.completeStep(currentStep.id)
        storage.markStepCompleted(currentStep.id)
        
        // Update progress
        val newProgress = _progress.value.copy(
            completedSteps = _progress.value.completedSteps + currentStep.id,
            lastUpdateTime = System.currentTimeMillis()
        )
        _progress.value = newProgress
        storage.saveProgress(newProgress)
        
        Log.d("OnboardingManager", "Completed step: ${currentStep.id}")
    }
    
    /**
     * Complete onboarding
     */
    suspend fun completeOnboarding() {
        try {
            // Mark onboarding as completed
            storage.markOnboardingCompleted()
            
            val newProgress = _progress.value.copy(
                isCompleted = true,
                lastUpdateTime = System.currentTimeMillis()
            )
            _progress.value = newProgress
            storage.saveProgress(newProgress)
            
            _isOnboardingActive.value = false
            _isCompleted.value = true
            _currentStep.value = null
            
            Log.d("OnboardingManager", "Onboarding completed successfully")
            
        } catch (e: Exception) {
            _error.value = "Failed to complete onboarding: ${e.message}"
            Log.e("OnboardingManager", "Error completing onboarding", e)
        }
    }
    
    /**
     * Cancel onboarding
     */
    suspend fun cancelOnboarding() {
        try {
            _isOnboardingActive.value = false
            _currentStep.value = null
            _currentConfig.value = null
            _error.value = null
            
            Log.d("OnboardingManager", "Onboarding cancelled")
            
        } catch (e: Exception) {
            _error.value = "Failed to cancel onboarding: ${e.message}"
            Log.e("OnboardingManager", "Error cancelling onboarding", e)
        }
    }
    
    /**
     * Track user interaction
     */
    fun trackInteraction(interactionType: String, elementId: String? = null, value: String? = null) {
        analytics.trackInteraction(interactionType, elementId, value)
    }
    
    /**
     * Get onboarding result
     */
    suspend fun getOnboardingResult(): OnboardingResult {
        val progress = _progress.value
        val analyticsData = analytics.exportAnalytics()
        
        return OnboardingResult(
            isCompleted = progress.isCompleted,
            completedSteps = progress.completedSteps.size,
            totalSteps = _currentConfig.value?.steps?.size ?: 0,
            timeSpent = progress.totalTimeSpent,
            skippedSteps = progress.skippedSteps.size,
            completionRate = if (progress.completedSteps.isNotEmpty()) {
                progress.completedSteps.size.toFloat() / (progress.completedSteps.size + progress.skippedSteps.size)
            } else 0f,
            analytics = analyticsData.stepAnalytics
        )
    }
    
    /**
     * Check if onboarding is needed
     */
    fun isOnboardingNeeded(): Flow<Boolean> {
        return storage.isOnboardingCompleted().map { !it }
    }
    
    /**
     * Reset onboarding progress
     */
    suspend fun resetOnboarding() {
        try {
            storage.resetProgress()
            analytics.clearAnalytics()
            
            _progress.value = OnboardingProgress()
            _currentStep.value = null
            _currentConfig.value = null
            _isOnboardingActive.value = false
            _isCompleted.value = false
            _error.value = null
            
            Log.d("OnboardingManager", "Onboarding progress reset")
            
        } catch (e: Exception) {
            _error.value = "Failed to reset onboarding: ${e.message}"
            Log.e("OnboardingManager", "Error resetting onboarding", e)
        }
    }
    
    /**
     * Load existing progress
     */
    private suspend fun loadProgress() {
        try {
            storage.getProgress().collect { currentProgress ->
                _progress.value = currentProgress
                _isCompleted.value = currentProgress.isCompleted
            }
        } catch (e: Exception) {
            Log.e("OnboardingManager", "Error loading progress", e)
        }
    }
    
    /**
     * Update navigation state
     */
    private fun updateNavigationState() {
        val config = _currentConfig.value ?: return
        val currentStep = _currentStep.value ?: return
        
        // Check if can go next
        val hasNextStep = config.steps.any { it.order > currentStep.order }
        _canGoNext.value = hasNextStep
        
        // Check if can go previous
        val hasPreviousStep = config.steps.any { it.order < currentStep.order }
        _canGoPrevious.value = hasPreviousStep
        
        // Check if can skip
        _canSkip.value = config.allowSkip && !currentStep.isRequired
    }
    
    /**
     * Validate current step
     */
    suspend fun validateCurrentStep(): Boolean {
        val currentStep = _currentStep.value ?: return false
        
        return when (currentStep.validation?.validationType) {
            ValidationType.PERMISSION_GRANTED -> {
                // Check if permission is granted
                val permission = currentStep.validation.condition
                // Implementation would check actual permission status
                true // Placeholder
            }
            ValidationType.SETTING_ENABLED -> {
                // Check if setting is enabled
                val setting = currentStep.validation.condition
                // Implementation would check actual setting status
                true // Placeholder
            }
            ValidationType.ACTION_COMPLETED -> {
                // Check if action was completed
                true // Placeholder
            }
            else -> true
        }
    }
}
