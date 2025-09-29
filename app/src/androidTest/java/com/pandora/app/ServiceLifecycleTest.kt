package com.pandora.app

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

// FIXED: FlowEngineService lifecycle
@RunWith(AndroidJUnit4::class)
class ServiceLifecycleTest {
    @Test
    fun startStopForegroundService() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, FlowEngineService::class.java)
        // Start foreground service; on older APIs, this will fallback to startService
        context.startForegroundService(intent)
        // Stop service safely
        context.stopService(intent)
    }
}
