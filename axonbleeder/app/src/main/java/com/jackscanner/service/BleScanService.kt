package com.axonbleeder.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.axonbleeder.R
import com.axonbleeder.ui.MainActivity

class BleScanService : Service() {

    private var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: android.bluetooth.le.ScanCallback? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isScanning = false
    
    // Keep track of detected devices for this session
    private val detectedDevices = mutableSetOf<String>()

    companion object {
        const val ACTION_START_SCANNING = "com.axonbleeder.START_SCANNING"
        const val ACTION_STOP_SCANNING = "com.axonbleeder.STOP_SCANNING"
        
        const val NOTIFICATION_CHANNEL_ID = "axonbleeder_channel"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        
        var isRunning = false
            private set
        
        var detectedCount = 0
            private set
            
        // Target OUI (Axon): 00:25:DF
        private const val TARGET_OUI = "00:25:DF"
        private const val TARGET_OUI_LOWER = "00:25:df"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCANNING -> startScanning()
            ACTION_STOP_SCANNING -> stopScanning()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopScanning()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun startScanning() {
        if (isScanning) return
        
        startForeground(NOTIFICATION_ID, createServiceNotification())
        
        isScanning = true
        isRunning = true
        
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            stopSelf()
            return
        }
        
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        
        if (bluetoothLeScanner == null) {
            stopSelf()
            return
        }
        
        scanCallback = object : android.bluetooth.le.ScanCallback() {
            override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
                val device = result.device
                val address = device.address ?: return
                
                // Check if this is a target device by OUI
                if (isTargetDevice(address)) {
                    if (!detectedDevices.contains(address)) {
                        detectedDevices.add(address)
                        detectedCount = detectedDevices.size
                    }
                    
                    val deviceName = device.name ?: getString(R.string.unknown_device)
                    val signalStrength = result.rssi
                    
                    android.util.Log.i("BleScanService", "Target detected: $address ($deviceName) RSSI: $signalStrength")
                    
                    // Alert!
                    sendAlertNotification(address, deviceName, signalStrength)
                    triggerAlert()
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                android.util.Log.e("BleScanService", "Scan failed with error: $errorCode")
            }
        }
        
        val scanSettings = android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
        } catch (e: SecurityException) {
            android.util.Log.e("BleScanService", "Permission denied: ${e.message}")
            stopSelf()
        }
    }

    private fun stopScanning() {
        isScanning = false
        // Keep detectedCount until manually cleared
        isRunning = false
        
        try {
            scanCallback?.let {
                bluetoothLeScanner?.stopScan(it)
            }
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Error stopping scan: ${e.message}")
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    /**
     * Check if MAC address matches target OUI (Axon: 00:25:DF)
     */
    private fun isTargetDevice(macAddress: String?): Boolean {
        if (macAddress.isNullOrBlank()) return false
        
        val normalized = macAddress.uppercase().replace("-", ":").trim()
        
        // Check first 8 characters (OUI)
        if (normalized.length >= 8) {
            val oui = normalized.substring(0, 8)
            return oui == TARGET_OUI || oui == TARGET_OUI_LOWER
        }
        
        return false
    }

    private fun sendAlertNotification(address: String, name: String, rssi: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.alert_title))
            .setContentText(getString(R.string.alert_message, name, address, rssi))
            .setSmallIcon(R.drawable.ic_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setSound(alertSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setOnlyAlertOnce(false)
            .build()
        
        notification.flags = notification.flags or Notification.FLAG_INSISTENT
        
        try {
            notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Alert notification error: ${e.message}")
        }
    }

    private fun triggerAlert() {
        // Vibrate
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            
            val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Vibration error: ${e.message}")
        }
        
        // Play sound
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone?.play()
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Sound error: ${e.message}")
        }
    }

    private fun createServiceNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val stopIntent = Intent(this, BleScanService::class.java).apply {
            action = ACTION_STOP_SCANNING
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.service_title))
            .setContentText(getString(R.string.service_scanning))
            .setSmallIcon(R.drawable.ic_scan)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_description)
            enableVibration(true)
            enableLights(true)
            
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), audioAttributes)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AxonBleeder::BleScanWakeLock"
        ).apply {
            acquire(10 * 60 * 60 * 1000L)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }
}