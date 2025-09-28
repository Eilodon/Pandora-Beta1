package com.pandora.core.ai.personalization

import android.content.Context
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.MemoryEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.ln

/**
 * Personalization Engine for Neural Keyboard
 * Learns from user behavior and adapts suggestions
 */
@Singleton
class PersonalizationEngine @Inject constructor(
    private val context: Context,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "PersonalizationEngine"
        private const val LEARNING_RATE = 0.1f
        private const val DECAY_FACTOR = 0.95f
        private const val MAX_MEMORY_AGE_DAYS = 30
    }
    
    private val userPreferences = mutableMapOf<String, Float>()
    private val actionFrequencies = mutableMapOf<String, Int>()
    private val contextPatterns = mutableMapOf<String, ContextPattern>()
    
    /**
     * Learn from user action
     */
    suspend fun learnFromAction(
        action: String,
        context: String,
        success: Boolean,
        timestamp: Long = System.currentTimeMillis()
    ) {
        try {
            // Update action frequency
            actionFrequencies[action] = actionFrequencies.getOrDefault(action, 0) + 1
            
            // Update context patterns
            val pattern = contextPatterns.getOrPut(context) { ContextPattern(context) }
            pattern.addAction(action, success, timestamp)
            
            // Update user preferences
            val preferenceKey = "${action}_${context}"
            val currentPreference = userPreferences.getOrDefault(preferenceKey, 0.5f)
            val adjustment = if (success) LEARNING_RATE else -LEARNING_RATE
            userPreferences[preferenceKey] = (currentPreference + adjustment).coerceIn(0f, 1f)
            
            // Save to memory
            saveLearningToMemory(action, context, success, timestamp)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error learning from action", e)
        }
    }
    
    /**
     * Get personalized suggestions
     */
    fun getPersonalizedSuggestions(
        text: String,
        context: String,
        baseSuggestions: List<String>
    ): Flow<List<PersonalizedSuggestion>> = flow {
        try {
            val personalizedSuggestions = baseSuggestions.map { suggestion ->
                val personalizationScore = calculatePersonalizationScore(suggestion, context)
                val frequencyScore = calculateFrequencyScore(suggestion)
                val contextScore = calculateContextScore(suggestion, context)
                
                val finalScore = (personalizationScore * 0.4f + 
                                frequencyScore * 0.3f + 
                                contextScore * 0.3f)
                
                PersonalizedSuggestion(
                    suggestion = suggestion,
                    score = finalScore,
                    personalizationFactor = personalizationScore,
                    frequencyFactor = frequencyScore,
                    contextFactor = contextScore
                )
            }.sortedByDescending { it.score }
            
            emit(personalizedSuggestions)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting personalized suggestions", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Calculate personalization score
     */
    private fun calculatePersonalizationScore(suggestion: String, context: String): Float {
        val preferenceKey = "${suggestion}_${context}"
        return userPreferences.getOrDefault(preferenceKey, 0.5f)
    }
    
    /**
     * Calculate frequency score
     */
    private fun calculateFrequencyScore(suggestion: String): Float {
        val frequency = actionFrequencies.getOrDefault(suggestion, 0)
        return if (frequency > 0) {
            // Normalize frequency to 0-1 range
            (frequency.toFloat() / (frequency + 1)).coerceIn(0f, 1f)
        } else {
            0.5f // Default score for new actions
        }
    }
    
    /**
     * Calculate context score
     */
    private fun calculateContextScore(suggestion: String, context: String): Float {
        val pattern = contextPatterns[context] ?: return 0.5f
        return pattern.getActionScore(suggestion)
    }
    
    /**
     * Get user behavior insights
     */
    suspend fun getUserInsights(): Flow<UserInsights> = flow {
        try {
            val recentMemories = cacDao.getMemoriesBySource("keyboard", 100)
            val insights = analyzeUserBehavior(recentMemories)
            emit(insights)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting user insights", e)
            emit(UserInsights.createEmpty())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Analyze user behavior
     */
    private fun analyzeUserBehavior(memories: List<MemoryEntry>): UserInsights {
        val actionCounts = mutableMapOf<String, Int>()
        val contextCounts = mutableMapOf<String, Int>()
        val timePatterns = mutableMapOf<Int, Int>() // Hour -> Count
        
        memories.forEach { memory ->
            // Count actions
            val action = extractActionFromMemory(memory)
            if (action.isNotEmpty()) {
                actionCounts[action] = actionCounts.getOrDefault(action, 0) + 1
            }
            
            // Count contexts
            val context = extractContextFromMemory(memory)
            if (context.isNotEmpty()) {
                contextCounts[context] = contextCounts.getOrDefault(context, 0) + 1
            }
            
            // Count time patterns
            val hour = (memory.timestamp / (1000 * 60 * 60)) % 24
            timePatterns[hour.toInt()] = timePatterns.getOrDefault(hour.toInt(), 0) + 1
        }
        
        return UserInsights(
            mostUsedActions = actionCounts.toList().sortedByDescending { it.second }.take(5).map { it.first },
            mostUsedContexts = contextCounts.toList().sortedByDescending { it.second }.take(5).map { it.first },
            peakUsageHours = timePatterns.toList().sortedByDescending { it.second }.take(3).map { it.first },
            totalActions = memories.size,
            learningProgress = calculateLearningProgress()
        )
    }
    
    /**
     * Calculate learning progress
     */
    private fun calculateLearningProgress(): Float {
        val totalPreferences = userPreferences.size
        val learnedPreferences = userPreferences.count { it.value != 0.5f }
        return if (totalPreferences > 0) {
            learnedPreferences.toFloat() / totalPreferences
        } else {
            0f
        }
    }
    
    /**
     * Save learning to memory
     */
    private suspend fun saveLearningToMemory(
        action: String,
        context: String,
        success: Boolean,
        timestamp: Long
    ) {
        try {
            val memory = MemoryEntry(
                id = "0", // Auto-generated
                content = "Action: $action, Context: $context, Success: $success",
                source = "personalization",
                timestamp = timestamp
            )
            cacDao.insertMemory(memory)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error saving learning to memory", e)
        }
    }
    
    /**
     * Extract action from memory
     */
    private fun extractActionFromMemory(memory: MemoryEntry): String {
        // Simple extraction from content
        return if (memory.content.contains("Action:")) {
            memory.content.substringAfter("Action: ").substringBefore(",")
        } else {
            ""
        }
    }
    
    /**
     * Extract context from memory
     */
    private fun extractContextFromMemory(memory: MemoryEntry): String {
        // Simple extraction from content
        return if (memory.content.contains("Context:")) {
            memory.content.substringAfter("Context: ").substringBefore(",")
        } else {
            ""
        }
    }
    
    /**
     * Reset personalization data
     */
    fun resetPersonalization() {
        userPreferences.clear()
        actionFrequencies.clear()
        contextPatterns.clear()
    }
}

/**
 * Context pattern for learning
 */
data class ContextPattern(
    val context: String,
    private val actions: MutableMap<String, ActionHistory> = mutableMapOf()
) {
    fun addAction(action: String, success: Boolean, timestamp: Long) {
        val history = actions.getOrPut(action) { ActionHistory() }
        history.addAttempt(success, timestamp)
    }
    
    fun getActionScore(action: String): Float {
        val history = actions[action] ?: return 0.5f
        return history.getSuccessRate()
    }
}

/**
 * Action history for tracking success/failure
 */
data class ActionHistory(
    private val attempts: MutableList<Attempt> = mutableListOf()
) {
    fun addAttempt(success: Boolean, timestamp: Long) {
        attempts.add(Attempt(success, timestamp))
    }
    
    fun getSuccessRate(): Float {
        if (attempts.isEmpty()) return 0.5f
        val successfulAttempts = attempts.count { it.success }
        return successfulAttempts.toFloat() / attempts.size
    }
}

/**
 * Individual attempt
 */
data class Attempt(
    val success: Boolean,
    val timestamp: Long
)

/**
 * Personalized suggestion
 */
data class PersonalizedSuggestion(
    val suggestion: String,
    val score: Float,
    val personalizationFactor: Float,
    val frequencyFactor: Float,
    val contextFactor: Float
)

/**
 * User insights
 */
data class UserInsights(
    val mostUsedActions: List<String>,
    val mostUsedContexts: List<String>,
    val peakUsageHours: List<Int>,
    val totalActions: Int,
    val learningProgress: Float
) {
    companion object {
        fun createEmpty() = UserInsights(
            mostUsedActions = emptyList(),
            mostUsedContexts = emptyList(),
            peakUsageHours = emptyList(),
            totalActions = 0,
            learningProgress = 0f
        )
    }
}
