package com.jackscanner.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.Detection
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(
    onDetectionClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
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
            text = "FEED",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DetectionFilter.entries) { filter ->
                FilterChip(
                    filter = filter,
                    isSelected = uiState.selectedFilter == filter,
                    onClick = { viewModel.setFilter(filter) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Detection List
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.primary)
            }
        } else if (uiState.detections.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.detections) { detection ->
                    FeedDetectionItem(
                        detection = detection,
                        onClick = { onDetectionClick(detection.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    filter: DetectionFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) colors.primary.copy(alpha = 0.2f) else colors.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = filter.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) colors.primary else colors.textSecondary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun FeedDetectionItem(
    detection: Detection,
    onClick: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.statusDanger.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AXON DEVICE DETECTED",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.statusDanger,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = detection.deviceName ?: "Unknown Device",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeedStatItem(
                    label = "SIGNALS",
                    value = detection.observedSignals.toString()
                )
                FeedStatItem(
                    label = "FIRST SEEN",
                    value = formatTime(detection.firstSeen)
                )
                FeedStatItem(
                    label = "LAST SEEN",
                    value = formatTime(detection.lastSeen)
                )
                FeedStatItem(
                    label = "RSSI",
                    value = "${detection.rssi} dBm"
                )
            }
        }
    }
}

@Composable
private fun FeedStatItem(
    label: String,
    value: String
) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
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
private fun EmptyState() {
    val colors = BlueMeanieTheme.colors
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📡",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Detections",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textSecondary
            )
            Text(
                text = "Start scanning to detect Axon devices",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

