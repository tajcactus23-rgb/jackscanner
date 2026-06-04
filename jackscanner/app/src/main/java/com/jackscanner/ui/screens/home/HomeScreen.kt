package com.jackscanner.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.Detection
import com.jackscanner.domain.model.ScannerStatus
import com.jackscanner.ui.components.GlassCard
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
    
    var activeTab by remember { mutableStateOf("radar") }
    val tabs = listOf("radar", "feed", "heatmap", "intel", "gear")
    val tabIcons = mapOf(
        "radar" to Icons.Default.Radar,
        "feed" to Icons.Default.List,
        "heatmap" to Icons.Default.Map,
        "intel" to Icons.Default.Insights,
        "gear" to Icons.Default.Settings
    )
    
    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            viewModel.onPermissionsGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }
    
    // Request permissions on first composition
    LaunchedEffect(Unit) {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
    
    // Alert dialogs
    if (uiState.needsBluetoothEnable) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Bluetooth Required") },
            text = { Text("Enable Bluetooth to scan for devices.") },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Animated background
        AnimatedBackground(colors)
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            HomeHeader(
                isScanning = uiState.isScanning,
                scannerStatus = uiState.scannerStatus,
                onSettingsClick = onSettingsClick,
                activeTab = activeTab,
                colors = colors
            )
            
            // Content based on tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "radar" -> RadarTab(
                        uiState = uiState,
                        onToggleScanning = { viewModel.toggleScanning() },
                        onDetectionClick = onDetectionClick,
                        colors = colors
                    )
                    "feed" -> FeedTab(
                        detections = uiState.recentDetections,
                        onDetectionClick = onDetectionClick,
                        colors = colors
                    )
                    "heatmap" -> HeatmapTab(colors = colors)
                    "intel" -> IntelTab(colors = colors)
                    "gear" -> SettingsTab(onSettingsClick = onSettingsClick, colors = colors)
                }
            }
            
            // Bottom Navigation
            BottomNavigationBar(
                activeTab = activeTab,
                onTabChange = { activeTab = it },
                tabs = tabs,
                tabIcons = tabIcons,
                colors = colors
            )
        }
    }
}

@Composable
private fun AnimatedBackground(colors: BlueMeanieTheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Radial gradient from center
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.primary.copy(alpha = pulseAlpha),
                    Color.Transparent
                ),
                center = Offset(size.width / 2, size.height * 0.3f),
                radius = size.width * 0.8f
            )
        )
    }
}

