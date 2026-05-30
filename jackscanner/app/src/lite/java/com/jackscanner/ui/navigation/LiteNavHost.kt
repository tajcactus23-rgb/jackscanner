package com.jackscanner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jackscanner.ui.screens.home.HomeScreen

@Composable
fun LiteNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    onNavigateToSettings: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(
                onDetectionClick = { _ -> },
                onSettingsClick = onNavigateToSettings
            )
        }
    }
}
