package com.jackscanner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Puzzle : Screen("puzzle", "Puzzle", Icons.Default.Build)
    object Heatmap : Screen("heatmap", "Heatmap", Icons.Default.Map)
    object Community : Screen("community", "Community", Icons.Default.People)
    object Scoreboard : Screen("scoreboard", "Scoreboard", Icons.Default.Star)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Home)
    object DetectionDetail : Screen("detection/{detectionId}", "Detection", Icons.Default.Home) {
        fun createRoute(detectionId: String) = "detection/$detectionId"
    }
    
    // Dev Screen - Hidden from main navigation, accessed via secret method
    object DevSettings : Screen("dev_settings", "Dev Settings", Icons.Default.Build) {
        fun createRoute() = "dev_settings"
    }
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Puzzle,
    Screen.Heatmap,
    Screen.Community,
    Screen.Scoreboard,
    Screen.Settings
)
