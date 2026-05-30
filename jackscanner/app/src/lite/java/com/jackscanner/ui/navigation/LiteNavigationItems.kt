package com.jackscanner.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings

object LiteNavItems {
    val items = listOf(
        LiteNavItem(
            route = "home",
            title = "Scanner",
            icon = Icons.Default.Home
        ),
        LiteNavItem(
            route = "settings",
            title = "Settings", 
            icon = Icons.Default.Settings
        )
    )
}

data class LiteNavItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
