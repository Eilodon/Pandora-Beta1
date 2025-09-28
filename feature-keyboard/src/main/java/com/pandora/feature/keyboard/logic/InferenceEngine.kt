package com.pandora.feature.keyboard.logic

import android.content.Context
import com.pandora.core.ai.ml.ActionSuggestion
import com.pandora.core.ai.ml.ActionType
import com.pandora.core.ai.ml.ModelManager
import com.pandora.core.cac.db.CACDao
import com.pandora.core.cac.db.MemoryEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enhanced Inference Engine for Neural Keyboard
 * Integrates with TensorFlow Lite for real AI inference
 */
@Singleton
class InferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacDao: CACDao,
    private val modelManager: ModelManager
) {
    
    /**
     * Analyze text and return action suggestions using AI
     */
    fun analyzeText(text: String): Flow<List<ActionSuggestion>> = flow {
        try {
            // Get AI suggestions from TensorFlow Lite model
            val aiSuggestions = modelManager.analyzeText(text)
            
            // Get context from recent memories
            val contextMemories = getContext()
            
            // Combine AI suggestions with context-based suggestions
            val contextualSuggestions = enhanceWithContext(aiSuggestions, contextMemories, text)
            
            emit(contextualSuggestions)
        } catch (e: Exception) {
            // Fallback to rule-based suggestions
            val fallbackSuggestions = getFallbackSuggestions(text)
            emit(fallbackSuggestions)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get context from recent memories
     */
    private suspend fun getContext(): List<MemoryEntry> {
        return try {
            cacDao.getMemoriesBySource("keyboard", 10)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Enhance AI suggestions with context from memories
     */
    private fun enhanceWithContext(
        aiSuggestions: List<ActionSuggestion>,
        memories: List<MemoryEntry>,
        currentText: String
    ): List<ActionSuggestion> {
        val enhancedSuggestions = aiSuggestions.toMutableList()
        
        // Add context-based suggestions
        memories.forEach { memory ->
            if (memory.content.contains(currentText, ignoreCase = true)) {
                // If similar text was used before, boost confidence
                enhancedSuggestions.forEach { suggestion ->
                    if (suggestion.action == ActionType.CALENDAR && 
                        memory.content.contains("họp", ignoreCase = true)) {
                        // Boost calendar suggestion confidence
                    }
                }
            }
        }
        
        return enhancedSuggestions.sortedByDescending { it.confidence }
    }
    
    /**
     * Fallback rule-based suggestions when AI fails
     */
    private fun getFallbackSuggestions(text: String): List<ActionSuggestion> {
        val suggestions = mutableListOf<ActionSuggestion>()
        val lowerText = text.lowercase()
        
        // Calendar patterns
        if (lowerText.contains("họp") || lowerText.contains("meeting") || 
            lowerText.contains("lịch") || lowerText.contains("calendar")) {
            suggestions.add(ActionSuggestion(ActionType.CALENDAR, 0.9f))
        }
        
        // Reminder patterns
        if (lowerText.contains("nhắc") || lowerText.contains("remind") || 
            lowerText.contains("nhớ") || lowerText.contains("remember")) {
            suggestions.add(ActionSuggestion(ActionType.REMIND, 0.8f))
        }
        
        // Send patterns
        if (lowerText.contains("gửi") || lowerText.contains("send") || 
            lowerText.contains("share") || lowerText.contains("chia sẻ")) {
            suggestions.add(ActionSuggestion(ActionType.SEND, 0.7f))
        }
        
        // Note patterns
        if (lowerText.contains("ghi chú") || lowerText.contains("note") || 
            lowerText.contains("ghi") || lowerText.contains("write")) {
            suggestions.add(ActionSuggestion(ActionType.NOTE, 0.8f))
        }
        
        // Math patterns
        if (lowerText.contains("tính") || lowerText.contains("math") || 
            lowerText.contains("calculator") || lowerText.contains("+") || 
            lowerText.contains("-") || lowerText.contains("*") || lowerText.contains("/")) {
            suggestions.add(ActionSuggestion(ActionType.MATH, 0.9f))
        }
        
        // Search patterns
        if (lowerText.contains("tìm") || lowerText.contains("search") || 
            lowerText.contains("google") || lowerText.contains("what is")) {
            suggestions.add(ActionSuggestion(ActionType.SEARCH, 0.8f))
        }
        
        return suggestions.take(4)
    }
    
    /**
     * Legacy method for backward compatibility
     */
    fun inferActionFromText(text: String): InferredAction? {
        val suggestions = getFallbackSuggestions(text)
        val topSuggestion = suggestions.firstOrNull()
        
        return topSuggestion?.let { suggestion ->
            InferredAction(
                sourceText = text,
                action = convertToPandoraAction(suggestion),
                confidence = suggestion.confidence
            )
        }
    }
    
    /**
     * Convert ActionSuggestion to PandoraAction
     */
    private fun convertToPandoraAction(suggestion: ActionSuggestion): PandoraAction {
        return when (suggestion.action) {
            ActionType.CALENDAR -> PandoraAction.AddToCalendar(
                title = "Cuộc họp",
                startTime = System.currentTimeMillis() + (2 * 60 * 60 * 1000) // 2 hours from now
            )
            ActionType.REMIND -> PandoraAction.CreateReminder(
                title = "Nhắc nhở",
                message = "Nhắc nhở từ PandoraOS"
            )
            ActionType.SEND -> PandoraAction.SendMessage(
                recipient = "",
                message = "Tin nhắn từ PandoraOS"
            )
            ActionType.NOTE -> PandoraAction.CreateNote(
                title = "Ghi chú",
                content = "Nội dung ghi chú"
            )
            ActionType.MATH -> PandoraAction.Calculate(
                expression = "2 + 2"
            )
            ActionType.SEARCH -> PandoraAction.Search(
                query = "Tìm kiếm"
            )
            else -> PandoraAction.Unknown
        }
    }
}