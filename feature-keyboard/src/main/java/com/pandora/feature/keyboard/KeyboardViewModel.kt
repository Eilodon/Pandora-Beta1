package com.pandora.feature.keyboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandora.core.cac.db.MemoryDao
import com.pandora.core.cac.db.MemoryEntryEntity
import com.pandora.feature.keyboard.logic.ActionExecutor
import com.pandora.feature.keyboard.logic.InferenceEngine
import com.pandora.feature.keyboard.logic.InferredAction
import com.pandora.feature.keyboard.logic.checkAllMiniFlows
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
    private val actionExecutor: ActionExecutor // Inject ActionExecutor
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

    fun onTextChanged(currentText: String) {
        // Mỗi khi văn bản thay đổi, hãy thử suy luận hành động
        _inferredAction.value = inferenceEngine.inferActionFromText(currentText)
        
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
}
