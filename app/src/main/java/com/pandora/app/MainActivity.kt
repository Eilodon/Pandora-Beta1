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
import com.pandora.feature.onboarding.OnboardingManager
import com.pandora.feature.onboarding.OnboardingOverlay
import com.pandora.feature.gamification.GamificationManager
import com.pandora.feature.gamification.GamificationDashboard
import com.pandora.feature.b2b.B2BManager
import com.pandora.feature.b2b.B2BDashboard
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    
    @Inject
    lateinit var onboardingManager: OnboardingManager

    @Inject
    lateinit var gamificationManager: GamificationManager

    @Inject
    lateinit var b2bManager: B2BManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test Hilt injection - "Kinh mạch" đã hoạt động!
        // CAC Database đã được inject thành công!
        println("PandoraOS v0.1.0-chimera - CAC Database: ${if (::cacDao.isInitialized) "Connected ✅" else "Disconnected ❌"}")
        
        // Initialize performance monitoring
        initializePerformanceMonitoring()

        // Initialize gamification system
        initializeGamificationSystem()

        // Initialize B2B system
        initializeB2BSystem()

        // Khởi động FlowEngineService để lắng nghe các trigger hệ thống
        startService(Intent(this, FlowEngineService::class.java))
        
        setContent {
            PandoraOSTheme(isDarkTheme = true) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DesignKitDemo()

                    // Onboarding overlay
                    OnboardingOverlay(onboardingManager = onboardingManager)
                    
                    // Gamification dashboard
                    GamificationDashboard(gamificationManager = gamificationManager)
                    
                    // B2B dashboard
                    B2BDashboard(b2bManager = b2bManager)
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

    private fun initializeGamificationSystem() {
        // Initialize gamification system in a coroutine
        kotlinx.coroutines.GlobalScope.launch {
            try {
                gamificationManager.initialize()
                println("Gamification System initialized successfully ✅")
            } catch (e: Exception) {
                println("Failed to initialize Gamification System: ${e.message} ❌")
            }
        }
    }

    private fun initializeB2BSystem() {
        // Initialize B2B system in a coroutine
        kotlinx.coroutines.GlobalScope.launch {
            try {
                b2bManager.initialize()
                println("B2B System initialized successfully ✅")
            } catch (e: Exception) {
                println("Failed to initialize B2B System: ${e.message} ❌")
            }
        }
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
