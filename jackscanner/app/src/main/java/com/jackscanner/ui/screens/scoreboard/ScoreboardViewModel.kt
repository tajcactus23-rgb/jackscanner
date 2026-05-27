package com.jackscanner.ui.screens.scoreboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jackscanner.domain.model.UserProfile
import com.jackscanner.domain.model.UserRank
import com.jackscanner.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScoreboardUiState(
    val userProfile: UserProfile? = null,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false
)

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val detectionCount: Int,
    val reputationScore: Int,
    val userRank: UserRank,
    val isAnonymous: Boolean
)

@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScoreboardUiState())
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
        loadLeaderboard()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            userRepository.getUserProfile().collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
    }
    
    private fun loadLeaderboard() {
        // Load real leaderboard data from user profile
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getUserProfile().collect { profile ->
                if (profile != null) {
                    // Build leaderboard with actual user data
                    val userEntry = LeaderboardEntry(
                        rank = 0,
                        username = profile.username,
                        detectionCount = profile.detectionCount,
                        reputationScore = profile.reputationScore,
                        userRank = profile.rank,
                        isAnonymous = profile.isAnonymous
                    )
                    _uiState.update { it.copy(leaderboard = listOf(userEntry), isLoading = false) }
                } else {
                    _uiState.update { it.copy(leaderboard = emptyList(), isLoading = false) }
                }
            }
        }
    }
}