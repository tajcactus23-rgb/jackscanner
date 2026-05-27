package com.jackscanner.ui.screens.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.UserRank
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme

@Composable
fun ScoreboardScreen(
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Text(
            text = "SCOREBOARD",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // User Stats Card
        uiState.userProfile?.let { profile ->
            UserStatsCard(profile = profile)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Rank Tiers Legend
        RankTiersLegend()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Leaderboard
        Text(
            text = "TOP SCANNERS",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            itemsIndexed(uiState.leaderboard) { index, entry ->
                LeaderboardItem(entry = entry, isTopThree = entry.rank <= 3)
            }
        }
    }
}

@Composable
private fun UserStatsCard(profile: com.jackscanner.domain.model.UserProfile) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR STATS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    letterSpacing = 1.sp
                )
                
                Surface(
                    color = getRankColor(profile.rank).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = profile.rank.displayName.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = getRankColor(profile.rank),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("DETECTIONS", profile.detectionCount.toString())
                StatItem("MESSAGES", profile.messages.toString())
                StatItem("SCORE", profile.reputationScore.toString())
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun RankTiersLegend() {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Column {
            Text(
                text = "RANK TIERS",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textTertiary,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RankTierItem(UserRank.OBSERVER, "0")
                RankTierItem(UserRank.SCOUT, "10")
                RankTierItem(UserRank.TRACKER, "50")
                RankTierItem(UserRank.ANALYST, "100")
                RankTierItem(UserRank.VETERAN, "500")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RankTierItem(UserRank.ELITE, "1000")
                RankTierItem(UserRank.LEGEND, "5000")
                Spacer(modifier = Modifier.width(60.dp))
                Spacer(modifier = Modifier.width(60.dp))
                Spacer(modifier = Modifier.width(60.dp))
            }
        }
    }
}

@Composable
private fun RankTierItem(rank: UserRank, minScore: String) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(getRankColor(rank).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (rank == UserRank.LEGEND) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = getRankColor(rank),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = rank.name.first().toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = getRankColor(rank)
                )
            }
        }
        Text(
            text = rank.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = getRankColor(rank)
        )
        Text(
            text = minScore,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun LeaderboardItem(
    entry: LeaderboardEntry,
    isTopThree: Boolean
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTopThree) getRankColor(entry.userRank).copy(alpha = 0.2f)
                        else colors.surface
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isTopThree) getRankColor(entry.userRank) else colors.textSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (entry.isAnonymous) "Anonymous" else entry.username,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    if (entry.isAnonymous) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(Anonymous)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                }
                Row {
                    Text(
                        text = entry.userRank.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = getRankColor(entry.userRank),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Stats
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.detectionCount}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
                Text(
                    text = "${entry.reputationScore} pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun getRankColor(rank: UserRank) = when (rank) {
    UserRank.OBSERVER -> BlueMeanieTheme.colors.textTertiary
    UserRank.SCOUT -> BlueMeanieTheme.colors.statusSilver
    UserRank.TRACKER -> BlueMeanieTheme.colors.statusBronze
    UserRank.ANALYST -> BlueMeanieTheme.colors.statusActive
    UserRank.VETERAN -> BlueMeanieTheme.colors.statusWarning
    UserRank.ELITE -> BlueMeanieTheme.colors.primary
    UserRank.LEGEND -> BlueMeanieTheme.colors.statusGold
}

