package com.jackscanner.ui.screens.settings

import androidx.compose.foundation.background
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.layout.*
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.rememberScrollState
import com.jackscanner.ui.theme.sp
import androidx.compose.foundation.verticalScroll
import com.jackscanner.ui.theme.sp
import androidx.compose.material.icons.Icons
import com.jackscanner.ui.theme.sp
import androidx.compose.material.icons.filled.ArrowBack
import com.jackscanner.ui.theme.sp
import androidx.compose.material3.*
import com.jackscanner.ui.theme.sp
import androidx.compose.runtime.*
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.Alignment
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.Modifier
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.text.font.FontWeight
import com.jackscanner.ui.theme.sp
import androidx.compose.ui.unit.dp
import com.jackscanner.ui.theme.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.theme.sp
import com.jackscanner.ui.components.DetectionNotice
import com.jackscanner.ui.theme.sp
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.sp
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp
import java.text.SimpleDateFormat
import com.jackscanner.ui.theme.sp
import java.util.*
import com.jackscanner.ui.theme.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionDetailScreen(
    detectionId: String,
    onBack: () -> Unit,
    viewModel: DetectionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    
    LaunchedEffect(detectionId) {
        viewModel.loadDetection(detectionId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "DETECTION DETAILS",
                        color = colors.textPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background
                )
            )
        },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.detection?.let { detection ->
                // Main Detection Card
                GlassCard {
                    Column {
                        // Alert Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = colors.statusDanger.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column {
                                Text(
                                    text = "AXON DEVICE DETECTED",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.statusDanger
                                )
                                Text(
                                    text = detection.deviceName ?: "Unknown Device",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
                
                // Core Info
                GlassCard {
                    Column {
                        DetailRow("IDENTIFIER", detection.macAddress)
                        HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow("FIRST SEEN", formatDateTime(detection.firstSeen))
                        HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow("LAST SEEN", formatDateTime(detection.lastSeen))
                        HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DetailRow("OBSERVED SIGNALS", detection.observedSignals.toString())
                    }
                }
                
                // Technical Details (Expandable)
                var expanded by remember { mutableStateOf(false) }
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TECHNICAL DETAILS",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.textTertiary,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = if (expanded) 
                                    Icons.Default.KeyboardArrowUp 
                                else 
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = colors.textTertiary
                            )
                        }
                        
                        if (expanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            DetailRow("MAC ADDRESS", detection.macAddress)
                            HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                            DetailRow("RSSI", "${detection.rssi} dBm")
                            detection.manufacturerData?.let {
                                HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("MANUFACTURER DATA", it)
                            }
                            detection.advertisementData?.let {
                                HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("ADVERTISEMENT DATA", it)
                            }
                            if (detection.serviceUuids.isNotEmpty()) {
                                HorizontalDivider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                                DetailRow("SERVICE UUIDs", detection.serviceUuids.joinToString(", "))
                            }
                        }
                    }
                }
                
                // Detection Notice
                DetectionNotice()
                
                Spacer(modifier = Modifier.height(80.dp))
            } ?: run {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun androidx.compose.ui.unit.dp.clickable(onClick: () -> Unit): androidx.compose.ui.Modifier {
    return this.then(androidx.compose.ui.Modifier.clickable(onClick = onClick))
}

private val androidx.compose.foundation.shape.RoundedCornerShape = androidx.compose.foundation.shape.RoundedCornerShape

private val androidx.compose.ui.Modifier.padding: (vertical: androidx.compose.ui.unit.Dp) -> androidx.compose.ui.Modifier
    get() = { vertical ->
        this.padding(vertical = vertical)
    }