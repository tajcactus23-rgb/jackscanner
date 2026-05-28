package com.jackscanner.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.jackscanner.BlueMeanieApp
import com.jackscanner.R
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.domain.model.ScanMode
import com.jackscanner.domain.repository.CommunityRepository
import com.jackscanner.domain.repository.DetectionRepository
import com.jackscanner.ui.MainActivity
import com.jackscanner.utils.OuiMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class BleScanService : Service() {

    @Inject
    lateinit var detectionRepository: DetectionRepository
    
    @Inject
    lateinit var communityRepository: CommunityRepository

    private var bluetoothLeScanner: android.bluetooth.le.BluetoothLeScanner? = null
    private var scanCallback: android.bluetooth.le.ScanCallback? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isScanning = false
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val detectedDevices = mutableMapOf<String, Detection>()

    companion object {
        const val ACTION_START_SCANNING = "com.jackscanner.START_SCANNING"
        const val ACTION_STOP_SCANNING = "com.jackscanner.STOP_SCANNING"
        const val ACTION_PAUSE_SCANNING = "com.jackscanner.PAUSE_SCANNING"
        const val ACTION_RESUME_SCANNING = "com.jackscanner.RESUME_SCANNING"
        
        const val NOTIFICATION_CHANNEL_ID_SCANNING = "bluemeanie_scanning"
        const val NOTIFICATION_CHANNEL_ID_ALERTS = "bluemeanie_alerts"
        const val NOTIFICATION_ID = 1
        const val ALERT_NOTIFICATION_ID = 2
        
        var isRunning = false
            private set
        
        var detectedCount = 0
            private set
        
        var status = ScannerStatus.IDLE
            private set
        
        var detectionsToday = 0
            private set
        
        var lastDetectionTime: Long = 0
            private set
    }

    inner class LocalBinder : Binder() {
        fun getService(): BleScanService = this@BleScanService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SCANNING -> startScanning()
            ACTION_STOP_SCANNING -> stopScanning()
            ACTION_PAUSE_SCANNING -> pauseScanning()
            ACTION_RESUME_SCANNING -> resumeScanning()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopScanning()
        releaseWakeLock()
        super.onDestroy()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Scanning Channel
        val scanningChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID_SCANNING,
            getString(R.string.channel_scanning_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.channel_scanning_description)
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }

        // Alerts Channel
        val alertsChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID_ALERTS,
            getString(R.string.channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.channel_alerts_description)
            enableVibration(true)
            enableLights(true)
            setShowBadge(true)
        }

        notificationManager.createNotificationChannels(listOf(scanningChannel, alertsChannel))
    }

    private fun startScanning() {
        if (isScanning) return
        
        try {
            startForeground(NOTIFICATION_ID, createServiceNotification())
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Failed to start foreground: ${e.message}")
            stopSelf()
            return
        }

        isScanning = true
        isRunning = true
        status = ScannerStatus.SCANNING
        
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null) {
            status = ScannerStatus.ERROR
            stopSelf()
            return
        }
        
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        
        if (bluetoothLeScanner == null) {
            status = ScannerStatus.ERROR
            stopSelf()
            return
        }
        
        scanCallback = object : android.bluetooth.le.ScanCallback() {
            override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
                val device = result.device
                val address = device.address ?: return
                val deviceName = device.name
                val rssi = result.rssi
                
                if (isTargetDevice(address, deviceName, rssi)) {
                    handleDetection(address, deviceName, rssi, result)
                }
            }
            
            override fun onScanFailed(errorCode: Int) {
                android.util.Log.e("BleScanService", "Scan failed with error: $errorCode")
                status = ScannerStatus.ERROR
                updateNotification()
            }
        }
        
        val scanSettings = android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        
        try {
            bluetoothLeScanner?.startScan(null, scanSettings, scanCallback)
            updateNotification()
        } catch (e: SecurityException) {
            android.util.Log.e("BleScanService", "Permission denied: ${e.message}")
            status = ScannerStatus.ERROR
            stopSelf()
        }
    }

    private fun handleDetection(address: String, deviceName: String?, rssi: Int, result: android.bluetooth.le.ScanResult) {
        serviceScope.launch {
            val now = System.currentTimeMillis()
            val displayName = deviceName ?: getString(R.string.unknown_device)
            
            android.util.Log.i("BleScanService", "Target detected: $address ($displayName) RSSI: $rssi")
            
            // Create or update detection
            val existingDetection = detectedDevices[address]
            val detection = if (existingDetection != null) {
                existingDetection.copy(
                    lastSeen = now,
                    rssi = rssi,
                    observedSignals = existingDetection.observedSignals + 1
                )
            } else {
                Detection(
                    id = UUID.randomUUID().toString(),
                    macAddress = address,
                    deviceName = displayName,
                    rssi = rssi,
                    timestamp = now,
                    firstSeen = now,
                    lastSeen = now,
                    observedSignals = 1
                )
            }
            
            detectedDevices[address] = detection
            detectedCount = detectedDevices.size
            detectionsToday++
            lastDetectionTime = now
            
            // Save to database
            detectionRepository.saveDetection(detection)
            
            // Update notification
            updateNotification()
            
            // Send alert notification
            sendAlertNotification(detection)
            triggerAlert()
        }
    }

    private fun pauseScanning() {
        isScanning = false
        status = ScannerStatus.PAUSED
        
        try {
            scanCallback?.let {
                bluetoothLeScanner?.stopScan(it)
            }
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Error pausing scan: ${e.message}")
        }
        
        updateNotification()
    }

    private fun resumeScanning() {
        startScanning()
    }

    private fun stopScanning() {
        isScanning = false
        isRunning = false
        status = ScannerStatus.IDLE
        
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
    
    private fun isTargetDevice(macAddress: String?, deviceName: String?, rssi: Int): Boolean {
        if (macAddress.isNullOrBlank()) return false
        return OuiMapper.isAxonDevice(macAddress, deviceName, rssi)
    }

    private fun sendAlertNotification(detection: Detection) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_ALERTS)
            .setContentTitle(getString(R.string.alert_title))
            .setContentText(getString(R.string.alert_message, detection.deviceName ?: "Unknown", detection.macAddress))
            .setSmallIcon(R.drawable.ic_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(alertSound)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setOnlyAlertOnce(false)
            .build()
        
        try {
            notificationManager.notify(ALERT_NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Alert notification error: ${e.message}")
        }
    }

    private fun triggerAlert() {
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
        
        val pauseIntent = Intent(this, BleScanService::class.java).apply {
            action = ACTION_PAUSE_SCANNING
        }
        val pausePendingIntent = PendingIntent.getService(
            this, 1, pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val heatmapIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate", "heatmap")
        }
        val heatmapPendingIntent = PendingIntent.getActivity(
            this, 2, heatmapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val feedIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate", "feed")
        }
        val feedPendingIntent = PendingIntent.getActivity(
            this, 3, feedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val lastDetectionStr = if (lastDetectionTime > 0) {
            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(lastDetectionTime))
        } else {
            "--:--:--"
        }
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_SCANNING)
            .setContentTitle(getString(R.string.service_title))
            .setContentText(getString(R.string.service_scanning))
            .setSmallIcon(R.drawable.ic_scan)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Detections Today: $detectionsToday\nLast Detection: $lastDetectionStr\nBluetooth: Active\nBackground Monitoring: Active"))
            .addAction(R.drawable.ic_scan, "Heatmap", heatmapPendingIntent)
            .addAction(R.drawable.ic_scan, "Feed", feedPendingIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.pause), pausePendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createServiceNotification()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            android.util.Log.e("BleScanService", "Error updating notification: ${e.message}")
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BlueMeanie::BleScanWakeLock"
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