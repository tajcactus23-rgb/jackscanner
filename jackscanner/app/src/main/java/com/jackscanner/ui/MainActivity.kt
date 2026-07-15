package com.jackscanner.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jackscanner.R
import com.jackscanner.databinding.ActivityMainBinding
import com.jackscanner.service.BleScanService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isScanning = false
    
    // Radar detection receiver
    private val radarReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BleScanService.ACTION_DEVICE_DETECTED -> {
                    val mac = intent.getStringExtra(BleScanService.EXTRA_MAC) ?: return
                    val name = intent.getStringExtra(BleScanService.EXTRA_NAME) ?: "UNKNOWN"
                    val type = intent.getStringExtra(BleScanService.EXTRA_TYPE) ?: "DEVICE"
                    binding.asteroidsRadar.addDetection(mac, name, type)
                }
                BleScanService.ACTION_SCAN_STARTED -> {
                    binding.asteroidsRadar.startScanning()
                }
                BleScanService.ACTION_SCAN_STOPPED -> {
                    binding.asteroidsRadar.stopScanning()
                }
            }
        }
    }

    private val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startScanning()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
            updateUI()
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (isBluetoothEnabled()) {
            checkPermissionsAndScan()
        } else {
            Toast.makeText(this, getString(R.string.bluetooth_required), Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        updateUI()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        checkBluetoothState()
    }
    
    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }
    
    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(BleScanService.ACTION_DEVICE_DETECTED)
            addAction(BleScanService.ACTION_SCAN_STARTED)
            addAction(BleScanService.ACTION_SCAN_STOPPED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(radarReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(radarReceiver, filter)
        }
    }
    
    private fun unregisterReceiver() {
        try {
            unregisterReceiver(radarReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    private fun setupUI() {
        binding.scanButton.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                checkBluetoothState()
            }
        }
        
        // Check if service is running
        isScanning = BleScanService.isRunning
        updateUI()
    }

    private fun checkBluetoothState() {
        if (!isBluetoothEnabled()) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableIntent)
        } else {
            checkPermissionsAndScan()
        }
    }

    private fun checkPermissionsAndScan() {
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            startScanning()
        }
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun startScanning() {
        val intent = Intent(this, BleScanService::class.java).apply {
            action = BleScanService.ACTION_START_SCANNING
        }
        startForegroundService(intent)
        isScanning = true
        updateUI()
    }

    private fun stopScanning() {
        val intent = Intent(this, BleScanService::class.java).apply {
            action = BleScanService.ACTION_STOP_SCANNING
        }
        startService(intent)
        isScanning = false
        updateUI()
    }

    private fun updateUI() {
        binding.apply {
            if (isScanning || BleScanService.isRunning) {
                scanButton.text = getString(R.string.stop_scan)
                statusBadge.text = "● SCANNING"
                statusBadge.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_active))
                targetStatus.text = getString(R.string.scanning_status)
                deviceCount.text = BleScanService.detectedCount.toString()
            } else {
                scanButton.text = getString(R.string.start_scan)
                statusBadge.text = "● IDLE"
                statusBadge.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_danger))
                targetStatus.text = "IDLE"
                deviceCount.text = "0"
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't stop service - let it run in background
    }
}