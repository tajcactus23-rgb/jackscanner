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
        // Sample leaderboard data
        val sampleLeaderboard = listOf(
            LeaderboardEntry(1, "ScannerPro", 1250, 5200, UserRank.LEGEND, false),
            LeaderboardEntry(2, "Anonymous", 890, 3400, UserRank.ELITE, true),
            LeaderboardEntry(3, "AxonHunter", 720, 2800, UserRank.ELITE, false),
            LeaderboardEntry(4, "Anonymous", 550, 1900, UserRank.VETERAN, true),
            LeaderboardEntry(5, "BlueScout", 420, 1500, UserRank.VETERAN, false),
            LeaderboardEntry(6, "Anonymous", 380, 1200, UserRank.ANALYST, true),
            LeaderboardEntry(7, "DetectorX", 290, 950, UserRank.ANALYST, false),
            LeaderboardEntry(8, "Anonymous", 220, 750, UserRank.TRACKER, true),
            LeaderboardEntry(9, "ScannerOne", 180, 600, UserRank.TRACKER, false),
            LeaderboardEntry(10, "Anonymous", 150, 450, UserRank.SCOUT, true)
        )
        _uiState.update { it.copy(leaderboard = sampleLeaderboard) }
    }
}