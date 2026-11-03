package com.safescreen.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.safescreen.app.presentation.screens.focus.FocusScheduleScreen
import com.safescreen.app.presentation.screens.focus.ReelsControlScreen
import com.safescreen.app.presentation.screens.home.HomeScreen
import com.safescreen.app.presentation.screens.onboarding.OnboardingScreen
import com.safescreen.app.presentation.screens.permission.PermissionHubScreen
import com.safescreen.app.presentation.screens.settings.SettingsScreen
import com.safescreen.app.presentation.screens.splash.SplashScreen
import com.safescreen.app.presentation.screens.stats.StatsScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(Screen.PermissionHub.route)
                }
            )
        }

        composable(Screen.PermissionHub.route) {
            PermissionHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAllPermissionsGranted = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToStats = {
                    navController.navigate(Screen.Stats.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToFocus = {
                    navController.navigate(Screen.ReelsControl.route)
                }
            )
        }

        composable(Screen.ReelsControl.route) {
            ReelsControlScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSchedule = {
                    navController.navigate(Screen.FocusSchedule.route)
                }
            )
        }

        composable(Screen.FocusSchedule.route) {
            FocusScheduleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetailedLog = {
                    // Ready for Module 05 Prompt 2: DetailedActivityLogScreen
                    navController.navigate(Screen.DetailedActivityLog.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
