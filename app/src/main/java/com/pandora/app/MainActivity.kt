package com.pandora.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pandora.core.ui.theme.PandoraOSTheme
import com.pandora.core.ui.designkit.demo.DesignKitDemo
import com.pandora.feature.overlay.FloatingAssistantService
import com.pandora.core.cac.db.CACDao
import com.pandora.app.performance.MemoryOptimizer
import com.pandora.app.performance.CPUOptimizer
import com.pandora.app.performance.PerformanceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var cacDao: CACDao
    
    @Inject
    lateinit var memoryOptimizer: MemoryOptimizer
    
    @Inject
    lateinit var cpuOptimizer: CPUOptimizer
    
    @Inject
    lateinit var performanceMonitor: PerformanceMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test Hilt injection - "Kinh mạch" đã hoạt động!
        // CAC Database đã được inject thành công!
        println("PandoraOS v0.1.0-chimera - CAC Database: ${if (::cacDao.isInitialized) "Connected ✅" else "Disconnected ❌"}")
        
        // Initialize performance monitoring
        initializePerformanceMonitoring()
        
        // Khởi động FlowEngineService để lắng nghe các trigger hệ thống
        startService(Intent(this, FlowEngineService::class.java))
        
        setContent {
            PandoraOSTheme(isDarkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DesignKitDemo()
                }
            }
        }
    }

    private fun initializePerformanceMonitoring() {
        // Start performance monitoring
        performanceMonitor.startMonitoring()
        
        // Record app startup time
        performanceMonitor.recordScreenLoadTime("MainActivity", System.currentTimeMillis())
        
        // Initialize memory optimization
        val memoryUsage = memoryOptimizer.getMemoryUsage()
        performanceMonitor.recordMemoryUsage(memoryUsage.usedMemory)
        
        // Initialize CPU optimization
        val cpuUsage = cpuOptimizer.getCPUUsage()
        performanceMonitor.recordCPUUsage(cpuUsage.usagePercentage)
        
        println("PandoraOS v0.1.0-chimera - Performance Monitoring: Initialized ✅")
        println("Memory Usage: ${memoryUsage.usedMemory / 1024 / 1024}MB / ${memoryUsage.maxMemory / 1024 / 1024}MB")
        println("CPU Usage: ${cpuUsage.usagePercentage}%")
    }

    private fun checkOverlayPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Yêu cầu quyền
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            // Bắt đầu service
            startService(Intent(this, FloatingAssistantService::class.java))
        }
    }
}
