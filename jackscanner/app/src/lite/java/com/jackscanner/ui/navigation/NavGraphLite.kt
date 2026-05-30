package com.jackscanner.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jackscanner.ui.screens.home.HomeScreen
import com.jackscanner.ui.screens.settings.SettingsScreen
import com.jackscanner.ui.screens.settings.DetectionDetailScreen

@Composable
fun NavGraphLite(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "home"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        composable("home") {
            HomeScreen(
                onDetectionClick = { detectionId ->
                    navController.navigate("detection/$detectionId")
                }
            )
        }

        composable("settings") {
            SettingsScreenLite()
        }

        composable(
            route = "detection/{detectionId}",
            arguments = listOf(
                navArgument("detectionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val detectionId = backStackEntry.arguments?.getString("detectionId") ?: ""
            DetectionDetailScreen(
                detectionId = detectionId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
