package com.jackscanner.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.domain.model.AppTheme
import com.jackscanner.domain.model.ScannerSettings
import com.jackscanner.domain.model.ScanMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val scannerSettings: ScannerSettings = ScannerSettings(),
    val selectedTheme: AppTheme = AppTheme.BLUE_MEANIE_CLASSIC,
    val notificationsEnabled: Boolean = true,
    val communityNotifications: Boolean = true,
    val privateMode: Boolean = true,
    val locationSharing: Boolean = false,
    val isPremium: Boolean = false,
    val userName: String = "",
    val userAvatar: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.scannerSettings.collect { settings ->
                _uiState.update { it.copy(scannerSettings = settings) }
            }
        }

        viewModelScope.launch {
            preferencesManager.selectedTheme.collect { theme ->
                _uiState.update { it.copy(selectedTheme = theme) }
            }
        }

        viewModelScope.launch {
            preferencesManager.notificationsEnabled.collect { enabled ->
                _uiState.update { it.copy(notificationsEnabled = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesManager.communityNotifications.collect { enabled ->
                _uiState.update { it.copy(communityNotifications = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesManager.privateMode.collect { enabled ->
                _uiState.update { it.copy(privateMode = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesManager.locationSharing.collect { enabled ->
                _uiState.update { it.copy(locationSharing = enabled) }
            }
        }

        viewModelScope.launch {
            preferencesManager.isPremium.collect { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }

        viewModelScope.launch {
            preferencesManager.userName.collect { name ->
                _uiState.update { it.copy(userName = name) }
            }
        }

        viewModelScope.launch {
            preferencesManager.userAvatar.collect { avatar ->
                _uiState.update { it.copy(userAvatar = avatar) }
            }
        }
    }

    fun updateScannerSettings(settings: ScannerSettings) {
        viewModelScope.launch {
            preferencesManager.updateScannerSettings(settings)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesManager.setTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun setCommunityNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setCommunityNotifications(enabled)
        }
    }

    fun setPrivateMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setPrivateMode(enabled)
        }
    }

    fun setLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setLocationSharing(enabled)
        }
    }

    fun toggleAutoStart() {
        val current = _uiState.value.scannerSettings.autoStartOnBoot
        updateScannerSettings(_uiState.value.scannerSettings.copy(autoStartOnBoot = !current))
    }

    fun toggleAlertSound() {
        val current = _uiState.value.scannerSettings.alertSound
        updateScannerSettings(_uiState.value.scannerSettings.copy(alertSound = !current))
    }

    fun toggleAlertVibration() {
        val current = _uiState.value.scannerSettings.alertVibration
        updateScannerSettings(_uiState.value.scannerSettings.copy(alertVibration = !current))
    }

    fun setScanMode(mode: ScanMode) {
        updateScannerSettings(_uiState.value.scannerSettings.copy(scanMode = mode))
    }

    fun logout() {
        viewModelScope.launch {
            preferencesManager.clearUserData()
        }
    }
}
