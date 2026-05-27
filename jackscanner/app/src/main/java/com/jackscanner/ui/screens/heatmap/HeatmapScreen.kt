package com.jackscanner.ui.screens.heatmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.jackscanner.domain.model.CommunityDetection
import com.jackscanner.domain.model.TimeRange
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme

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
        
        // Google Maps Heatmap
        HeatmapView(
            detections = uiState.communityDetections,
            onRegionSelected = { viewModel.selectRegion(it) }
        )
        
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

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun HeatmapView(
    detections: List<CommunityDetection>,
    onRegionSelected: (CommunityDetection?) -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    // Convert detections to LatLng points
    val heatmapPoints = remember(detections) {
        detections.mapNotNull { detection ->
            if (detection.latitude != null && detection.longitude != null) {
                LatLng(detection.latitude, detection.longitude)
            } else null
        }
    }
    
    // Default camera position (US center)
    val defaultPosition = LatLng(39.8283, -98.5795)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPosition, 4f)
    }
    
    // Auto-zoom to fit detections
    LaunchedEffect(heatmapPoints) {
        if (heatmapPoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            heatmapPoints.forEach { boundsBuilder.include(it) }
            try {
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 100)
                )
            } catch (e: Exception) {
                // Fallback to default zoom if bounds calculation fails
            }
        }
    }
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (heatmapPoints.isEmpty()) {
                // Show info when no detections available
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📡",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Detection Data",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Start scanning to build the heatmap",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary
                        )
                    }
                }
            } else {
                // Create heatmap provider
                val heatmapProvider = remember(heatmapPoints) {
                    HeatmapTileProvider.Builder()
                        .data(heatmapPoints)
                        .radius(25)
                        .maxIntensity(10.0)
                        .build()
                }
                
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled = false
                    ),
                    properties = MapProperties(
                        mapType = MapType.NORMAL
                    )
                ) {
                    // Add heatmap tile overlay
                    TileOverlay(
                        tileProvider = heatmapProvider,
                        visible = true
                    )
                    
                    // Add markers for each detection point
                    heatmapPoints.forEachIndexed { index, latLng ->
                        val detection = detections.getOrNull(index)
                        Marker(
                            state = MarkerState(position = latLng),
                            title = detection?.let { "Detections: ${it.detectionCount}" } ?: "Detection",
                            snippet = detection?.let {
                                "Activity: ${(it.intensity * 100).toInt()}%"
                            },
                            onClick = {
                                onRegionSelected(detection)
                                true
                            }
                        )
                    }
                }
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
    val colors = BlueMeanieTheme.colors
    
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
    }
}

