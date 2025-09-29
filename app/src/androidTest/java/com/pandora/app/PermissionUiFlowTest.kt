package com.pandora.app

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Test
import org.junit.runner.RunWith

// FIXED: UiAutomator test for BLE permission flow
@RunWith(AndroidJUnit4::class)
class PermissionUiFlowTest {
    @Test
    fun grantBlePermissionsFlow() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, com.pandora.app.permissions.PermissionActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)

        // Wait for our intro screen
        device.wait(Until.hasObject(By.text("Cho phép")), 5000)
        device.findObject(By.text("Cho phép")).click()

        // Handle system dialog (OEM text may differ)
        device.wait(Until.hasObject(By.textContains("Cho phép")), 5000)
        device.findObject(By.textContains("Cho phép")).click()

        // Validate that intro screen is gone (button disappears)
        device.wait(Until.gone(By.text("Cho phép")), 5000)
    }
}
