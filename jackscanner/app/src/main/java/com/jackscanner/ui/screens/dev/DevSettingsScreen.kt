package com.jackscanner.ui.screens.dev

import androidx.compose.animation.analyzeAsColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme

@Composable
fun DevSettingsScreen(
    onBack: () -> Unit,
    viewModel: DevSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        // Dev Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔧 DEV CONTROL",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        DevBadge()
                    }
                    Text(
                        text = "Feature flags & developer tools",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Dev Account Section
            item {
                DevSectionHeader(title = "DEV ACCOUNT", icon = Icons.Default.AdminPanelSettings)
                GlassCard {
                    Column {
                        DevToggle(
                            title = "Developer Account",
                            subtitle = "Enable dev features & controls",
                            icon = Icons.Default.Badge,
                            checked = uiState.isDevAccount,
                            onCheckedChange = { viewModel.setDevAccount(it) },
                            enabled = true
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DevToggle(
                            title = "Show Dev Badge",
                            subtitle = "Display special badge next to your name",
                            icon = Icons.Default.Verified,
                            checked = uiState.devBadgeEnabled,
                            onCheckedChange = { viewModel.setDevBadgeEnabled(it) },
                            enabled = uiState.isDevAccount
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DevToggle(
                            title = "Colored Usernames",
                            subtitle = "Enable rainbow/gradient username colors",
                            icon = Icons.Default.Palette,
                            checked = uiState.coloredUsernamesEnabled,
                            onCheckedChange = { viewModel.setColoredUsernamesEnabled(it) },
                            enabled = uiState.isDevAccount
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DevToggle(
                            title = "Chat Boundaries",
                            subtitle = "Show special chat borders & containers",
                            icon = Icons.Default.BorderStyle,
                            checked = uiState.chatBoundariesEnabled,
                            onCheckedChange = { viewModel.setChatBoundariesEnabled(it) },
                            enabled = uiState.isDevAccount
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DevToggle(
                            title = "Special Fonts",
                            subtitle = "Enable custom font styles for dev",
                            icon = Icons.Default.FontDownload,
                            checked = uiState.specialFontsEnabled,
                            onCheckedChange = { viewModel.setSpecialFontsEnabled(it) },
                            enabled = uiState.isDevAccount
                        )
                    }
                }
            }

            // Feature Flags Section - Control what's available for ALL users
            item {
                DevSectionHeader(title = "FEATURE FLAGS", icon = Icons.Default.ToggleOn)
                Text(
                    text = "These flags control what's available to ALL users. Toggle off to disable features until ready.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                GlassCard {
                    Column {
                        FeatureFlagToggle(
                            title = "🗺️ Heatmap",
                            subtitle = "Community detection heatmap view",
                            checked = uiState.flagHeatmapEnabled,
                            onCheckedChange = { viewModel.setFlagHeatmapEnabled(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "💬 Community Chat",
                            subtitle = "Community discussion features",
                            checked = uiState.flagCommunityEnabled,
                            onCheckedChange = { viewModel.setFlagCommunityEnabled(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "🏆 Scoreboard",
                            subtitle = "User detection rankings",
                            checked = uiState.flagScoreboardEnabled,
                            onCheckedChange = { viewModel.setFlagScoreboardEnabled(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "🌐 Global Leaderboard",
                            subtitle = "Cross-user leaderboards (BETA)",
                            checked = uiState.flagLeaderboardGlobal,
                            onCheckedChange = { viewModel.setFlagLeaderboardGlobal(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "⚡ Detection Alerts",
                            subtitle = "Push notifications for detections",
                            checked = uiState.flagDetectionAlerts,
                            onCheckedChange = { viewModel.setFlagDetectionAlerts(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "📍 Location Tracking",
                            subtitle = "Record detection locations",
                            checked = uiState.flagLocationTracking,
                            onCheckedChange = { viewModel.setFlagLocationTracking(it) }
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        FeatureFlagToggle(
                            title = "💎 Premium Features",
                            subtitle = "Premium subscription features",
                            checked = uiState.flagPremiumFeatures,
                            onCheckedChange = { viewModel.setFlagPremiumFeatures(it) }
                        )
                    }
                }
            }

            // Danger Zone
            item {
                DevSectionHeader(title = "⚠️ DANGER ZONE", icon = Icons.Default.Warning)
                GlassCard {
                    Column {
                        DevActionButton(
                            title = "Reset All Feature Flags",
                            subtitle = "Reset all flags to default state",
                            icon = Icons.Default.RestartAlt,
                            color = colors.statusWarning
                        )
                        Divider(color = colors.border, modifier = Modifier.padding(vertical = 12.dp))
                        DevActionButton(
                            title = "Clear All User Data",
                            subtitle = "Delete all local data (cannot be undone)",
                            icon = Icons.Default.DeleteForever,
                            color = colors.statusDanger
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DevBadge() {
    val colors = BlueMeanieTheme.colors
    
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFFFFD700).copy(alpha = 0.2f)
    ) {
        Text(
            text = "DEV",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun DevSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.primary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun DevToggle(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) colors.primary else colors.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) colors.textPrimary else colors.textTertiary.copy(alpha = 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary.copy(alpha = if (enabled) 1f else 0.5f)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun FeatureFlagToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (checked) colors.statusActive.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (checked) colors.statusActive.copy(alpha = 0.3f) else colors.border,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                checkedThumbColor = colors.statusActive,
                checkedTrackColor = colors.statusActive.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

@Composable
private fun DevActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    val colors = BlueMeanieTheme.colors
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = color
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = color.copy(alpha = 0.5f)
        )
    }
}
