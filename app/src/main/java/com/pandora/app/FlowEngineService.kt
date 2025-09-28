// app/src/main/java/com/pandora/app/FlowEngineService.kt
package com.pandora.app

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
        
        Log.d("FlowEngine", "Enhanced FlowEngineService started with BLE and NFC support.")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
        serviceScope.cancel()
        Log.d("FlowEngine", "FlowEngineService destroyed.")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun initializeBLE() {
        serviceScope.launch {
            bleManager.isScanning.collect { isScanning ->
                if (isScanning) {
                    Log.d("FlowEngine", "BLE scanning started")
                } else {
                    Log.d("FlowEngine", "BLE scanning stopped")
                }
            }
        }
        
        serviceScope.launch {
            bleManager.connectedDevices.collect { devices ->
                Log.d("FlowEngine", "Connected BLE devices: ${devices.size}")
                devices.forEach { device ->
                    Log.d("FlowEngine", "Connected: ${device.name} (${device.address})")
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
            Log.d("FlowEngine", "Spotify launched due to Bluetooth connection")
        }
    }

    private fun handleBluetoothDisconnection() {
        // Logic xử lý khi ngắt kết nối Bluetooth
        Log.d("FlowEngine", "Bluetooth disconnected - stopping audio flows")
    }

    private fun handleNFCDataRead(data: String) {
        // Logic xử lý dữ liệu đọc từ NFC
        Log.d("FlowEngine", "Processing NFC data: $data")
        
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
