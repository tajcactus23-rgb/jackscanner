package com.jackscanner.ui.screens.dev

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DevSettingsUiState(
    // Dev Account Settings
    val isDevAccount: Boolean = false,
    val devBadgeEnabled: Boolean = true,
    val coloredUsernamesEnabled: Boolean = true,
    val chatBoundariesEnabled: Boolean = true,
    val specialFontsEnabled: Boolean = true,
    
    // Feature Flags (control what's available for ALL users)
    val flagHeatmapEnabled: Boolean = true,
    val flagCommunityEnabled: Boolean = true,
    val flagScoreboardEnabled: Boolean = true,
    val flagLeaderboardGlobal: Boolean = false,
    val flagDetectionAlerts: Boolean = true,
    val flagLocationTracking: Boolean = false,
    val flagPremiumFeatures: Boolean = false,
)

@HiltViewModel
class DevSettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DevSettingsUiState())
    val uiState: StateFlow<DevSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.isDevAccount,
                preferencesManager.devBadgeEnabled,
                preferencesManager.coloredUsernamesEnabled,
                preferencesManager.chatBoundariesEnabled,
                preferencesManager.specialFontsEnabled,
                preferencesManager.flagHeatmapEnabled,
                preferencesManager.flagCommunityEnabled,
                preferencesManager.flagScoreboardEnabled,
                preferencesManager.flagLeaderboardGlobal,
                preferencesManager.flagDetectionAlerts,
                preferencesManager.flagLocationTracking,
                preferencesManager.flagPremiumFeatures
            ) { values ->
                DevSettingsUiState(
                    isDevAccount = values[0] as Boolean,
                    devBadgeEnabled = values[1] as Boolean,
                    coloredUsernamesEnabled = values[2] as Boolean,
                    chatBoundariesEnabled = values[3] as Boolean,
                    specialFontsEnabled = values[4] as Boolean,
                    flagHeatmapEnabled = values[5] as Boolean,
                    flagCommunityEnabled = values[6] as Boolean,
                    flagScoreboardEnabled = values[7] as Boolean,
                    flagLeaderboardGlobal = values[8] as Boolean,
                    flagDetectionAlerts = values[9] as Boolean,
                    flagLocationTracking = values[10] as Boolean,
                    flagPremiumFeatures = values[11] as Boolean
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // Dev Account Setters
    fun setDevAccount(isDev: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDevAccount(isDev)
        }
    }

    fun setDevBadgeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setDevBadgeEnabled(enabled)
        }
    }

    fun setColoredUsernamesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setColoredUsernamesEnabled(enabled)
        }
    }

    fun setChatBoundariesEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setChatBoundariesEnabled(enabled)
        }
    }

    fun setSpecialFontsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSpecialFontsEnabled(enabled)
        }
    }

    // Feature Flag Setters
    fun setFlagHeatmapEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagHeatmapEnabled(enabled)
        }
    }

    fun setFlagCommunityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagCommunityEnabled(enabled)
        }
    }

    fun setFlagScoreboardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagScoreboardEnabled(enabled)
        }
    }

    fun setFlagLeaderboardGlobal(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagLeaderboardGlobal(enabled)
        }
    }

    fun setFlagDetectionAlerts(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagDetectionAlerts(enabled)
        }
    }

    fun setFlagLocationTracking(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagLocationTracking(enabled)
        }
    }

    fun setFlagPremiumFeatures(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setFlagPremiumFeatures(enabled)
        }
    }

    fun resetAllFlags() {
        viewModelScope.launch {
            preferencesManager.setFlagHeatmapEnabled(true)
            preferencesManager.setFlagCommunityEnabled(true)
            preferencesManager.setFlagScoreboardEnabled(true)
            preferencesManager.setFlagLeaderboardGlobal(false)
            preferencesManager.setFlagDetectionAlerts(true)
            preferencesManager.setFlagLocationTracking(false)
            preferencesManager.setFlagPremiumFeatures(false)
        }
    }
}
