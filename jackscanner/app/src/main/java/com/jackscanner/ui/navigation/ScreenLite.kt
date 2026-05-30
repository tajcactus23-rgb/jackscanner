package com.jackscanner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class ScreenLite(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : ScreenLite("home", "Scanner", Icons.Default.Home)
    object Settings : ScreenLite("settings", "Settings", Icons.Default.Settings)

    object Onboarding : ScreenLite("onboarding", "Onboarding", Icons.Default.Home)
    object DetectionDetail : ScreenLite("detection/{detectionId}", "Detection", Icons.Default.Home) {
        fun createRoute(detectionId: String) = "detection/$detectionId"
    }
}

val bottomNavItemsLite = listOf(
    ScreenLite.Home,
    ScreenLite.Settings
)
