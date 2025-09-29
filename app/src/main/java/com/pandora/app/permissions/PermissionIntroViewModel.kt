package com.pandora.app.permissions

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// FIXED: Permission VM
class PermissionIntroViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _state = MutableStateFlow(PermissionState.initial())
    val state: StateFlow<PermissionState> = _state

    fun refresh() {
        val granted = PermissionUtils.hasBlePermissions(appContext)
        _state.value = if (granted) PermissionState(granted = true) else PermissionState(granted = false)
    }

    fun onGrantClicked(activity: Activity) {
        PermissionUtils.requestBlePermissions(activity)
    }
}

data class PermissionState(
    val granted: Boolean,
    val showRationale: Boolean = false
) {
    companion object {
        fun initial() = PermissionState(granted = false)
    }
}
