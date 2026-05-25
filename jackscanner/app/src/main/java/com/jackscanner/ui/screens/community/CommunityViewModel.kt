package com.jackscanner.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val showProfileCard: Boolean = false
)

data class ChatMessage(
    val id: String,
    val username: String,
    val message: String,
    val timestamp: Long,
    val isAnonymous: Boolean,
    val reactions: Map<String, Int> = emptyMap()
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
        loadMessages()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile ?: createDefaultProfile()) }
            }
        }
    }
    
    private fun loadMessages() {
        // Load sample messages for demonstration
        val sampleMessages = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                username = "Anonymous",
                message = "Just detected an Axon device in the downtown area!",
                timestamp = System.currentTimeMillis() - 300000,
                isAnonymous = true
            ),
            ChatMessage(
                id = UUID.randomUUID().toString(),
                username = "Anonymous",
                message = "Scanner is working great. Detection radius seems solid.",
                timestamp = System.currentTimeMillis() - 600000,
                isAnonymous = true
            ),
            ChatMessage(
                id = UUID.randomUUID().toString(),
                username = "Anonymous",
                message = "First detection today! 🔍",
                timestamp = System.currentTimeMillis() - 1200000,
                isAnonymous = true
            )
        )
        _uiState.update { it.copy(messages = sampleMessages) }
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
        val newMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            username = _uiState.value.userProfile?.username ?: "Anonymous",
            message = message,
            timestamp = System.currentTimeMillis(),
            isAnonymous = _uiState.value.userProfile?.isAnonymous ?: true
        )
        
        _uiState.update { state ->
            state.copy(messages = state.messages + newMessage)
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