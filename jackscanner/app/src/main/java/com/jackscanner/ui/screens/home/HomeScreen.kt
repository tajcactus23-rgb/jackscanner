package com.jackscanner.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.components.RadarAnimation
import com.jackscanner.ui.components.StatusBadge
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onDetectionClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Header
        item {
            HomeHeader(
                isScanning = uiState.isScanning,
                scannerStatus = uiState.scannerStatus
            )
        }
        
        // Radar Card
        item {
            RadarCard(isScanning = uiState.isScanning)
        }
        
        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    title = "DETECTIONS TODAY",
                    value = uiState.detectionsToday.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "LAST DETECTION",
                    value = uiState.lastDetection?.let { formatTime(it.lastSeen) } ?: "--:--",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    title = "COMMUNITY ACTIVITY",
                    value = uiState.communityActivity.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    title = "SCANNER STATUS",
                    value = if (uiState.isScanning) "ACTIVE" else "IDLE",
                    isActive = uiState.isScanning,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Scanner Control Button
        item {
            ScannerButton(
                isScanning = uiState.isScanning,
                onClick = { viewModel.toggleScanning() }
            )
        }
        
        // Recent Detections Section
        if (uiState.recentDetections.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT DETECTIONS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            items(uiState.recentDetections.take(5)) { detection ->
                DetectionItem(
                    detection = detection,
                    onClick = { onDetectionClick(detection.id) }
                )
            }
        }
        
        // Bottom spacing for navigation
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun HomeHeader(
    isScanning: Boolean,
    scannerStatus: ScannerStatus
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BLUEMEANIE",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "AXON DETECTION SYSTEM",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                letterSpacing = 1.5.sp
            )
        }
        
        StatusBadge(
            status = scannerStatus.name,
            isActive = isScanning
        )
    }
}

@Composable
private fun RadarCard(isScanning: Boolean) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RadarAnimation(
                isScanning = isScanning,
                modifier = Modifier.padding(vertical = 24.dp)
            )
            
            AnimatedVisibility(
                visible = isScanning,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "SCANNING...",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    letterSpacing = 2.sp
                )
            }
            
            if (!isScanning) {
                Text(
                    text = "READY TO SCAN",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textTertiary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isActive) colors.statusActive else colors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ScannerButton(
    isScanning: Boolean,
    onClick: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isScanning) colors.statusDanger else colors.primary
        )
    ) {
        Icon(
            imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isScanning) "STOP SCANNING" else "START SCANNING",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DetectionItem(
    detection: Detection,
    onClick: () -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    GlassCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Alert Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.statusDanger.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AXON DEVICE DETECTED",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.statusDanger,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = detection.deviceName ?: "Unknown Device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = formatTime(detection.lastSeen),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${detection.rssi} dBm",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.statusWarning,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${detection.observedSignals} signals",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

