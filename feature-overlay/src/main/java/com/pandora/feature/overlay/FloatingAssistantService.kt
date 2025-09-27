package com.pandora.feature.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*

class FloatingAssistantService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingOrbView: ComposeView? = null
    private val lifecycleOwner = ServiceLifecycleOwner()

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        lifecycleOwner.onCreate()
        showOrb()
    }

    private fun showOrb() {
        if (floatingOrbView != null) return

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        floatingOrbView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setContent {
                FloatingOrb()
            }
        }
        
        windowManager.addView(floatingOrbView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleOwner.onDestroy()
        floatingOrbView?.let { windowManager.removeView(it) }
        floatingOrbView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

// Helper class để quản lý Lifecycle cho Compose trong Service
private class ServiceLifecycleOwner : ViewModelStoreOwner, LifecycleOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val _viewModelStore = ViewModelStore()

    fun onCreate() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onDestroy() = lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = _viewModelStore
}
