package com.pandora.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandora.core.cac.db.MemoryDao
import com.pandora.core.cac.db.MemoryEntryEntity
import com.pandora.feature.keyboard.logic.ActionExecutor
import com.pandora.feature.keyboard.logic.InferenceEngine
import com.pandora.feature.keyboard.logic.InferredAction
import com.pandora.feature.keyboard.logic.checkAllMiniFlows
import com.pandora.feature.keyboard.logic.QuickActionManager
import com.pandora.feature.keyboard.logic.QuickActionSuggestion
import com.pandora.feature.keyboard.logic.QuickActionResponse
import com.pandora.core.ai.EnhancedInferenceEngine
import com.pandora.core.ai.EnhancedInferenceResult
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class KeyboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // Inject context
    private val memoryDao: MemoryDao,
    private val inferenceEngine: InferenceEngine, // Inject InferenceEngine
    private val enhancedInferenceEngine: EnhancedInferenceEngine, // Inject Enhanced Inference Engine
    private val actionExecutor: ActionExecutor, // Inject ActionExecutor
    private val quickActionManager: QuickActionManager // Inject QuickActionManager
) : ViewModel() {

    val recentMemories: StateFlow<List<MemoryEntryEntity>> =
        memoryDao.getRecentEntries(limit = 5)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _inferredAction = MutableStateFlow<InferredAction?>(null)
    val inferredAction: StateFlow<InferredAction?> = _inferredAction

    private val _enhancedInferenceResult = MutableStateFlow<EnhancedInferenceResult?>(null)
    val enhancedInferenceResult: StateFlow<EnhancedInferenceResult?> = _enhancedInferenceResult

    private val _quickActionSuggestions = MutableStateFlow<List<QuickActionSuggestion>>(emptyList())
    val quickActionSuggestions: StateFlow<List<QuickActionSuggestion>> = _quickActionSuggestions

    private val _quickActionResponse = MutableStateFlow<QuickActionResponse?>(null)
    val quickActionResponse: StateFlow<QuickActionResponse?> = _quickActionResponse

    fun onTextChanged(currentText: String) {
        // Mỗi khi văn bản thay đổi, hãy thử suy luận hành động
        _inferredAction.value = inferenceEngine.inferActionFromText(currentText)
        
        // Sử dụng Enhanced Inference Engine cho phân tích nâng cao
        viewModelScope.launch {
            enhancedInferenceEngine.analyzeTextEnhanced(currentText)
                .collect { result ->
                    _enhancedInferenceResult.value = result
                }
        }
        
        // Get Quick Action suggestions
        viewModelScope.launch {
            quickActionManager.getSuggestions(currentText)
                .collect { suggestions ->
                    _quickActionSuggestions.value = suggestions
                }
        }
        
        // Kích hoạt tất cả mini-flows
        checkAllMiniFlows(context, currentText)
    }

    fun recordTypingMemory(typedText: String) {
        if (typedText.isBlank()) return

        viewModelScope.launch {
            val memory = MemoryEntryEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                source = "keyboard",
                content = typedText
            )
            memoryDao.insert(memory)
        }
    }
    
    fun executeAction(action: InferredAction) {
        actionExecutor.execute(action.action) // Gọi đến ActionExecutor
        
        // Ghi nhớ rằng hành động đã được thực thi
        viewModelScope.launch {
            val memory = MemoryEntryEntity(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                source = "action_executed",
                content = "Executed: ${action.action::class.simpleName} from '${action.sourceText}'"
            )
            memoryDao.insert(memory)
        }

        _inferredAction.value = null // Xóa gợi ý sau khi thực thi
    }

    /**
     * Initialize Enhanced Inference Engine
     */
    fun initializeEnhancedAI() {
        viewModelScope.launch {
            try {
                enhancedInferenceEngine.initialize()
                android.util.Log.d("KeyboardViewModel", "Enhanced AI initialized successfully")
            } catch (e: Exception) {
                android.util.Log.e("KeyboardViewModel", "Error initializing Enhanced AI", e)
            }
        }
    }

    /**
     * Execute Quick Action
     */
    fun executeQuickAction(suggestion: QuickActionSuggestion) {
        viewModelScope.launch {
            try {
                quickActionManager.executeAction(suggestion)
                    .collect { response ->
                        _quickActionResponse.value = response
                        
                        // Clear suggestions after execution
                        _quickActionSuggestions.value = emptyList()
                        
                        // Learn from interaction
                        quickActionManager.learnFromInteraction(
                            actionType = suggestion.actionType,
                            originalText = suggestion.displayText,
                            success = response.success
                        )
                        
                        android.util.Log.d("KeyboardViewModel", "Quick Action executed: ${suggestion.actionType}")
                    }
            } catch (e: Exception) {
                android.util.Log.e("KeyboardViewModel", "Error executing Quick Action", e)
            }
        }
    }

    /**
     * Get user insights from Enhanced AI
     */
    fun getUserInsights() {
        viewModelScope.launch {
            try {
                enhancedInferenceEngine.getUserInsights()
                    .collect { insights ->
                        android.util.Log.d("KeyboardViewModel", "User insights: $insights")
                    }
            } catch (e: Exception) {
                android.util.Log.e("KeyboardViewModel", "Error getting user insights", e)
            }
        }
    }
}
