package com.pandora.app.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import com.pandora.app.permissions.PermissionUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bluetooth Low Energy Manager
 * Manages BLE connections and device discovery with energy optimization
 */
@Singleton
class BLEManager(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    
    private val _discoveredDevices = MutableStateFlow<List<BLEDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BLEDevice>> = _discoveredDevices.asStateFlow()
    
    private val _connectedDevices = MutableStateFlow<List<BLEDevice>>(emptyList())
    val connectedDevices: StateFlow<List<BLEDevice>> = _connectedDevices.asStateFlow()
    
    // FIXED: Provide optional error callback instead of crashing
    var onScanError: ((Throwable) -> Unit)? = null

    // FIXED: Add handler for duty-cycle scan management
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var scanStopRunnable: Runnable? = null
    private var scanRestartRunnable: Runnable? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = BLEDevice(
                address = result.device.address,
                name = if (context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    result.device.name ?: "Unknown Device"
                } else {
                    "Unknown Device"
                },
                rssi = result.rssi,
                isConnected = false,
                lastSeen = System.currentTimeMillis()
            )
            
            val currentDevices = _discoveredDevices.value.toMutableList()
            val existingIndex = currentDevices.indexOfFirst { it.address == device.address }
            
            if (existingIndex >= 0) {
                currentDevices[existingIndex] = device
            } else {
                currentDevices.add(device)
            }
            
            _discoveredDevices.value = currentDevices
        }
        
        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            android.util.Log.e("BLEManager", "Scan failed with error: $errorCode")
        }
    }
    
    /**
     * Check if BLE is supported and available
     */
    fun isBLESupported(): Boolean {
        return bluetoothAdapter != null && bluetoothLeScanner != null
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Check if location permission is granted (required for BLE scanning)
     */
    fun hasLocationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Start scanning for BLE devices with energy optimization
     */
    fun startScanning() {
        // FIXED: Pre-scan permission checks for Android 12+ (BLUETOOTH_SCAN/CONNECT) and legacy (<31) FINE_LOCATION
        if (!isBLESupported() || !isBluetoothEnabled()) {
            android.util.Log.w("BLEManager", "Cannot start scanning - BLE not supported or Bluetooth disabled")
            onScanError?.invoke(IllegalStateException("BLE unsupported or Bluetooth disabled"))
            return
        }

        if (!PermissionUtils.hasBlePermissions(context)) {
            android.util.Log.w("BLEManager", "Cannot start scanning - missing BLE runtime permissions")
            onScanError?.invoke(IllegalStateException("Missing BLE permissions"))
            return
        }
        
        if (_isScanning.value) {
            android.util.Log.w("BLEManager", "Already scanning")
            return
        }
        
        try {
            val scanner = bluetoothLeScanner
            if (scanner == null) {
                onScanError?.invoke(IllegalStateException("BluetoothLeScanner unavailable"))
                return
            }

            // FIXED: Set start time for energy metrics
            scanStartTime = SystemClock.elapsedRealtime()

            // FIXED: Use low-power scan settings and optional filters (empty to scan all)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(0L)
                .build()
            val filters: List<ScanFilter> = emptyList()

            scanner.startScan(filters, settings, scanCallback)
            _isScanning.value = true
            android.util.Log.d("BLEManager", "Started BLE scanning")

            // FIXED: Duty-cycle scanning: scan 10s, rest 50s, then restart
            scanStopRunnable?.let { handler.removeCallbacks(it) }
            scanRestartRunnable?.let { handler.removeCallbacks(it) }

            scanStopRunnable = Runnable {
                stopScanning()
                scanRestartRunnable = Runnable {
                    // Re-check permissions each cycle to avoid crashes
                    if (PermissionUtils.hasBlePermissions(context)) {
                        startScanning()
                    } else {
                        onScanError?.invoke(IllegalStateException("Missing BLE permissions"))
                    }
                }
                handler.postDelayed(scanRestartRunnable!!, 50_000L)
            }
            handler.postDelayed(scanStopRunnable!!, 10_000L)
        } catch (e: SecurityException) {
            android.util.Log.e("BLEManager", "Security exception while starting scan", e)
            onScanError?.invoke(e)
        }
    }
    
    /**
     * Stop scanning for BLE devices
     */
    fun stopScanning() {
        if (!_isScanning.value) {
            return
        }
        
        try {
            bluetoothLeScanner?.stopScan(scanCallback)
            _isScanning.value = false
            android.util.Log.d("BLEManager", "Stopped BLE scanning")
            // FIXED: Clear duty-cycle callbacks to avoid leaks
            scanStopRunnable?.let { handler.removeCallbacks(it) }
            scanRestartRunnable?.let { handler.removeCallbacks(it) }
            scanStopRunnable = null
            scanRestartRunnable = null
        } catch (e: SecurityException) {
            android.util.Log.e("BLEManager", "Security exception while stopping scan", e)
            onScanError?.invoke(e)
        }
    }
    
    /**
     * Connect to a BLE device
     */
    fun connectToDevice(device: BLEDevice) {
        // Implementation for connecting to BLE device
        // This would involve establishing a GATT connection
        android.util.Log.d("BLEManager", "Connecting to device: ${device.name}")
        
        val connectedDevice = device.copy(isConnected = true)
        val currentConnected = _connectedDevices.value.toMutableList()
        currentConnected.add(connectedDevice)
        _connectedDevices.value = currentConnected
    }
    
    /**
     * Disconnect from a BLE device
     */
    fun disconnectFromDevice(device: BLEDevice) {
        android.util.Log.d("BLEManager", "Disconnecting from device: ${device.name}")
        
        val currentConnected = _connectedDevices.value.toMutableList()
        currentConnected.removeAll { it.address == device.address }
        _connectedDevices.value = currentConnected
    }
    
    /**
     * Clear discovered devices list
     */
    fun clearDiscoveredDevices() {
        _discoveredDevices.value = emptyList()
    }
    
    /**
     * Get energy usage statistics
     */
    fun getEnergyUsage(): BLEEnergyUsage {
        val scanTime = if (scanStartTime > 0L) {
            // FIXED: Use elapsedRealtime to compute active scan time window
            val now = SystemClock.elapsedRealtime()
            (now - scanStartTime).coerceAtLeast(0L)
        } else 0L
        
        return BLEEnergyUsage(
            scanTimeMs = scanTime,
            devicesDiscovered = _discoveredDevices.value.size,
            devicesConnected = _connectedDevices.value.size,
            estimatedBatteryUsage = calculateBatteryUsage(scanTime)
        )
    }
    
    private var scanStartTime = 0L // FIXED: updated when starting scan
    
    private fun calculateBatteryUsage(scanTimeMs: Long): Float {
        // Rough estimation: BLE scanning uses ~1-2% battery per hour
        val hours = scanTimeMs.toFloat() / (1000f * 60 * 60)
        return (hours * 1.5f).coerceAtMost(10f) // Cap at 10%
    }
    
    init {
        if (isBLESupported()) {
            android.util.Log.d("BLEManager", "BLE Manager initialized successfully")
        } else {
            android.util.Log.w("BLEManager", "BLE not supported on this device")
        }
    }
}

/**
 * Data class representing a BLE device
 */
data class BLEDevice(
    val address: String,
    val name: String,
    val rssi: Int,
    val isConnected: Boolean,
    val lastSeen: Long
)

/**
 * Data class for BLE energy usage statistics
 */
data class BLEEnergyUsage(
    val scanTimeMs: Long,
    val devicesDiscovered: Int,
    val devicesConnected: Int,
    val estimatedBatteryUsage: Float
)
