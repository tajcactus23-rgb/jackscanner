package com.jackscanner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Scanner", Icons.Default.Home)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Home)
    object DetectionDetail : Screen("detection/{detectionId}", "Detection", Icons.Default.Home) {
        fun createRoute(detectionId: String) = "detection/$detectionId"
    }
    object DevSettings : Screen("dev_settings", "Dev Settings", Icons.Default.Settings) {
        fun createRoute() = "dev_settings"
    }
}

val bottomNavItems = listOf(Screen.Home, Screen.Settings)
