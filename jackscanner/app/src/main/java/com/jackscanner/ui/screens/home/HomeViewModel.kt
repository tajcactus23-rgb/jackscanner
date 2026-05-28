package com.jackscanner.ui.screens.home

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.domain.repository.DetectionRepository
import com.jackscanner.service.BleScanService
import com.jackscanner.service.ScanController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isScanning: Boolean = false,
    val scannerStatus: ScannerStatus = ScannerStatus.IDLE,
    val detectionsToday: Int = 0,
    val lastDetection: Detection? = null,
    val recentDetections: List<Detection> = emptyList(),
    val detectedCount: Int = 0,
    val bluetoothActive: Boolean = false,
    val communityActivity: Int = 0,
    val needsBluetoothEnable: Boolean = false,
    val needsPermissions: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository,
    private val preferencesManager: PreferencesManager,
    private val scanController: ScanController,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    private var bluetoothCheckCallback: (() -> Unit)? = null
    private var permissionsCheckCallback: (() -> Unit)? = null
    
    init {
        loadData()
        observeScannerStatus()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            detectionRepository.getRecentDetections(10).collect { detections ->
                _uiState.update { state ->
                    state.copy(
                        recentDetections = detections,
                        lastDetection = detections.firstOrNull()
                    )
                }
            }
        }
        
        viewModelScope.launch {
            try {
                val count = detectionRepository.getDetectionCountToday()
                _uiState.update { it.copy(detectionsToday = count) }
            } catch (e: Exception) {
                _uiState.update { it.copy(detectionsToday = 0) }
            }
        }
    }
    
    private fun observeScannerStatus() {
        viewModelScope.launch {
            while (true) {
                val isRunning = BleScanService.isRunning
                val count = BleScanService.detectedCount
                _uiState.update { state ->
                    state.copy(
                        isScanning = isRunning,
                        scannerStatus = if (isRunning) ScannerStatus.SCANNING else ScannerStatus.IDLE,
                        detectedCount = count
                    )
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    fun setCallbacks(onBluetoothCheck: () -> Unit, onPermissionsCheck: () -> Unit) {
        bluetoothCheckCallback = onBluetoothCheck
        permissionsCheckCallback = onPermissionsCheck
    }
    
    fun toggleScanning() {
        if (BleScanService.isRunning) {
            scanController.stopScanning()
            return
        }
        
        // Check Bluetooth is enabled
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _uiState.update { it.copy(needsBluetoothEnable = true) }
            bluetoothCheckCallback?.invoke()
            return
        }
        
        // Check permissions
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        
        val missingPermissions = requiredPermissions.filter {
            context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            _uiState.update { it.copy(needsPermissions = true) }
            permissionsCheckCallback?.invoke()
            return
        }
        
        // All checks passed - start scanning
        scanController.startScanning(checkBluetooth = false)
    }
    
    fun onBluetoothEnabled() {
        _uiState.update { it.copy(needsBluetoothEnable = false) }
        toggleScanning()
    }
    
    fun onPermissionsGranted() {
        _uiState.update { it.copy(needsPermissions = false) }
        toggleScanning()
    }
    
    fun refreshData() {
        loadData()
    }
}