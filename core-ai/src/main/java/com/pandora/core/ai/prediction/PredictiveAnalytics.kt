package com.pandora.core.ai.prediction

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
import kotlin.math.sqrt

/**
 * Predictive Analytics for Neural Keyboard
 * Predicts user actions and provides proactive suggestions
 */
@Singleton
class PredictiveAnalytics @Inject constructor(
    private val context: Context,
    private val cacDao: CACDao
) {
    
    companion object {
        private const val TAG = "PredictiveAnalytics"
        private const val PREDICTION_WINDOW_HOURS = 24
        private const val MIN_CONFIDENCE_THRESHOLD = 0.6f
        private const val MAX_PREDICTIONS = 5
    }
    
    private val actionSequences = mutableListOf<ActionSequence>()
    private val timePatterns = mutableMapOf<Int, List<String>>() // Hour -> Actions
    private val contextPatterns = mutableMapOf<String, List<String>>() // Context -> Actions
    
    /**
     * Predict next actions based on current context
     */
    fun predictNextActions(
        currentText: String,
        currentContext: String,
        timeOfDay: Int
    ): Flow<List<ActionPrediction>> = flow {
        try {
            val predictions = mutableListOf<ActionPrediction>()
            
            // Time-based predictions
            val timeBasedPredictions = predictBasedOnTime(timeOfDay)
            predictions.addAll(timeBasedPredictions)
            
            // Context-based predictions
            val contextBasedPredictions = predictBasedOnContext(currentContext)
            predictions.addAll(contextBasedPredictions)
            
            // Sequence-based predictions
            val sequenceBasedPredictions = predictBasedOnSequence(currentText)
            predictions.addAll(sequenceBasedPredictions)
            
            // Merge and rank predictions
            val mergedPredictions = mergeAndRankPredictions(predictions)
            
            emit(mergedPredictions.take(MAX_PREDICTIONS))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error predicting next actions", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Predict based on time of day
     */
    private fun predictBasedOnTime(timeOfDay: Int): List<ActionPrediction> {
        val hour = timeOfDay
        val actions = timePatterns[hour] ?: emptyList()
        
        return actions.map { action ->
            ActionPrediction(
                action = action,
                confidence = 0.7f,
                reason = "Based on time pattern",
                predictionType = PredictionType.TIME_BASED
            )
        }
    }
    
    /**
     * Predict based on context
     */
    private fun predictBasedOnContext(context: String): List<ActionPrediction> {
        val actions = contextPatterns[context] ?: emptyList()
        
        return actions.map { action ->
            ActionPrediction(
                action = action,
                confidence = 0.8f,
                reason = "Based on context pattern",
                predictionType = PredictionType.CONTEXT_BASED
            )
        }
    }
    
    /**
     * Predict based on action sequences
     */
    private fun predictBasedOnSequence(currentText: String): List<ActionPrediction> {
        val relevantSequences = actionSequences.filter { sequence ->
            sequence.actions.any { it.contains(currentText, ignoreCase = true) }
        }
        
        return relevantSequences.map { sequence ->
            val nextAction = sequence.getNextAction(currentText)
            ActionPrediction(
                action = nextAction,
                confidence = sequence.confidence,
                reason = "Based on action sequence",
                predictionType = PredictionType.SEQUENCE_BASED
            )
        }
    }
    
    /**
     * Merge and rank predictions
     */
    private fun mergeAndRankPredictions(predictions: List<ActionPrediction>): List<ActionPrediction> {
        val actionGroups = predictions.groupBy { it.action }
        
        return actionGroups.map { (action, actionPredictions) ->
            val avgConfidence = actionPredictions.map { it.confidence }.average().toFloat()
            val reasons = actionPredictions.map { it.reason }.distinct()
            val types = actionPredictions.map { it.predictionType }.distinct()
            
            ActionPrediction(
                action = action,
                confidence = avgConfidence,
                reason = reasons.joinToString(", "),
                predictionType = types.firstOrNull() ?: PredictionType.UNKNOWN
            )
        }.sortedByDescending { it.confidence }
            .filter { it.confidence >= MIN_CONFIDENCE_THRESHOLD }
    }
    
    /**
     * Learn from user actions
     */
    suspend fun learnFromAction(
        action: String,
        context: String,
        timestamp: Long = System.currentTimeMillis()
    ) {
        try {
            val hour = (timestamp / (1000 * 60 * 60)) % 24
            
            // Update time patterns
            val currentTimeActions = timePatterns.getOrDefault(hour.toInt(), emptyList()).toMutableList()
            if (!currentTimeActions.contains(action)) {
                currentTimeActions.add(action)
                timePatterns[hour.toInt()] = currentTimeActions
            }
            
            // Update context patterns
            val currentContextActions = contextPatterns.getOrDefault(context, emptyList()).toMutableList()
            if (!currentContextActions.contains(action)) {
                currentContextActions.add(action)
                contextPatterns[context] = currentContextActions
            }
            
            // Update action sequences
            updateActionSequences(action, context, timestamp)
            
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error learning from action", e)
        }
    }
    
    /**
     * Update action sequences
     */
    private fun updateActionSequences(action: String, context: String, timestamp: Long) {
        // Find existing sequence or create new one
        val existingSequence = actionSequences.find { it.context == context }
        
        if (existingSequence != null) {
            existingSequence.addAction(action, timestamp)
        } else {
            val newSequence = ActionSequence(context, mutableListOf(action), timestamp)
            actionSequences.add(newSequence)
        }
        
        // Limit sequence length
        if (actionSequences.size > 100) {
            actionSequences.removeAt(0)
        }
    }
    
    /**
     * Get proactive suggestions
     */
    fun getProactiveSuggestions(
        currentTime: Long = System.currentTimeMillis(),
        currentContext: String = "unknown"
    ): Flow<List<ProactiveSuggestion>> = flow {
        try {
            val hour = (currentTime / (1000 * 60 * 60)) % 24
            val suggestions = mutableListOf<ProactiveSuggestion>()
            
            // Time-based proactive suggestions
            val timeSuggestions = getTimeBasedSuggestions(hour.toInt())
            suggestions.addAll(timeSuggestions)
            
            // Context-based proactive suggestions
            val contextSuggestions = getContextBasedSuggestions(currentContext)
            suggestions.addAll(contextSuggestions)
            
            // Pattern-based proactive suggestions
            val patternSuggestions = getPatternBasedSuggestions()
            suggestions.addAll(patternSuggestions)
            
            emit(suggestions.sortedByDescending { it.priority })
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error getting proactive suggestions", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get time-based suggestions
     */
    private fun getTimeBasedSuggestions(hour: Int): List<ProactiveSuggestion> {
        val suggestions = mutableListOf<ProactiveSuggestion>()
        
        when (hour) {
            in 6..9 -> {
                suggestions.add(ProactiveSuggestion(
                    title = "Good morning!",
                    description = "Check your calendar for today's meetings",
                    action = "calendar",
                    priority = 0.8f,
                    suggestionType = SuggestionType.TIME_BASED
                ))
            }
            in 12..14 -> {
                suggestions.add(ProactiveSuggestion(
                    title = "Lunch break",
                    description = "Set a reminder for your next meeting",
                    action = "reminder",
                    priority = 0.7f,
                    suggestionType = SuggestionType.TIME_BASED
                ))
            }
            in 17..19 -> {
                suggestions.add(ProactiveSuggestion(
                    title = "End of day",
                    description = "Review your notes and plan for tomorrow",
                    action = "notes",
                    priority = 0.9f,
                    suggestionType = SuggestionType.TIME_BASED
                ))
            }
        }
        
        return suggestions
    }
    
    /**
     * Get context-based suggestions
     */
    private fun getContextBasedSuggestions(context: String): List<ProactiveSuggestion> {
        val suggestions = mutableListOf<ProactiveSuggestion>()
        
        when (context.lowercase()) {
            "messaging" -> {
                suggestions.add(ProactiveSuggestion(
                    title = "Quick reply",
                    description = "Send a quick response",
                    action = "send_message",
                    priority = 0.8f,
                    suggestionType = SuggestionType.CONTEXT_BASED
                ))
            }
            "calendar" -> {
                suggestions.add(ProactiveSuggestion(
                    title = "Schedule meeting",
                    description = "Add a new event to your calendar",
                    action = "calendar",
                    priority = 0.9f,
                    suggestionType = SuggestionType.CONTEXT_BASED
                ))
            }
            "notes" -> {
                suggestions.add(ProactiveSuggestion(
                    title = "Create note",
                    description = "Jot down your thoughts",
                    action = "note",
                    priority = 0.7f,
                    suggestionType = SuggestionType.CONTEXT_BASED
                ))
            }
        }
        
        return suggestions
    }
    
    /**
     * Get pattern-based suggestions
     */
    private fun getPatternBasedSuggestions(): List<ProactiveSuggestion> {
        val suggestions = mutableListOf<ProactiveSuggestion>()
        
        // Analyze recent patterns and suggest accordingly
        val recentActions = actionSequences.takeLast(10)
        val actionCounts = recentActions.flatMap { it.actions }.groupingBy { it }.eachCount()
        
        val mostFrequentAction = actionCounts.maxByOrNull { it.value }?.key
        if (mostFrequentAction != null) {
            suggestions.add(ProactiveSuggestion(
                title = "Frequent action",
                description = "You often use this action",
                action = mostFrequentAction,
                priority = 0.6f,
                suggestionType = SuggestionType.PATTERN_BASED
            ))
        }
        
        return suggestions
    }
    
    /**
     * Get prediction accuracy
     */
    suspend fun getPredictionAccuracy(): Flow<Float> = flow {
        try {
            val recentMemories = cacDao.getMemoriesBySource("prediction", 100)
            val accuracy = calculateAccuracy(recentMemories)
            emit(accuracy)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Error calculating prediction accuracy", e)
            emit(0f)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Calculate prediction accuracy
     */
    private fun calculateAccuracy(memories: List<MemoryEntry>): Float {
        if (memories.isEmpty()) return 0f
        
        val correctPredictions = memories.count { memory ->
            memory.content.contains("correct: true", ignoreCase = true)
        }
        
        return correctPredictions.toFloat() / memories.size
    }
}

/**
 * Action sequence for learning patterns
 */
data class ActionSequence(
    val context: String,
    val actions: MutableList<String>,
    val startTime: Long,
    var confidence: Float = 0.5f
) {
    fun addAction(action: String, timestamp: Long) {
        actions.add(action)
        // Update confidence based on sequence length
        confidence = (actions.size * 0.1f).coerceAtMost(1.0f)
    }
    
    fun getNextAction(currentText: String): String {
        val currentIndex = actions.indexOfFirst { it.contains(currentText, ignoreCase = true) }
        return if (currentIndex >= 0 && currentIndex < actions.size - 1) {
            actions[currentIndex + 1]
        } else {
            actions.firstOrNull() ?: "unknown"
        }
    }
}

/**
 * Action prediction
 */
data class ActionPrediction(
    val action: String,
    val confidence: Float,
    val reason: String,
    val predictionType: PredictionType
)

/**
 * Proactive suggestion
 */
data class ProactiveSuggestion(
    val title: String,
    val description: String,
    val action: String,
    val priority: Float,
    val suggestionType: SuggestionType
)

/**
 * Prediction types
 */
enum class PredictionType {
    TIME_BASED, CONTEXT_BASED, SEQUENCE_BASED, UNKNOWN
}

/**
 * Suggestion types
 */
enum class SuggestionType {
    TIME_BASED, CONTEXT_BASED, PATTERN_BASED, UNKNOWN
}