@Composable
private fun HomeHeader(
    isScanning: Boolean,
    scannerStatus: ScannerStatus,
    onSettingsClick: () -> Unit,
    activeTab: String,
    colors: BlueMeanieTheme
) {
    val headerTitle = when (activeTab) {
        "radar" -> "RADAR"
        "feed" -> "FEED"
        "heatmap" -> "HEATMAP"
        "intel" -> "INTEL"
        "gear" -> "SETTINGS"
        else -> "BLUEMEANIE"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "BLUE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = colors.primary,
                letterSpacing = 4.sp
            )
            Text(
                text = headerTitle,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                letterSpacing = 2.sp
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Status indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isScanning) colors.statusActive else colors.textTertiary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isScanning) "SCANNING" else "STANDBY",
                style = MaterialTheme.typography.labelSmall,
                color = if (isScanning) colors.statusActive else colors.textTertiary,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.surface)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun RadarTab(
    uiState: HomeUiState,
    onToggleScanning: () -> Unit,
    onDetectionClick: (String) -> Unit,
    colors: BlueMeanieTheme
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Large Radar Display
        item {
            RadarDisplay(
                isScanning = uiState.isScanning,
                detections = uiState.recentDetections.size,
                colors = colors
            )
        }
        
        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickStat(
                    label = "TODAY",
                    value = uiState.detectionsToday.toString(),
                    icon = Icons.Default.Today,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                QuickStat(
                    label = "SIGNAL",
                    value = if (uiState.lastDetection != null) "${uiState.lastDetection.rssi ?: 0}" else "--",
                    icon = Icons.Default.SignalCellularAlt,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                QuickStat(
                    label = "STATUS",
                    value = if (uiState.isScanning) "ACTIVE" else "IDLE",
                    icon = Icons.Default.Radar,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Scan Button
        item {
            ScanButton(
                isScanning = uiState.isScanning,
                onClick = onToggleScanning,
                colors = colors
            )
        }
        
        // Recent Detections
        if (uiState.recentDetections.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT CONTACTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(uiState.recentDetections.take(5)) { detection ->
                DetectionCard(
                    detection = detection,
                    onClick = { onDetectionClick(detection.id) },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun RadarDisplay(
    isScanning: Boolean,
    detections: Int,
    colors: BlueMeanieTheme
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(colors.surface.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 8
            
            // Outer ring
            drawCircle(
                color = colors.primary.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // Middle ring
            drawCircle(
                color = colors.primary.copy(alpha = 0.2f),
                radius = radius * 0.65f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            
            // Inner ring
            drawCircle(
                color = colors.primary.copy(alpha = 0.15f),
                radius = radius * 0.35f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            
            if (isScanning) {
                // Scan sweep
                val sweepAngle = Math.toRadians(scanAngle.toDouble())
                val sweepLength = radius * 0.95f
                
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.8f),
                            colors.primary.copy(alpha = 0f)
                        ),
                        start = center,
                        end = Offset(
                            center.x + sweepLength * kotlin.math.cos(sweepAngle).toFloat(),
                            center.y + sweepLength * kotlin.math.sin(sweepAngle).toFloat()
                        )
                    ),
                    start = center,
                    end = Offset(
                        center.x + sweepLength * kotlin.math.cos(sweepAngle).toFloat(),
                        center.y + sweepLength * kotlin.math.sin(sweepAngle).toFloat()
                    ),
                    strokeWidth = 4.dp.toPx()
                )
                
                // Center glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.primary.copy(alpha = 0.5f * pulseScale),
                            colors.primary.copy(alpha = 0f)
                        ),
                        center = center,
                        radius = 20.dp.toPx() * pulseScale
                    ),
                    radius = 12.dp.toPx(),
                    center = center
                )
            } else {
                // Idle center
                drawCircle(
                    color = colors.textTertiary.copy(alpha = 0.5f),
                    radius = 6.dp.toPx(),
                    center = center
                )
            }
        }
        
        // Status text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isScanning) "SCANNING" else "READY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isScanning) colors.statusActive else colors.textSecondary,
                letterSpacing = 2.sp
            )
            Text(
                text = "$detections contacts",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun QuickStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colors: BlueMeanieTheme,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun ScanButton(
    isScanning: Boolean,
    onClick: () -> Unit,
    colors: BlueMeanieTheme
) {
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (isScanning) "STOP SCANNING" else "START SCANNING",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DetectionCard(
    detection: Detection,
    onClick: () -> Unit,
    colors: BlueMeanieTheme
) {
    GlassCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Threat indicator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.statusDanger.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = colors.statusDanger,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = detection.deviceName ?: "UNKNOWN DEVICE",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
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
                    text = "${detection.rssi ?: 0} dBm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.statusWarning
                )
                Text(
                    text = "${detection.observedSignals ?: 0} pings",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun FeedTab(
    detections: List<Detection>,
    onDetectionClick: (String) -> Unit,
    colors: BlueMeanieTheme
) {
    if (detections.isEmpty()) {
        EmptyState(
            icon = Icons.Default.SignalCellularAlt,
            message = "No contacts detected yet",
            colors = colors
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "${detections.size} CONTACTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textTertiary,
                    letterSpacing = 1.5.sp
                )
            }
            
            items(detections) { detection ->
                DetectionCard(
                    detection = detection,
                    onClick = { onDetectionClick(detection.id) },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun HeatmapTab(colors: BlueMeanieTheme) {
    EmptyState(
        icon = Icons.Default.Map,
        message = "Heatmap coming soon",
        colors = colors
    )
}

@Composable
private fun IntelTab(colors: BlueMeanieTheme) {
    EmptyState(
        icon = Icons.Default.Insights,
        message = "Intel analysis coming soon",
        colors = colors
    )
}

@Composable
private fun SettingsTab(onSettingsClick: () -> Unit, colors: BlueMeanieTheme) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp, bottom = 100.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.clickable(onClick = onSettingsClick)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Tune,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Scanner Settings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Auto-start, alerts, modes",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.textTertiary
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard {
                Column {
                    Text(
                        text = "Version 2.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                    Text(
                        text = "BlueMeanie APEX",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    colors: BlueMeanieTheme
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    activeTab: String,
    onTabChange: (String) -> Unit,
    tabs: List<String>,
    tabIcons: Map<String, androidx.compose.ui.graphics.vector.ImageVector>,
    colors: BlueMeanieTheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface.copy(alpha = 0.95f))
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEach { tab ->
            val isActive = activeTab == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) colors.primary.copy(alpha = 0.2f) else Color.Transparent)
                    .clickable { onTabChange(tab) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tabIcons[tab]!!,
                    contentDescription = tab,
                    tint = if (isActive) colors.primary else colors.textTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatTime(timestamp: Long): String = 
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))