package com.jackscanner.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jackscanner.ui.screens.home.HomeScreen
import com.jackscanner.ui.screens.onboarding.OnboardingScreen
import com.jackscanner.ui.screens.settings.SettingsScreen
import com.jackscanner.ui.screens.settings.DetectionDetailScreen

@Composable
fun BlueMeanieNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
        popEnterTransition = { fadeIn(animationSpec = tween(300)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
        popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } } })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onDetectionClick = { detectionId -> navController.navigate(Screen.DetectionDetail.createRoute(detectionId)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onDevAccess = { navController.navigate("dev_settings") })
        }
        composable(route = "detection/{detectionId}", arguments = listOf(navArgument("detectionId") { type = NavType.StringType })) { backStackEntry ->
            DetectionDetailScreen(detectionId = backStackEntry.arguments?.getString("detectionId") ?: "", onBack = { navController.popBackStack() })
        }
        composable("dev_settings") {
            com.jackscanner.ui.screens.dev.DevSettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
