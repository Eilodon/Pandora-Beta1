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
import android.os.IBinder
import android.util.Log

class FlowEngineService : Service() {

    private lateinit var audioManager: AudioManager

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                // Flow 2: Play Spotify khi kết nối tai nghe
                Log.d("FlowEngine", "Bluetooth connected. Triggering Spotify flow.")
                // Logic mở Spotify sẽ được thêm ở đây
                val launchIntent = packageManager.getLaunchIntentForPackage("com.spotify.music")
                launchIntent?.let { startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Đăng ký lắng nghe sự kiện kết nối Bluetooth
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED)
        registerReceiver(bluetoothReceiver, filter)
        
        Log.d("FlowEngine", "FlowEngineService started.")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
