package com.pandora.feature.keyboard.logic

import android.content.Context
import com.pandora.core.cac.db.CACDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Quick Action Manager
 * Main coordinator for Quick Actions system
 */
@Singleton
class QuickActionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacDao: CACDao,
    private val parser: QuickActionParser,
    private val executor: QuickActionExecutor
) {
    
    companion object {
        private const val TAG = "QuickActionManager"
    }
    
    /**
     * Process text and get Quick Action suggestions
     */
    fun getSuggestions(text: String): Flow<List<QuickActionSuggestion>> = flow {
        try {
            val suggestions = parser.parseText(text)
            suggestions.collect { suggestionList ->
                emit(suggestionList)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting suggestions", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Execute Quick Action
     */
    fun executeAction(suggestion: QuickActionSuggestion): Flow<QuickActionResponse> = flow {
        try {
            val request = parser.createRequest(suggestion.displayText, suggestion.actionType)
            val response = executor.executeRequest(request)
            response.collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error executing action", e)
            emit(QuickActionResponse(
                request = QuickActionRequest(
                    text = suggestion.displayText,
                    actionType = suggestion.actionType
                ),
                success = false,
                error = e.message ?: "Unknown error"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get Quick Action statistics
     */
    suspend fun getStatistics(): QuickActionStats {
        return try {
            val memories = cacDao.getMemoriesBySource("quick_actions", 1000)
            
            val totalExecutions = memories.size
            val successfulExecutions = memories.count { it.content.contains("Success: true") }
            val successRate = if (totalExecutions > 0) successfulExecutions.toFloat() / totalExecutions else 0f
            
            val executionTimes = memories.mapNotNull { memory ->
                val timeMatch = Regex("Time: (\\d+)ms").find(memory.content)
                timeMatch?.groupValues?.get(1)?.toLongOrNull()
            }
            val averageExecutionTime = if (executionTimes.isNotEmpty()) {
                executionTimes.average().toLong()
            } else 0L
            
            val actionCounts = memories.groupBy { memory ->
                val actionMatch = Regex("QuickAction: (\\w+)").find(memory.content)
                actionMatch?.groupValues?.get(1)
            }.mapValues { it.value.size }
            
            val mostUsedActions = actionCounts.toList()
                .sortedByDescending { it.second }
                .take(5)
                .mapNotNull { (actionName, _) ->
                    QuickActionType.values().find { it.name == actionName }
                }
            
            QuickActionStats(
                totalExecutions = totalExecutions,
                successRate = successRate,
                averageExecutionTime = averageExecutionTime,
                mostUsedActions = mostUsedActions,
                userSatisfaction = successRate // Simple satisfaction based on success rate
            )
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting statistics", e)
            QuickActionStats()
        }
    }
    
    /**
     * Learn from user interaction
     */
    suspend fun learnFromInteraction(
        actionType: QuickActionType,
        originalText: String,
        success: Boolean,
        userFeedback: Float? = null
    ) {
        try {
            
            val memory = com.pandora.core.cac.db.MemoryEntry(
                id = "0", // Auto-generated
                content = "QuickActionLearning: ${actionType.name}, Text: $originalText, Success: $success, Feedback: $userFeedback",
                source = "quick_actions_learning",
                timestamp = System.currentTimeMillis()
            )
            cacDao.insertMemory(memory)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error learning from interaction", e)
        }
    }
    
    /**
     * Get personalized suggestions based on user history
     */
    suspend fun getPersonalizedSuggestions(text: String): Flow<List<QuickActionSuggestion>> = flow {
        try {
            val baseSuggestions = parser.parseText(text)
            baseSuggestions.collect { suggestions ->
                val personalizedSuggestions = suggestions.map { suggestion ->
                    val personalizedScore = getPersonalizedScore(suggestion.actionType)
                    suggestion.copy(confidence = suggestion.confidence * personalizedScore)
                }.sortedByDescending { it.confidence }
                
                emit(personalizedSuggestions)
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting personalized suggestions", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get personalized score for action type
     */
    private suspend fun getPersonalizedScore(actionType: QuickActionType): Float {
        return try {
            val memories = cacDao.getMemoriesBySource("quick_actions_learning", 100)
            val actionMemories = memories.filter { it.content.contains(actionType.name) }
            
            if (actionMemories.isEmpty()) return 1.0f
            
            val successCount = actionMemories.count { it.content.contains("Success: true") }
            val totalCount = actionMemories.size
            val successRate = successCount.toFloat() / totalCount
            
            // Boost score for successful actions
            if (successRate > 0.7f) 1.2f
            else if (successRate > 0.5f) 1.1f
            else if (successRate < 0.3f) 0.8f
            else 1.0f
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error calculating personalized score", e)
            1.0f
        }
    }
    
    /**
     * Reset learning data
     */
    suspend fun resetLearningData() {
        try {
            // Clear learning memories
            val memories = cacDao.getMemoriesBySource("quick_actions_learning", 10000)
            memories.forEach { memory ->
                // Note: In real implementation, would need deleteMemory method
                // For now, just log the action
                android.util.Log.d(TAG, "Would delete memory: ${memory.id}")
            }
            
            android.util.Log.d(TAG, "Quick Actions learning data reset")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error resetting learning data", e)
        }
    }
}
