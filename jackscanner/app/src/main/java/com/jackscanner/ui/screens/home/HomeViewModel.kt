package com.jackscanner.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.domain.repository.DetectionRepository
import com.jackscanner.service.BleScanService
import com.jackscanner.service.ScanController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    val communityActivity: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository,
    private val preferencesManager: PreferencesManager,
    private val scanController: ScanController
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
        observeScannerStatus()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            // Observe recent detections
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
                // Handle error gracefully
                _uiState.update { it.copy(detectionsToday = 0) }
            }
        }
    }
    
    private fun observeScannerStatus() {
        viewModelScope.launch {
            // Poll scanner status
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
    
    fun toggleScanning() {
        if (BleScanService.isRunning) {
            scanController.stopScanning()
        } else {
            scanController.startScanning()
        }
    }
    
    fun refreshData() {
        loadData()
    }
}