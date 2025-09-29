// app/src/main/java/com/pandora/app/FlowEngineService.kt
package com.pandora.app

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.media.AudioManager
import android.nfc.NfcAdapter
import android.os.IBinder
import android.util.Log
import com.pandora.app.bluetooth.BLEManager
import com.pandora.app.nfc.NFCManager
import com.pandora.app.permissions.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FlowEngineService : Service() {

    @Inject
    lateinit var bleManager: BLEManager
    
    @Inject
    lateinit var nfcManager: NFCManager
    
    @Inject
    lateinit var permissionManager: PermissionManager

    private lateinit var audioManager: AudioManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    // Flow 2: Play Spotify khi kết nối tai nghe
                    Log.d("FlowEngine", "Bluetooth connected. Triggering Spotify flow.")
                    handleBluetoothConnection()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.d("FlowEngine", "Bluetooth disconnected.")
                    handleBluetoothDisconnection()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // FIXED: foreground service to keep long-running listeners alive
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flow Engine active")
            .setContentText("Listening for BLE/NFC events")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
        startForeground(NOTI_ID, notification)
        
        // Đăng ký lắng nghe sự kiện kết nối Bluetooth
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        registerReceiver(bluetoothReceiver, filter)
        
        // Khởi tạo BLE scanning
        initializeBLE()
        
        // Khởi tạo NFC monitoring
        initializeNFC()
        
        // FIXED: use Timber
        timber.log.Timber.d("Enhanced FlowEngineService started with BLE and NFC support.")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        serviceScope.cancel()
        timber.log.Timber.d("FlowEngineService destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeBLE() {
        serviceScope.launch {
            bleManager.isScanning.collect { isScanning ->
                if (isScanning) {
                    timber.log.Timber.d("BLE scanning started")
                } else {
                    timber.log.Timber.d("BLE scanning stopped")
                }
            }
        }
        
        serviceScope.launch {
            bleManager.connectedDevices.collect { devices ->
                timber.log.Timber.d("Connected BLE devices: %s", devices.size)
                devices.forEach { device ->
                    timber.log.Timber.d("Connected: %s (%s)", device.name, device.address)
                }
            }
        }
        
        // Bắt đầu quét BLE devices
        if (bleManager.isBLESupported() && bleManager.isBluetoothEnabled()) {
            bleManager.startScanning()
        }
    }

    private fun initializeNFC() {
        serviceScope.launch {
            nfcManager.isNFCAvailable.collect { isAvailable ->
                if (isAvailable) {
                    Log.d("FlowEngine", "NFC is available and enabled")
                } else {
                    Log.d("FlowEngine", "NFC is not available or disabled")
                }
            }
        }
        
        serviceScope.launch {
            nfcManager.lastReadData.collect { data ->
                data?.let {
                    Log.d("FlowEngine", "NFC data read: $it")
                    handleNFCDataRead(it)
                }
            }
        }
    }

    private fun handleBluetoothConnection() {
        // Logic mở Spotify khi kết nối tai nghe
        val launchIntent = packageManager.getLaunchIntentForPackage("com.spotify.music")
        launchIntent?.let { 
            startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            timber.log.Timber.d("Spotify launched due to Bluetooth connection")
        }
    }

    private fun handleBluetoothDisconnection() {
        // Logic xử lý khi ngắt kết nối Bluetooth
        timber.log.Timber.d("Bluetooth disconnected - stopping audio flows")
    }

    private fun handleNFCDataRead(data: String) {
        // Logic xử lý dữ liệu đọc từ NFC
        // Avoid logging plaintext NFC data in release
        if (BuildConfig.DEBUG) timber.log.Timber.d("Processing NFC data: %s", data)
        
        // Có thể thêm logic để xử lý các lệnh từ NFC tag
        when {
            data.contains("spotify") -> {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.spotify.music")
                launchIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
            data.contains("calendar") -> {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                launchIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
            data.contains("maps") -> {
                val launchIntent = packageManager.getLaunchIntentForPackage("com.google.android.apps.maps")
                launchIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }
    }

    /**
     * Get service statistics
     */
    fun getServiceStatistics(): ServiceStatistics {
        val bleEnergyUsage = bleManager.getEnergyUsage()
        val nfcStats = nfcManager.getNFCStatistics()
        val permissionStats = permissionManager.getPermissionAnalytics()
        
        return ServiceStatistics(
            isRunning = true,
            bleDevicesConnected = bleManager.connectedDevices.value.size,
            bleDevicesDiscovered = bleManager.discoveredDevices.value.size,
            nfcAvailable = nfcStats.isAvailable,
            nfcReads = nfcStats.totalReads,
            nfcWrites = nfcStats.totalWrites,
            permissionsGranted = permissionStats.grantedPermissions,
            totalPermissions = permissionStats.totalPermissions,
            estimatedBatteryUsage = bleEnergyUsage.estimatedBatteryUsage
        )
    }

    // FIXED: create notification channel for foreground service
    private fun createNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Flow Engine",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        // FIXED: constants for foreground service
        const val CHANNEL_ID = "flow_engine"
        const val NOTI_ID = 100
    }
}

/**
 * Data class for service statistics
 */
data class ServiceStatistics(
    val isRunning: Boolean,
    val bleDevicesConnected: Int,
    val bleDevicesDiscovered: Int,
    val nfcAvailable: Boolean,
    val nfcReads: Int,
    val nfcWrites: Int,
    val permissionsGranted: Int,
    val totalPermissions: Int,
    val estimatedBatteryUsage: Float
)
