package com.jackscanner.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentStep: Int = 0,
    val totalSteps: Int = 7,
    val username: String = "",
    val isAnonymous: Boolean = false,
    val autoRotateUsername: Boolean = false,
    val optionalLocation: Boolean = false,
    val permissionsGranted: PermissionsState = PermissionsState()
)

data class PermissionsState(
    val bluetooth: Boolean = false,
    val notifications: Boolean = false,
    val location: Boolean = false,
    val backgroundLocation: Boolean = false
)

enum class OnboardingStep {
    WELCOME,
    BLUETOOTH,
    NOTIFICATIONS,
    LOCATION,
    BACKGROUND_LOCATION,
    USERNAME,
    COMPLETE
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()
    
    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < _uiState.value.totalSteps - 1) {
            _uiState.update { it.copy(currentStep = current + 1) }
        }
    }
    
    fun previousStep() {
        val current = _uiState.value.currentStep
        if (current > 0) {
            _uiState.update { it.copy(currentStep = current - 1) }
        }
    }
    
    fun setUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }
    
    fun setAnonymous(isAnonymous: Boolean) {
        _uiState.update { it.copy(isAnonymous = isAnonymous) }
    }
    
    fun setAutoRotateUsername(autoRotate: Boolean) {
        _uiState.update { it.copy(autoRotateUsername = autoRotate) }
    }
    
    fun setOptionalLocation(enabled: Boolean) {
        _uiState.update { it.copy(optionalLocation = enabled) }
    }
    
    fun grantBluetooth() {
        _uiState.update { 
            it.copy(permissionsGranted = it.permissionsGranted.copy(bluetooth = true))
        }
    }
    
    fun grantNotifications() {
        _uiState.update { 
            it.copy(permissionsGranted = it.permissionsGranted.copy(notifications = true))
        }
    }
    
    fun grantLocation() {
        _uiState.update { 
            it.copy(permissionsGranted = it.permissionsGranted.copy(location = true))
        }
    }
    
    fun grantBackgroundLocation() {
        _uiState.update { 
            it.copy(permissionsGranted = it.permissionsGranted.copy(backgroundLocation = true))
        }
    }
    
    fun onPermissionResult(permission: String, granted: Boolean) {
        _uiState.update { state ->
            val newPermissions = when (permission) {
                "bluetooth" -> state.permissionsGranted.copy(bluetooth = granted)
                "notifications" -> state.permissionsGranted.copy(notifications = granted)
                "location" -> state.permissionsGranted.copy(location = granted)
                "background_location" -> state.permissionsGranted.copy(backgroundLocation = granted)
                else -> state.permissionsGranted
            }
            state.copy(permissionsGranted = newPermissions)
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesManager.setUserProfile(
                username = _uiState.value.username.ifBlank { "Anonymous" },
                isAnonymous = _uiState.value.isAnonymous,
                autoRotate = _uiState.value.autoRotateUsername,
                location = _uiState.value.optionalLocation
            )
            preferencesManager.setOnboardingCompleted(true)
        }
    }
}