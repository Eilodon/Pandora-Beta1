package com.pandora.feature.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedText
import androidx.compose.ui.platform.ComposeView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NeuralKeyboardService : InputMethodService(), LifecycleOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val inputManager = InputManager()
    private lateinit var viewModel: KeyboardViewModel

    override val lifecycle: Lifecycle = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setContent {
                viewModel = hiltViewModel() // Lấy ViewModel từ Hilt
                PandoraKeyboardView(inputManager, viewModel) // Truyền ViewModel vào UI
            }
        }.also { composeView ->
            composeView.setViewTreeLifecycleOwner(this)
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        inputManager.initialize(currentInputConnection)
    }

    // Phương thức này được gọi mỗi khi có thay đổi trong trình soạn thảo
    override fun onUpdateExtractedText(token: Int, text: ExtractedText) {
        super.onUpdateExtractedText(token, text)
        if (::viewModel.isInitialized) {
            viewModel.onTextChanged(text.text.toString())
        }
    }
    
    override fun onInitializeInterface() {
        super.onInitializeInterface()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
