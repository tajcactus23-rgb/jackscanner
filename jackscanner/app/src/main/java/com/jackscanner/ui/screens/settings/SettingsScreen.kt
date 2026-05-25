package com.jackscanner.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jackscanner.domain.model.AppTheme
import com.jackscanner.domain.model.ScanMode
import com.jackscanner.ui.components.GlassCard
import com.jackscanner.ui.theme.BlueMeanieTheme
import com.jackscanner.ui.theme.sp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = BlueMeanieTheme.colors
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header with Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.userName.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = uiState.userName.ifEmpty { "Anonymous" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = when {
                        uiState.isPremium -> "Premium Member"
                        uiState.privateMode -> "Private Mode"
                        else -> "Free Member"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary
                )
            }
            
            IconButton(onClick = { /* Edit profile */ }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = colors.textSecondary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Account Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Person,
                    title = "Account",
                    subtitle = "Profile, username, privacy settings",
                    onClick = { selectedCategory = "account" }
                )
            }
            
            // User Section  
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Face,
                    title = "User",
                    subtitle = "Avatar, display name, preferences",
                    onClick = { selectedCategory = "user" }
                )
            }
            
            // Appearance / Theme Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Palette,
                    title = "Appearance",
                    subtitle = "Themes: ${uiState.selectedTheme.displayName}",
                    onClick = { selectedCategory = "appearance" }
                )
            }
            
            // Scanner Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Radar,
                    title = "Scanner",
                    subtitle = "Scan mode, auto-start, alerts",
                    onClick = { selectedCategory = "scanner" }
                )
            }
            
            // Language Model (LLM) Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Psychology,
                    title = "Language Model (LLM)",
                    subtitle = "Configure AI model settings",
                    onClick = { selectedCategory = "llm" }
                )
            }
            
            // Condenser Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Compress,
                    title = "Condenser",
                    subtitle = "Data compression settings",
                    onClick = { selectedCategory = "condenser" }
                )
            }
            
            // Verification Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Verified,
                    title = "Verification",
                    subtitle = "Account verification status",
                    onClick = { selectedCategory = "verification" }
                )
            }
            
            // Agent Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.SmartToy,
                    title = "Agent",
                    subtitle = "AI agent configuration",
                    onClick = { selectedCategory = "agent" }
                )
            }
            
            // API Keys Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Key,
                    title = "API Keys",
                    subtitle = "Manage your API keys",
                    onClick = { selectedCategory = "api_keys" }
                )
            }
            
            // Secrets Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Lock,
                    title = "Secrets",
                    subtitle = "Secure storage for credentials",
                    onClick = { selectedCategory = "secrets" }
                )
            }
            
            // MCP Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Hub,
                    title = "MCP",
                    subtitle = "Model Context Protocol settings",
                    onClick = { selectedCategory = "mcp" }
                )
            }
            
            // Application Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Apps,
                    title = "Application",
                    subtitle = "App info, permissions, cache",
                    onClick = { selectedCategory = "application" }
                )
            }
            
            // Billing Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.CreditCard,
                    title = "Billing",
                    subtitle = if (uiState.isPremium) "Premium Active" else "Free Plan",
                    onClick = { selectedCategory = "billing" }
                )
            }
            
            // Integrations Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Link,
                    title = "Integrations",
                    subtitle = "Connect external services",
                    onClick = { selectedCategory = "integrations" }
                )
            }
            
            // Skills Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Stars,
                    title = "Skills",
                    subtitle = "Configure AI skills and plugins",
                    onClick = { selectedCategory = "skills" }
                )
            }
            
            // Notifications Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Alert preferences, sounds",
                    onClick = { selectedCategory = "notifications" }
                )
            }
            
            // Heatmap Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Map,
                    title = "Heatmap",
                    subtitle = "Map settings, filters, time range",
                    onClick = { selectedCategory = "heatmap" }
                )
            }
            
            // Community Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Groups,
                    title = "Community",
                    subtitle = "Chat, sharing, visibility",
                    onClick = { selectedCategory = "community" }
                )
            }
            
            // Privacy Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Security,
                    title = "Privacy",
                    subtitle = "Data, location, anonymous mode",
                    onClick = { selectedCategory = "privacy" }
                )
            }
            
            // Advanced Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Settings,
                    title = "Advanced",
                    subtitle = "Developer options, debugging",
                    onClick = { selectedCategory = "advanced" }
                )
            }
            
            // Documentation Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Menu,
                    title = "Documentation",
                    subtitle = "Help, guides, FAQ",
                    onClick = { selectedCategory = "documentation" }
                )
            }
            
            // About Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Info,
                    title = "About",
                    subtitle = "Version 2.0.0 • Axon Device Scanner",
                    onClick = { selectedCategory = "about" }
                )
            }
            
            // Logout Section
            item {
                SettingsCategoryItem(
                    icon = Icons.Default.Logout,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    isDestructive = true,
                    onClick = { viewModel.logout() }
                )
            }
            
            // Spacer for bottom nav
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SettingsCategoryItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) colors.statusDanger else colors.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) colors.statusDanger else colors.textPrimary
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
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
