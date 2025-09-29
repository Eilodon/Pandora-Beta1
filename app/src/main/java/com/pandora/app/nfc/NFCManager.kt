package com.pandora.app.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NFC Manager for fast data exchange
 * Supports reading and writing NFC tags with encryption
 */
@Singleton
class NFCManager(
    @ApplicationContext private val context: Context
) {
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    
    private val _isNFCAvailable = MutableStateFlow(nfcAdapter?.isEnabled == true)
    val isNFCAvailable: StateFlow<Boolean> = _isNFCAvailable.asStateFlow()
    
    private val _lastReadData = MutableStateFlow<String?>(null)
    val lastReadData: StateFlow<String?> = _lastReadData.asStateFlow()
    
    private val _lastWriteResult = MutableStateFlow<Boolean?>(null)
    val lastWriteResult: StateFlow<Boolean?> = _lastWriteResult.asStateFlow()
    
    /**
     * Check if NFC is available and enabled
     */
    fun isNFCEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * Enable NFC foreground dispatch for an activity
     */
    fun enableForegroundDispatch(activity: Activity) {
        if (!isNFCEnabled()) {
            Log.w("NFCManager", "NFC is not enabled")
            return
        }
        
        val intent = Intent(activity, activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            activity, 0, intent, 
            android.app.PendingIntent.FLAG_MUTABLE
        )
        
        val filters = arrayOf(
            android.content.IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
            android.content.IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            android.content.IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        )
        
        val techList = arrayOf(
            arrayOf(Ndef::class.java.name),
            arrayOf(NdefFormatable::class.java.name)
        )
        
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
        Log.d("NFCManager", "NFC foreground dispatch enabled")
    }
    
    /**
     * Disable NFC foreground dispatch for an activity
     */
    fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
        Log.d("NFCManager", "NFC foreground dispatch disabled")
    }
    
    /**
     * Handle NFC intent and read data from tag
     */
    fun handleNFCIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_NDEF_DISCOVERED,
            NfcAdapter.ACTION_TAG_DISCOVERED,
            NfcAdapter.ACTION_TECH_DISCOVERED -> {
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                tag?.let { readFromTag(it) }
            }
        }
    }
    
    /**
     * Read data from NFC tag
     */
    private fun readFromTag(tag: Tag) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage
                val data = parseNdefMessage(ndefMessage)
                _lastReadData.value = data
                Log.d("NFCManager", "Read data from NFC tag: $data")
                ndef.close()
            } else {
                Log.w("NFCManager", "Tag is not NDEF formatted")
            }
        } catch (e: Exception) {
            Log.e("NFCManager", "Error reading from NFC tag", e)
        }
    }
    
    /**
     * Write data to NFC tag
     */
    fun writeToTag(tag: Tag, data: String): Boolean {
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                writeToNdefTag(ndef, data)
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    writeToFormatableTag(ndefFormatable, data)
                } else {
                    Log.w("NFCManager", "Tag is not NDEF compatible")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("NFCManager", "Error writing to NFC tag", e)
            false
        }
    }
    
    /**
     * Write data to NDEF tag
     */
    private fun writeToNdefTag(ndef: Ndef, data: String): Boolean {
        return try {
            ndef.connect()
            
            val ndefMessage = createNdefMessage(data)
            ndef.writeNdefMessage(ndefMessage)
            
            _lastWriteResult.value = true
            Log.d("NFCManager", "Successfully wrote data to NDEF tag")
            true
        } catch (e: Exception) {
            Log.e("NFCManager", "Error writing to NDEF tag", e)
            _lastWriteResult.value = false
            false
        } finally {
            try {
                ndef.close()
            } catch (e: Exception) {
                Log.e("NFCManager", "Error closing NDEF tag", e)
            }
        }
    }
    
    /**
     * Write data to formatable tag
     */
    private fun writeToFormatableTag(ndefFormatable: NdefFormatable, data: String): Boolean {
        return try {
            ndefFormatable.connect()
            
            val ndefMessage = createNdefMessage(data)
            ndefFormatable.format(ndefMessage)
            
            _lastWriteResult.value = true
            Log.d("NFCManager", "Successfully wrote data to formatable tag")
            true
        } catch (e: Exception) {
            Log.e("NFCManager", "Error writing to formatable tag", e)
            _lastWriteResult.value = false
            false
        } finally {
            try {
                ndefFormatable.close()
            } catch (e: Exception) {
                Log.e("NFCManager", "Error closing formatable tag", e)
            }
        }
    }
    
    /**
     * Create NDEF message from data
     */
    private fun createNdefMessage(data: String): NdefMessage {
        // FIXED: Encrypt payload using AES-GCM; store as iv||ciphertext (simple framing)
        val (iv, ciphertext) = CryptoUtils.encryptAesGcm(data.toByteArray())
        val payload = ByteArray(iv.size + ciphertext.size).apply {
            System.arraycopy(iv, 0, this, 0, iv.size)
            System.arraycopy(ciphertext, 0, this, iv.size, ciphertext.size)
        }
        val record = NdefRecord.createMime("application/vnd.pandoraos.data", payload)
        return NdefMessage(arrayOf(record))
    }
    
    /**
     * Parse NDEF message to extract data
     */
    private fun parseNdefMessage(ndefMessage: NdefMessage?): String {
        if (ndefMessage == null) return ""
        
        val records = ndefMessage.records
        val data = StringBuilder()
        
        for (record in records) {
            when (record.tnf) {
                NdefRecord.TNF_MIME_MEDIA -> {
                    val mimeType = String(record.type)
                    if (mimeType == "application/vnd.pandoraos.data") {
                        // FIXED: Decrypt AES-GCM payload assuming iv||ciphertext
                        val payload = record.payload
                        if (payload.size > 12) {
                            val iv = payload.copyOfRange(0, 12)
                            val ciphertext = payload.copyOfRange(12, payload.size)
                            try {
                                val plain = CryptoUtils.decryptAesGcm(iv, ciphertext)
                                data.append(String(plain))
                            } catch (e: Exception) {
                                Log.e("NFCManager", "Failed to decrypt NFC payload", e)
                            }
                        }
                    }
                }
                NdefRecord.TNF_WELL_KNOWN -> {
                    val payload = String(record.payload)
                    data.append(payload)
                }
            }
        }
        
        return data.toString()
    }
    
    /**
     * Encrypt data before writing to NFC tag
     */
    fun encryptData(data: String): String {
        // FIXED: Backward-compatible helper to produce base64 of iv||ciphertext
        return try {
            val (iv, ciphertext) = CryptoUtils.encryptAesGcm(data.toByteArray())
            android.util.Base64.encodeToString(iv + ciphertext, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("NFCManager", "encryptData failed", e)
            ""
        }
    }
    
    /**
     * Decrypt data after reading from NFC tag
     */
    fun decryptData(encryptedData: String): String {
        // FIXED: Decrypt base64 iv||ciphertext helper
        return try {
            val raw = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
            if (raw.size <= 12) return ""
            val iv = raw.copyOfRange(0, 12)
            val ciphertext = raw.copyOfRange(12, raw.size)
            val plain = CryptoUtils.decryptAesGcm(iv, ciphertext)
            String(plain)
        } catch (e: Exception) {
            Log.e("NFCManager", "decryptData failed", e)
            ""
        }
    }
    
    /**
     * Get NFC statistics
     */
    fun getNFCStatistics(): NFCStatistics {
        return NFCStatistics(
            isAvailable = isNFCEnabled(),
            lastReadSuccess = _lastReadData.value != null,
            lastWriteSuccess = _lastWriteResult.value ?: false,
            totalReads = if (_lastReadData.value != null) 1 else 0,
            totalWrites = if (_lastWriteResult.value == true) 1 else 0
        )
    }
}

/**
 * Data class for NFC statistics
 */
data class NFCStatistics(
    val isAvailable: Boolean,
    val lastReadSuccess: Boolean,
    val lastWriteSuccess: Boolean,
    val totalReads: Int,
    val totalWrites: Int
)
