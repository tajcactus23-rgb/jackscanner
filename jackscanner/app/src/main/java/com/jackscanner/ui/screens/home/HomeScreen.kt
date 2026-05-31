package com.jackscanner.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
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
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.components.RadarAnimation
import com.jackscanner.ui.components.StatusBadge
import com.jackscanner.ui.theme.BlueMeanieTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    onDetectionClick: (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors

    val context = LocalContext.current
    
    // Permission step tracking
    var currentPermissionStep by remember { mutableIntStateOf(0) }
    
    // Define permission steps (requests one at a time)
    val permissionSteps = listOf(
        PermissionStep(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            title = "Location Required",
            description = "Location access is needed to detect BLE devices",
            rationale = "This app scans for Bluetooth devices and needs location permission to find nearby devices. This is required by Android for BLE scanning."
        ),
        PermissionStep(
            permission = Manifest.permission.ACCESS_COARSE_LOCATION,
            title = "Coarse Location",
            description = "Approximate location helps improve device detection",
            rationale = "Using coarse location improves the accuracy of device detection in your area."
        ),
        PermissionStep(
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                Manifest.permission.BLUETOOTH_SCAN else null,
            title = "Bluetooth Scan",
            description = "Required to scan for Bluetooth Low Energy devices",
            rationale = "Bluetooth scan permission allows the app to discover nearby BLE devices."
        ),
        PermissionStep(
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 
                Manifest.permission.BLUETOOTH_CONNECT else null,
            title = "Bluetooth Connect",
            description = "Needed to communicate with detected devices",
            rationale = "This permission allows the app to connect to and receive data from Bluetooth devices."
        ),
        PermissionStep(
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) 
                Manifest.permission.POST_NOTIFICATIONS else null,
            title = "Notifications",
            description = "Show alerts when devices are detected",
            rationale = "Get notified when the scanner detects devices, even when the app is in the background."
        )
    ).filter { it.permission != null }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Move to next permission step
            if (currentPermissionStep < permissionSteps.size - 1) {
                currentPermissionStep++
                // Launch next permission
                permissionSteps.getOrNull(currentPermissionStep)?.permission?.let {
                    permissionLauncher.launch(it)
                }
            } else {
                // All permissions granted
                viewModel.onPermissionsGranted()
            }
        } else {
            // Permission denied, show rationale dialog
            viewModel.onPermissionDenied()
        }
    }
    
    // Request first permission on first composable entry
    LaunchedEffect(Unit) {
        if (currentPermissionStep == 0 && permissionSteps.isNotEmpty()) {
            permissionSteps[0].permission?.let {
                permissionLauncher.launch(it)
            }
            currentPermissionStep = 0
        }
    }
    
    // Alert dialog for Bluetooth
    if (uiState.needsBluetoothEnable) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Bluetooth Required") },
            text = { Text("Please enable Bluetooth to scan for devices.") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Alert dialog for permission rationale
    if (uiState.needsPermissions && permissionSteps.isNotEmpty()) {
        val currentStep = permissionSteps.getOrNull(currentPermissionStep) ?: permissionSteps[0]
        AlertDialog(
            onDismissRequest = { },
            title = { Text(currentStep.title) },
            text = { Text(currentStep.description + "\n\n" + currentStep.rationale) },
            confirmButton = {
                TextButton(onClick = {
                    permissionLauncher.launch(currentStep.permission!!)
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Skip")
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            HomeHeader(
                isScanning = uiState.isScanning,
                scannerStatus = uiState.scannerStatus,
                onSettingsClick = onSettingsClick
            )
        }

        item { RadarCard(isScanning = uiState.isScanning) }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(title = "DETECTIONS TODAY", value = uiState.detectionsToday.toString(), modifier = Modifier.weight(1f))
                StatItem(title = "LAST DETECTION", value = uiState.lastDetection?.let { formatTime(it.lastSeen) } ?: "--:--", modifier = Modifier.weight(1f))
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(title = "COMMUNITY ACTIVITY", value = uiState.communityActivity.toString(), modifier = Modifier.weight(1f))
                StatItem(title = "SCANNER STATUS", value = if (uiState.isScanning) "ACTIVE" else "IDLE", isActive = uiState.isScanning, modifier = Modifier.weight(1f))
            }
        }

        item { ScannerButton(isScanning = uiState.isScanning, onClick = { viewModel.toggleScanning() }) }

        if (uiState.recentDetections.isNotEmpty()) {
            item { Text("RECENT DETECTIONS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = colors.textTertiary, modifier = Modifier.padding(top = 16.dp)) }
            items(uiState.recentDetections.take(5)) { detection ->
                DetectionItem(detection = detection, onClick = { onDetectionClick(detection.id) })
            }
        }
    }
}

@Composable
private fun HomeHeader(isScanning: Boolean, scannerStatus: ScannerStatus, onSettingsClick: () -> Unit) {
    val colors = BlueMeanieTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text("BLUEMEANIE", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colors.primary, letterSpacing = 2.sp)
            Text("AXON DETECTION SYSTEM", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, letterSpacing = 1.5.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(status = scannerStatus.name, isActive = isScanning)
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.surface)) {
                Icon(Icons.Default.Settings, "Settings", tint = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun RadarCard(isScanning: Boolean) {
    val colors = BlueMeanieTheme.colors
    GlassCard {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            RadarAnimation(isScanning = isScanning, modifier = Modifier.padding(vertical = 24.dp))
            AnimatedVisibility(visible = isScanning, enter = fadeIn(), exit = fadeOut()) {
                Text("SCANNING...", style = MaterialTheme.typography.labelLarge, color = colors.primary, letterSpacing = 2.sp)
            }
            if (!isScanning) Text("READY TO SCAN", style = MaterialTheme.typography.labelLarge, color = colors.textTertiary, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun StatItem(title: String, value: String, isActive: Boolean = false, modifier: Modifier = Modifier) {
    val colors = BlueMeanieTheme.colors
    GlassCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (isActive) colors.statusActive else colors.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, letterSpacing = 0.5.sp)
        }
    }
}

@Composable
private fun ScannerButton(isScanning: Boolean, onClick: () -> Unit) {
    val colors = BlueMeanieTheme.colors
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) colors.statusDanger else colors.primary)) {
        Icon(if (isScanning) Icons.Default.Stop else Icons.Default.PlayArrow, null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (isScanning) "STOP SCANNING" else "START SCANNING", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
    }
}

@Composable
private fun DetectionItem(detection: Detection, onClick: () -> Unit) {
    val colors = BlueMeanieTheme.colors
    GlassCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(colors.statusDanger.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Text("⚡", style = MaterialTheme.typography.titleMedium) }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("AXON DEVICE DETECTED", style = MaterialTheme.typography.labelMedium, color = colors.statusDanger, fontWeight = FontWeight.Bold)
                Text(detection.deviceName ?: "Unknown Device", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                Text(formatTime(detection.lastSeen), style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${detection.rssi ?: 0} dBm", style = MaterialTheme.typography.labelMedium, color = colors.statusWarning, fontWeight = FontWeight.Bold)
                Text("${detection.observedSignals ?: 0} signals", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            }
        }
    }
}

private fun formatTime(timestamp: Long): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
