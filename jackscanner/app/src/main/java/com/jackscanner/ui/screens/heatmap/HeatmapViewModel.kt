package com.jackscanner.ui.screens.heatmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.domain.model.CommunityDetection
import com.jackscanner.domain.model.TimeRange
import com.jackscanner.domain.repository.CommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HeatmapUiState(
    val communityDetections: List<CommunityDetection> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.TWENTY_FOUR_HOURS,
    val isLoading: Boolean = false,
    val showFilters: Boolean = false,
    val selectedRegion: CommunityDetection? = null
)

@HiltViewModel
class HeatmapViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HeatmapUiState())
    val uiState: StateFlow<HeatmapUiState> = _uiState.asStateFlow()
    
    init {
        loadCommunityDetections()
    }
    
    private fun loadCommunityDetections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            communityRepository.getCommunityDetectionsByTimeRange(
                _uiState.value.selectedTimeRange.name
            ).collect { detections ->
                _uiState.update { 
                    it.copy(communityDetections = detections, isLoading = false)
                }
            }
        }
    }
    
    fun setTimeRange(timeRange: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = timeRange) }
        loadCommunityDetections()
    }
    
    fun toggleFilters() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }
    
    fun selectRegion(detection: CommunityDetection?) {
        _uiState.update { it.copy(selectedRegion = detection) }
    }
}