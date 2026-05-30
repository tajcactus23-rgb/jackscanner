package com.jackscanner.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.ScanMode
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                SettingsSection(title = "SCANNER") {
                    SettingsToggle(
                        title = "Auto-start on boot",
                        subtitle = "Automatically start scanning when device boots",
                        icon = Icons.Default.Power,
                        checked = uiState.scannerSettings.autoStartOnBoot,
                        onCheckedChange = { viewModel.toggleAutoStart() }
                    )
                    SettingsToggle(
                        title = "Alert Sound",
                        subtitle = "Play sound when device is detected",
                        icon = Icons.Default.VolumeUp,
                        checked = uiState.scannerSettings.alertSound,
                        onCheckedChange = { viewModel.toggleAlertSound() }
                    )
                    SettingsToggle(
                        title = "Alert Vibration",
                        subtitle = "Vibrate when device is detected",
                        icon = Icons.Default.Vibration,
                        checked = uiState.scannerSettings.alertVibration,
                        onCheckedChange = { viewModel.toggleAlertVibration() }
                    )
                    ScanModeSelector(
                        selectedMode = uiState.scannerSettings.scanMode,
                        onModeSelected = { viewModel.setScanMode(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "NOTIFICATIONS") {
                    SettingsToggle(
                        title = "Detection Notifications",
                        subtitle = "Show notifications when devices are detected",
                        icon = Icons.Default.Notifications,
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                }
            }

            item {
                SettingsSection(title = "ABOUT") {
                    GlassCard {
                        Column {
                            Text(
                                text = "JackScanner Lite",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Version 2.0.0 Lite",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Axon Device Scanner",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BlueMeanieTheme.colors
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = BlueMeanieTheme.colors
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.primary,
                    checkedTrackColor = colors.primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun ScanModeSelector(
    selectedMode: ScanMode,
    onModeSelected: (ScanMode) -> Unit
) {
    val colors = BlueMeanieTheme.colors
    GlassCard {
        Column {
            Text(
                text = "Scan Mode",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = when (selectedMode) {
                    ScanMode.LOW_POWER -> "Optimized for battery life"
                    ScanMode.BALANCED -> "Balanced performance and battery"
                    ScanMode.LOW_LATENCY -> "Fastest detection, higher battery use"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanMode.entries.forEach { mode ->
                    val label = when (mode) {
                        ScanMode.LOW_POWER -> "Low Power"
                        ScanMode.BALANCED -> "Balanced"
                        ScanMode.LOW_LATENCY -> "Low Latency"
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = if (selectedMode == mode) colors.primary.copy(alpha = 0.2f) else colors.surface,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        onClick = { onModeSelected(mode) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedMode == mode) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedMode == mode) colors.primary else colors.textSecondary,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
