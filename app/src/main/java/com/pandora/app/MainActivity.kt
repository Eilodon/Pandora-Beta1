package com.pandora.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import com.pandora.app.permissions.PermissionUtils
import com.pandora.app.permissions.PermissionActivity
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
        // FIXED: use Timber for logging
        timber.log.Timber.d("PandoraOS v0.1.0-chimera - CAC Database: %s", if (::cacDao.isInitialized) "Connected ✅" else "Disconnected ❌")
        
        // Initialize performance monitoring
        initializePerformanceMonitoring()

        // Initialize gamification system
        initializeGamificationSystem()

        // Initialize B2B system
        initializeB2BSystem()

        // Khởi động FlowEngineService để lắng nghe các trigger hệ thống
        // FIXED: permission gate - launch intro screen if missing BLE permissions
        if (!PermissionUtils.hasBlePermissions(this)) {
            startActivity(Intent(this, PermissionActivity::class.java))
        } else {
            startService(Intent(this, FlowEngineService::class.java))
        }
        
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
        
        // FIXED: use Timber
        timber.log.Timber.d("PandoraOS v0.1.0-chimera - Performance Monitoring: Initialized ✅")
        timber.log.Timber.d("Memory Usage: %sMB / %sMB", memoryUsage.usedMemory / 1024 / 1024, memoryUsage.maxMemory / 1024 / 1024)
        timber.log.Timber.d("CPU Usage: %s%%", cpuUsage.usagePercentage)
    }

    private fun initializeGamificationSystem() {
        // FIXED: lifecycle-aware coroutines (replace GlobalScope with lifecycleScope)
        lifecycleScope.launch {
            try {
                gamificationManager.initialize()
                timber.log.Timber.d("Gamification System initialized successfully ✅")
            } catch (e: Exception) {
                timber.log.Timber.w(e, "Failed to initialize Gamification System")
            }
        }

        // Example for lifecycle-bound collectors if needed in future
        // FIXED: lifecycle-aware collectors using repeatOnLifecycle
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect flows here when needed
            }
        }
    }

    private fun initializeB2BSystem() {
        // FIXED: lifecycle-aware coroutines (replace GlobalScope with lifecycleScope)
        lifecycleScope.launch {
            try {
                b2bManager.initialize()
                timber.log.Timber.d("B2B System initialized successfully ✅")
            } catch (e: Exception) {
                timber.log.Timber.w(e, "Failed to initialize B2B System")
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
