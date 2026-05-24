package com.jackscanner.ui.screens.heatmap

import androidx.compose.foundation.background
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.clickable
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.horizontalScroll
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.layout.*
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.rememberScrollState
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.jackscanner.ui.theme.sp
import androidx.compose.material.icons.Icons
import com.jackscanner.ui.theme.sp
import androidx.compose.material.icons.filled.FilterList
import com.jackscanner.ui.theme.sp
import androidx.compose.material3.*
import com.jackscanner.ui.theme.sp
import androidx.compose.runtime.*
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.Alignment
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.Modifier
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.draw.clip
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.text.font.FontWeight
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.unit.dp
import com.jackscanner.ui.theme.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.theme.sp
import com.jackscanner.domain.model.CommunityDetection
import com.jackscanner.ui.theme.sp
import com.jackscanner.domain.model.TimeRange
import com.jackscanner.ui.theme.sp
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.sp
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp

@Composable
fun HeatmapScreen(
    viewModel: HeatmapViewModel = hiltViewModel()
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HEATMAP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Community Detection Activity",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
            
            IconButton(onClick = { viewModel.toggleFilters() }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters",
                    tint = if (uiState.showFilters) colors.primary else colors.textSecondary
                )
            }
        }
        
        // Time Range Filter
        TimeRangeSelector(
            selectedRange = uiState.selectedTimeRange,
            onRangeSelected = { viewModel.setTimeRange(it) },
            isExpanded = uiState.showFilters
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Map placeholder
        MapPlaceholder()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selected Region Details
        uiState.selectedRegion?.let { region ->
            RegionDetailsCard(detection = region)
        } ?: run {
            // Info card
            GlassCard {
                Column {
                    Text(
                        text = "COMMUNITY HEATMAP",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Displays areas where BlueMeanie users have detected one or more Axon devices. Heat intensity reflects the concentration and frequency of detections recorded during the selected time period.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ActivityLevelIndicator()
                }
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit,
    isExpanded: Boolean
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeRange.entries.forEach { range ->
            val label = when (range) {
                TimeRange.ONE_HOUR -> "1H"
                TimeRange.THREE_HOURS -> "3H"
                TimeRange.TWELVE_HOURS -> "12H"
                TimeRange.TWENTY_FOUR_HOURS -> "24H"
                TimeRange.THREE_DAYS -> "3D"
                TimeRange.SEVEN_DAYS -> "7D"
                TimeRange.FOURTEEN_DAYS -> "14D"
                TimeRange.THIRTY_DAYS -> "30D"
                TimeRange.ONE_YEAR -> "1Y"
                TimeRange.ALL -> "ALL"
            }
            
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onRangeSelected(range) },
                color = if (selectedRange == range) colors.primary.copy(alpha = 0.3f) else colors.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedRange == range) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedRange == range) colors.primary else colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MapPlaceholder() {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🗺️",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Map View",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textSecondary
                )
                Text(
                    text = "Google Maps SDK integration required",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun ActivityLevelIndicator() {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ActivityLevelItem("LOW", colors.statusActive.copy(alpha = 0.3f))
        ActivityLevelItem("MODERATE", colors.statusActive.copy(alpha = 0.5f))
        ActivityLevelItem("HIGH", colors.statusActive.copy(alpha = 0.7f))
        ActivityLevelItem("VERY HIGH", colors.statusActive)
    }
}

@Composable
private fun ActivityLevelItem(label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
    }
}

@Composable
private fun RegionDetailsCard(detection: CommunityDetection) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Column {
            Text(
                text = "REGION DETAILS",
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
                DetailItem("DETECTIONS", detection.detectionCount.toString())
                DetailItem("ACTIVITY", "${(detection.intensity * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
