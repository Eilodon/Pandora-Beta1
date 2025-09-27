package com.pandora.feature.keyboard

import android.view.inputmethod.InputConnection

class InputManager {
    private var inputConnection: InputConnection? = null

    fun initialize(inputConnection: InputConnection?) {
        this.inputConnection = inputConnection
    }

    fun sendText(text: String) {
        inputConnection?.commitText(text, 1)
    }

    fun sendKey(keyCode: Int) {
        // Sẽ hiện thực sau
    }
}
