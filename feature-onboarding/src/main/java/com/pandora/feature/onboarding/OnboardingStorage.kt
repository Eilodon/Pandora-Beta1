package com.pandora.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages onboarding progress persistence using DataStore
 */
@Singleton
class OnboardingStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding")

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val CURRENT_STEP_ID = stringPreferencesKey("current_step_id")
        private val COMPLETED_STEPS = stringSetPreferencesKey("completed_steps")
        private val SKIPPED_STEPS = stringSetPreferencesKey("skipped_steps")
        private val START_TIME = longPreferencesKey("start_time")
        private val LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        private val TOTAL_TIME_SPENT = longPreferencesKey("total_time_spent")
        private val FLOW_ID = stringPreferencesKey("flow_id")
        private val FLOW_VERSION = stringPreferencesKey("flow_version")
    }

    /**
     * Get current onboarding progress
     */
    fun getProgress(): Flow<OnboardingProgress> = context.dataStore.data.map { preferences ->
        OnboardingProgress(
            currentStepId = preferences[CURRENT_STEP_ID],
            completedSteps = preferences[COMPLETED_STEPS] ?: emptySet(),
            skippedSteps = preferences[SKIPPED_STEPS] ?: emptySet(),
            startTime = preferences[START_TIME] ?: System.currentTimeMillis(),
            lastUpdateTime = preferences[LAST_UPDATE_TIME] ?: System.currentTimeMillis(),
            totalTimeSpent = preferences[TOTAL_TIME_SPENT] ?: 0L,
            isCompleted = preferences[ONBOARDING_COMPLETED] ?: false
        )
    }

    /**
     * Save onboarding progress
     */
    suspend fun saveProgress(progress: OnboardingProgress) {
        context.dataStore.edit { preferences ->
            progress.currentStepId?.let { 
                preferences[CURRENT_STEP_ID] = it 
            }
            preferences[COMPLETED_STEPS] = progress.completedSteps
            preferences[SKIPPED_STEPS] = progress.skippedSteps
            preferences[START_TIME] = progress.startTime
            preferences[LAST_UPDATE_TIME] = progress.lastUpdateTime
            preferences[TOTAL_TIME_SPENT] = progress.totalTimeSpent
            preferences[ONBOARDING_COMPLETED] = progress.isCompleted
        }
    }

    /**
     * Mark step as completed
     */
    suspend fun markStepCompleted(stepId: String) {
        context.dataStore.edit { preferences ->
            val currentCompleted = preferences[COMPLETED_STEPS] ?: emptySet()
            preferences[COMPLETED_STEPS] = currentCompleted + stepId
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Mark step as skipped
     */
    suspend fun markStepSkipped(stepId: String) {
        context.dataStore.edit { preferences ->
            val currentSkipped = preferences[SKIPPED_STEPS] ?: emptySet()
            preferences[SKIPPED_STEPS] = currentSkipped + stepId
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Update current step
     */
    suspend fun updateCurrentStep(stepId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_STEP_ID] = stepId
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Update total time spent
     */
    suspend fun updateTimeSpent(timeSpent: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOTAL_TIME_SPENT] = timeSpent
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Mark onboarding as completed
     */
    suspend fun markOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = true
            preferences[LAST_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    /**
     * Check if onboarding is completed
     */
    fun isOnboardingCompleted(): Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    /**
     * Reset onboarding progress
     */
    suspend fun resetProgress() {
        context.dataStore.edit { preferences ->
            preferences.remove(ONBOARDING_COMPLETED)
            preferences.remove(CURRENT_STEP_ID)
            preferences.remove(COMPLETED_STEPS)
            preferences.remove(SKIPPED_STEPS)
            preferences.remove(START_TIME)
            preferences.remove(LAST_UPDATE_TIME)
            preferences.remove(TOTAL_TIME_SPENT)
            preferences.remove(FLOW_ID)
            preferences.remove(FLOW_VERSION)
        }
    }

    /**
     * Save flow configuration
     */
    suspend fun saveFlowConfig(flowId: String, version: String) {
        context.dataStore.edit { preferences ->
            preferences[FLOW_ID] = flowId
            preferences[FLOW_VERSION] = version
        }
    }

    /**
     * Get flow configuration
     */
    fun getFlowConfig(): Flow<Pair<String?, String?>> = context.dataStore.data.map { preferences ->
        Pair(preferences[FLOW_ID], preferences[FLOW_VERSION])
    }

    /**
     * Get completion rate
     */
    fun getCompletionRate(): Flow<Float> = context.dataStore.data.map { preferences ->
        val completed = preferences[COMPLETED_STEPS]?.size ?: 0
        val skipped = preferences[SKIPPED_STEPS]?.size ?: 0
        val total = completed + skipped
        
        if (total == 0) 0f else completed.toFloat() / total.toFloat()
    }

    /**
     * Get time spent on current session
     */
    fun getCurrentSessionTime(): Flow<Long> = context.dataStore.data.map { preferences ->
        val startTime = preferences[START_TIME] ?: System.currentTimeMillis()
        val lastUpdate = preferences[LAST_UPDATE_TIME] ?: System.currentTimeMillis()
        val totalSpent = preferences[TOTAL_TIME_SPENT] ?: 0L
        
        totalSpent + (System.currentTimeMillis() - lastUpdate)
    }
}
