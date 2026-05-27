package com.jackscanner.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.repository.DetectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetectionDetailUiState(
    val detection: Detection? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetectionDetailViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetectionDetailUiState())
    val uiState: StateFlow<DetectionDetailUiState> = _uiState.asStateFlow()
    
    fun loadDetection(detectionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val detection = detectionRepository.getDetectionById(detectionId)
                _uiState.update { it.copy(detection = detection, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}