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
import com.pandora.feature.overlay.FloatingAssistantService
import com.pandora.core.cac.db.CACDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var cacDao: CACDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Test Hilt injection - "Kinh mạch" đã hoạt động!
        // CAC Database đã được inject thành công!
        println("PandoraOS v0.1.0-chimera - CAC Database: ${if (::cacDao.isInitialized) "Connected ✅" else "Disconnected ❌"}")
        
        // Khởi động FlowEngineService để lắng nghe các trigger hệ thống
        startService(Intent(this, FlowEngineService::class.java))
        
        setContent {
            PandoraOSTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "PandoraOS v0.1.0-chimera",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            
                            Text(
                                text = "CAC Database: ${if (::cacDao.isInitialized) "Connected ✅" else "Disconnected ❌"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Button(onClick = {
                                checkOverlayPermissionAndStartService()
                            }) {
                                Text("Kích hoạt Pandora Orb")
                            }
                        }
                    }
                }
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
