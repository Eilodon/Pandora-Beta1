package com.pandora.app.permissions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.MaterialTheme
import com.pandora.core.ui.theme.PandoraOSTheme

// FIXED: Activity tích hợp
class PermissionActivity : ComponentActivity() {
    private val vm: PermissionIntroViewModel by lazy { PermissionIntroViewModel(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.refresh()
        setContent {
            PandoraOSTheme(isDarkTheme = true) {
                val state by vm.state.collectAsState()
                PermissionIntroScreen(
                    state = state,
                    onGrant = { vm.onGrantClicked(this) },
                    onLearnMore = { openGuide() }
                )
            }
        }
    }

    private fun openGuide() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Eilodon/Pandora-Beta1/blob/main/USER_GUIDE.md"))
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        vm.refresh()
        if (vm.state.value.granted) finish()
    }
}
