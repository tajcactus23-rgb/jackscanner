@file:OptIn(ExperimentalMaterial3Api::class)

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
import com.jackscanner.ui.theme.BlueMeanieColors
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
private fun AnimatedBackground(colors: BlueMeanieColors) {
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
    colors: BlueMeanieColors
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
                    .background(colors.surface, RoundedCornerShape(3.dp))
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
    colors: BlueMeanieColors
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
    colors: BlueMeanieColors
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
    colors: BlueMeanieColors,
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
    colors: BlueMeanieColors
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
    colors: BlueMeanieColors
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
    colors: BlueMeanieColors
) {
    var filter by remember { mutableStateOf("ALL") }
    var sortBy by remember { mutableStateOf("TIME") }
    var searchQuery by remember { mutableStateOf("") }
    
    val filters = listOf("ALL", "AXON", "SIGNALS")
    val sorts = listOf("TIME", "SIGNAL", "RECENT")
    
    val filteredDetections = remember(detections, filter, sortBy, searchQuery) {
        detections
            .filter { detection ->
                val matchesFilter = when (filter) {
                    "AXON" -> detection.deviceName?.contains("AXON", ignoreCase = true) == true
                    "SIGNALS" -> (detection.observedSignals ?: 0) > 5
                    else -> true
                }
                val matchesSearch = searchQuery.isEmpty() || 
                    detection.deviceName?.contains(searchQuery, ignoreCase = true) == true ||
                    detection.id.contains(searchQuery)
                matchesFilter && matchesSearch
            }
            .sortedByDescending { detection: Detection ->
                when (sortBy) {
                    "TIME" -> detection.lastSeen.toLong()
                    "SIGNAL" -> ((detection.rssi ?: -100) * -1).toLong()
                    "RECENT" -> detection.firstSeen.toLong()
                    else -> detection.lastSeen.toLong()
                }
            }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { 
                Text(
                    "Search devices...",
                    color = colors.textTertiary,
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.textTertiary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = colors.textTertiary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        // Filters row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Filter chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filters.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = {
                            Text(
                                f,
                                fontSize = 11.sp,
                                fontWeight = if (filter == f) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary.copy(alpha = 0.2f),
                            selectedLabelColor = colors.primary,
                            containerColor = colors.surface,
                            labelColor = colors.textTertiary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (filter == f) colors.primary else colors.border,
                            selectedBorderColor = colors.primary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Sort dropdown
            var expanded by remember { mutableStateOf(false) }
            Box {
                TextButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.textSecondary)
                ) {
                    Text(
                        "Sort: $sortBy",
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    sorts.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s, color = colors.textPrimary) },
                            onClick = {
                                sortBy = s
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        
        // Stats summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val axonCount = filteredDetections.count { 
                it.deviceName?.contains("AXON", ignoreCase = true) == true 
            }
            val totalSignals = filteredDetections.sumOf { it.observedSignals ?: 0 }
            
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${filteredDetections.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = "DEVICES",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$axonCount",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.statusDanger
                    )
                    Text(
                        text = "AXON",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalSignals",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.statusWarning
                    )
                    Text(
                        text = "SIGNALS",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
            }
        }
        
        // Detection list
        if (filteredDetections.isEmpty()) {
            EmptyState(
                icon = Icons.Default.SearchOff,
                message = if (searchQuery.isNotEmpty()) "No devices match your search" else "No contacts detected yet",
                colors = colors
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)
            ) {
                items(filteredDetections) { detection ->
                    DetailedDetectionCard(
                        detection = detection,
                        onClick = { onDetectionClick(detection.id) },
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailedDetectionCard(
    detection: Detection,
    onClick: () -> Unit,
    colors: BlueMeanieColors
) {
    val isAxon = detection.deviceName?.contains("AXON", ignoreCase = true) == true
    val rssi = detection.rssi ?: 0
    
    GlassCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Threat level indicator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isAxon) colors.statusDanger.copy(alpha = 0.2f) 
                            else colors.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAxon) Icons.Default.Warning else Icons.Default.BluetoothConnected,
                        contentDescription = null,
                        tint = if (isAxon) colors.statusDanger else colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detection.deviceName ?: "UNKNOWN DEVICE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAxon) colors.statusDanger else colors.textPrimary
                    )
                    Row {
                        Text(
                            text = detection.id.take(17),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                        if (isAxon) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = colors.statusDanger.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "THREAT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.statusDanger,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatBadge(
                    label = "RSSI",
                    value = "$rssi dBm",
                    color = when {
                        rssi > -60 -> colors.statusActive
                        rssi > -80 -> colors.statusWarning
                        else -> colors.statusDanger
                    }
                )
                StatBadge(
                    label = "SIGNALS",
                    value = "${detection.observedSignals ?: 0}",
                    color = colors.primary
                )
                StatBadge(
                    label = "FIRST",
                    value = formatTimeShort(detection.firstSeen),
                    color = colors.textSecondary
                )
                StatBadge(
                    label = "LAST",
                    value = formatTimeShort(detection.lastSeen),
                    color = colors.textSecondary
                )
            }
            
            // Signal strength bar
            Spacer(modifier = Modifier.height(8.dp))
            val signalStrength = ((rssi + 100f) / 50f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.surface, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(signalStrength)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                rssi > -60 -> colors.statusActive
                                rssi > -80 -> colors.statusWarning
                                else -> colors.statusDanger
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

private fun formatTimeShort(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))

@Composable
private fun HeatmapTab(colors: BlueMeanieColors) {
    var showCommunity by remember { mutableStateOf(false) }
    
    // Mock data for visualization
    val detections = remember {
        listOf(
            HeatmapPoint(-0.002, 0.003, "AXON", true),
            HeatmapPoint(0.001, -0.001, "BLE", false),
            HeatmapPoint(-0.003, 0.002, "AXON", true),
            HeatmapPoint(0.002, -0.002, "BLE", false),
            HeatmapPoint(0.001, 0.004, "AXON", true),
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Header with controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HEATMAP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${detections.size} detections in range",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Community",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = showCommunity,
                    onCheckedChange = { showCommunity = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.statusWarning,
                        checkedTrackColor = colors.statusWarning.copy(alpha = 0.3f)
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Canvas heatmap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface, RoundedCornerShape(3.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2
                val cy = h / 2
                val maxR = minOf(w, h) / 2 - 20
                
                // Draw grid
                for (i in 0..4) {
                    val r = maxR * i / 4
                    drawCircle(
                        color = colors.border.copy(alpha = 0.3f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                
                // Cross lines
                drawLine(
                    color = colors.border.copy(alpha = 0.2f),
                    start = Offset(cx, 0f),
                    end = Offset(cx, h),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = colors.border.copy(alpha = 0.2f),
                    start = Offset(0f, cy),
                    end = Offset(w, cy),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw detection points as heat blobs
                detections.forEach { point ->
                    val px = cx + (point.x * 8000).toFloat()
                    val py = cy - (point.y * 8000).toFloat()
                    val isAxon = point.isThreat
                    
                    // Heat blob
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = if (isAxon) {
                                listOf(colors.statusDanger.copy(alpha = 0.6f), colors.statusDanger.copy(alpha = 0f))
                            } else {
                                listOf(colors.primary.copy(alpha = 0.4f), colors.primary.copy(alpha = 0f))
                            },
                            center = Offset(px, py),
                            radius = if (isAxon) 50.dp.toPx() else 35.dp.toPx()
                        ),
                        radius = if (isAxon) 50.dp.toPx() else 35.dp.toPx(),
                        center = Offset(px, py)
                    )
                    
                    // Center dot
                    drawCircle(
                        color = if (isAxon) colors.statusDanger else colors.primary,
                        radius = if (isAxon) 8.dp.toPx() else 5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
                
                // Center position marker
                drawCircle(
                    color = colors.statusActive,
                    radius = 10.dp.toPx(),
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = colors.statusActive.copy(alpha = 0.3f),
                    radius = 20.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Direction labels
                val labelStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
            }
            
            // Legend overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                LegendItem(color = colors.statusDanger, label = "AXON", colors = colors)
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = colors.primary, label = "BLE", colors = colors)
                Spacer(modifier = Modifier.height(4.dp))
                LegendItem(color = colors.statusActive, label = "YOU", colors = colors)
            }
            
            // Stats overlay
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(colors.surface.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                val axonCount = detections.count { it.isThreat }
                Text(
                    text = "$axonCount AXON",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.statusDanger,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${detections.size - axonCount} BLE",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info card
        GlassCard {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Detections are relative to your position. Accuracy varies by device.",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, colors: BlueMeanieColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
    }
}

data class HeatmapPoint(
    val x: Double,
    val y: Double,
    val label: String,
    val isThreat: Boolean
)

@Composable
private fun IntelTab(colors: BlueMeanieColors) {
    // Mock stats data
    val stats = remember {
        IntelStats(
            totalDetections = 127,
            axonCount = 12,
            avgSignal = -72,
            peakSignal = -45,
            scanTimeHours = 4.5f,
            mostActiveHour = "14:00",
            topDeviceType = "AXON BODY"
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Text(
                text = "INTEL ANALYSIS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Session statistics and insights",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }
        
        // Main stats grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IntelCard(
                        modifier = Modifier.weight(1f),
                        title = "TOTAL",
                        value = "${stats.totalDetections}",
                        subtitle = "Detections",
                        icon = Icons.Default.Radar,
                        color = colors.primary,
                        colors = colors
                    )
                    IntelCard(
                        modifier = Modifier.weight(1f),
                        title = "THREATS",
                        value = "${stats.axonCount}",
                        subtitle = "AXON Devices",
                        icon = Icons.Default.Warning,
                        color = colors.statusDanger,
                        colors = colors
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IntelCard(
                        modifier = Modifier.weight(1f),
                        title = "AVG SIGNAL",
                        value = "${stats.avgSignal} dBm",
                        subtitle = "Signal Strength",
                        icon = Icons.Default.SignalCellularAlt,
                        color = colors.statusWarning,
                        colors = colors
                    )
                    IntelCard(
                        modifier = Modifier.weight(1f),
                        title = "PEAK",
                        value = "${stats.peakSignal} dBm",
                        subtitle = "Strongest Signal",
                        icon = Icons.Default.TrendingUp,
                        color = colors.statusActive,
                        colors = colors
                    )
                }
            }
        }
        
        // Detailed stats
        item {
            GlassCard {
                Column {
                    Text(
                        text = "SESSION METRICS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    IntelRow(
                        label = "Scan Duration",
                        value = "${stats.scanTimeHours}h active",
                        colors = colors
                    )
                    IntelRow(
                        label = "Most Active Hour",
                        value = stats.mostActiveHour,
                        colors = colors
                    )
                    IntelRow(
                        label = "Top Device Type",
                        value = stats.topDeviceType,
                        colors = colors
                    )
                    IntelRow(
                        label = "Detection Rate",
                        value = "${(stats.totalDetections / stats.scanTimeHours).toInt()}/h",
                        colors = colors
                    )
                }
            }
        }
        
        // Threat breakdown
        item {
            GlassCard {
                Column {
                    Text(
                        text = "THREAT BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.statusDanger,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ThreatBar(
                        label = "AXON BODY",
                        count = 5,
                        total = stats.axonCount,
                        color = colors.statusDanger,
                        colors = colors
                    )
                    ThreatBar(
                        label = "AXON FLEX",
                        count = 4,
                        total = stats.axonCount,
                        color = colors.statusWarning,
                        colors = colors
                    )
                    ThreatBar(
                        label = "AXON CAMERA",
                        count = 3,
                        total = stats.axonCount,
                        color = colors.primary,
                        colors = colors
                    )
                }
            }
        }
        
        // Signal distribution
        item {
            GlassCard {
                Column {
                    Text(
                        text = "SIGNAL DISTRIBUTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    SignalBar(
                        label = "> -50 dBm",
                        percent = 0.15f,
                        color = colors.statusActive,
                        colors = colors
                    )
                    SignalBar(
                        label = "-50 to -70 dBm",
                        percent = 0.45f,
                        color = colors.statusWarning,
                        colors = colors
                    )
                    SignalBar(
                        label = "-70 to -85 dBm",
                        percent = 0.30f,
                        color = colors.primary,
                        colors = colors
                    )
                    SignalBar(
                        label = "< -85 dBm",
                        percent = 0.10f,
                        color = colors.textTertiary,
                        colors = colors
                    )
                }
            }
        }
        
        // Recommendation
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = colors.statusWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "INSIGHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.statusWarning,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "High concentration of AXON devices detected. Consider increasing scan sensitivity.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntelCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    colors: BlueMeanieColors
) {
    GlassCard(modifier = modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun IntelRow(
    label: String,
    value: String,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ThreatBar(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    colors: BlueMeanieColors
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surface, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(count.toFloat() / total)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun SignalBar(
    label: String,
    percent: Float,
    color: Color,
    colors: BlueMeanieColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.width(100.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.surface, RoundedCornerShape(3.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(percent * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(40.dp)
        )
    }
}

data class IntelStats(
    val totalDetections: Int,
    val axonCount: Int,
    val avgSignal: Int,
    val peakSignal: Int,
    val scanTimeHours: Float,
    val mostActiveHour: String,
    val topDeviceType: String
)

@Composable
private fun SettingsTab(onSettingsClick: () -> Unit, colors: BlueMeanieColors) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
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
    colors: BlueMeanieColors
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
    colors: BlueMeanieColors
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