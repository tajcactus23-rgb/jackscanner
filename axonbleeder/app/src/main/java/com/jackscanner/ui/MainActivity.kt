package com.axonbleeder.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.axonbleeder.R
import com.axonbleeder.databinding.ActivityMainBinding
import com.axonbleeder.service.BleScanService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isScanning = false

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
        checkBluetoothState()
    }

    private fun setupUI() {
        binding.btnScan.setOnClickListener {
            if (isScanning) {
                stopScanning()
            } else {
                checkBluetoothState()
            }
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.fromParts("package", packageName, null)
            })
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
                btnScan.text = getString(R.string.stop_scan)
                statusText.text = getString(R.string.scanning_status)
                statusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_active))
                deviceCount.text = getString(R.string.devices_found, BleScanService.detectedCount)
            } else {
                btnScan.text = getString(R.string.start_scan)
                statusText.text = getString(R.string.ready_status)
                statusText.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.status_idle))
                deviceCount.text = getString(R.string.devices_found, 0)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Keep scanning running in background
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Don't stop service - let it run in background
    }
}