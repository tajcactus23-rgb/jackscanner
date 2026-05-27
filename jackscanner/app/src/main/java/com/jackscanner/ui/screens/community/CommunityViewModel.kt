package com.jackscanner.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.data.preferences.PreferencesManager
import com.jackscanner.domain.model.UserProfile
import com.jackscanner.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CommunityUiState(
    val userProfile: UserProfile? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val showProfileCard: Boolean = false,
    // Dev features
    val isDevAccount: Boolean = false,
    val devBadgeEnabled: Boolean = true,
    val coloredUsernamesEnabled: Boolean = true,
    val chatBoundariesEnabled: Boolean = true,
    val specialFontsEnabled: Boolean = true,
)

data class ChatMessage(
    val id: String,
    val username: String,
    val message: String,
    val timestamp: Long,
    val isAnonymous: Boolean,
    val reactions: Map<String, Int> = emptyMap(),
    val isDevMessage: Boolean = false
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadMessages()
        observeDevSettings()
    }

    private fun observeDevSettings() {
        viewModelScope.launch {
            combine(
                preferencesManager.isDevAccount,
                preferencesManager.devBadgeEnabled,
                preferencesManager.coloredUsernamesEnabled,
                preferencesManager.chatBoundariesEnabled,
                preferencesManager.specialFontsEnabled
            ) { isDev, badge, colored, boundaries, fonts ->
                _uiState.update {
                    it.copy(
                        isDevAccount = isDev,
                        devBadgeEnabled = badge,
                        coloredUsernamesEnabled = colored,
                        chatBoundariesEnabled = boundaries,
                        specialFontsEnabled = fonts
                    )
                }
            }.collect()
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile ?: createDefaultProfile()) }
            }
        }
    }

    private fun loadMessages() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    private fun createDefaultProfile(): UserProfile {
        return UserProfile(
            username = "Anonymous",
            detectionCount = 0,
            messages = 0,
            rank = com.jackscanner.domain.model.UserRank.OBSERVER,
            reputationScore = 0,
            isAnonymous = true
        )
    }

    fun toggleProfileCard() {
        _uiState.update { it.copy(showProfileCard = !it.showProfileCard) }
    }

    fun sendMessage(message: String) {
        val state = _uiState.value
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            username = state.userProfile?.username ?: "Anonymous",
            message = message,
            timestamp = System.currentTimeMillis(),
            isAnonymous = state.userProfile?.isAnonymous ?: true,
            isDevMessage = state.isDevAccount
        )

        _uiState.update { currentState ->
            currentState.copy(messages = currentState.messages + newMessage)
        }
    }

    fun addReaction(messageId: String, reaction: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId) {
                    val reactions = msg.reactions.toMutableMap()
                    reactions[reaction] = (reactions[reaction] ?: 0) + 1
                    msg.copy(reactions = reactions)
                } else msg
            }
            state.copy(messages = updatedMessages)
        }
    }
}
