package com.jackscanner.ui.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.repository.DetectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class FeedUiState(
    val detections: List<Detection> = emptyList(),
    val isLoading: Boolean = false,
    val selectedFilter: DetectionFilter = DetectionFilter.ALL
)

enum class DetectionFilter {
    ALL,
    TODAY,
    WEEK,
    MONTH
}

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val detectionRepository: DetectionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    
    private var loadJob: Job? = null
    
    init {
        observeDetections()
    }
    
    private fun observeDetections() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            detectionRepository.getAllDetections().collect { detections ->
                val filtered = filterDetections(detections, _uiState.value.selectedFilter)
                _uiState.update { 
                    it.copy(detections = filtered, isLoading = false)
                }
            }
        }
    }
    
    fun setFilter(filter: DetectionFilter) {
        if (_uiState.value.selectedFilter != filter) {
            _uiState.update { it.copy(selectedFilter = filter) }
            // The collect will automatically apply the new filter
        }
    }
    
    private fun filterDetections(detections: List<Detection>, filter: DetectionFilter): List<Detection> {
        val now = System.currentTimeMillis()
        val startTime = when (filter) {
            DetectionFilter.ALL -> 0L
            DetectionFilter.TODAY -> getStartOfDay()
            DetectionFilter.WEEK -> now - (7 * 24 * 60 * 60 * 1000L)
            DetectionFilter.MONTH -> now - (30 * 24 * 60 * 60 * 1000L)
        }
        return detections.filter { it.timestamp >= startTime }
    }
    
    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}