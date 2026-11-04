package com.safescreen.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.safescreen.app.presentation.components.BottomNavDestination
import com.safescreen.app.presentation.components.GlobalFloatingBottomBar
import com.safescreen.app.presentation.screens.focus.FocusScheduleScreen
import com.safescreen.app.presentation.screens.focus.ReelsControlScreen
import com.safescreen.app.presentation.screens.home.HomeScreen
import com.safescreen.app.presentation.screens.onboarding.OnboardingScreen
import com.safescreen.app.presentation.screens.permission.PermissionHubScreen
import com.safescreen.app.presentation.screens.settings.AppWhitelistScreen
import com.safescreen.app.presentation.screens.settings.SecurityLockScreen
import com.safescreen.app.presentation.screens.settings.SettingsScreen
import com.safescreen.app.presentation.screens.splash.SplashScreen
import com.safescreen.app.presentation.screens.states.EmptyAndErrorStatesScreen
import com.safescreen.app.presentation.screens.stats.DetailedActivityLogScreen
import com.safescreen.app.presentation.screens.stats.StatsScreen
import com.safescreen.app.services.overlay.FocusInterventionContent
import com.safescreen.app.services.overlay.LiveBlurOverlayContent
import com.safescreen.app.presentation.theme.DeepBlack

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    hasCompletedOnboarding: () -> Boolean = { false }
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabs = listOf(
        Screen.Home.route,
        Screen.Stats.route,
        Screen.FocusSchedule.route,
        Screen.Settings.route
    )

    // Define the 4 primary persistent tabs
    val isMainTab = currentRoute in mainTabs

    val activeDestination = when (currentRoute) {
        Screen.Home.route -> BottomNavDestination.Home
        Screen.Stats.route -> BottomNavDestination.Stats
        Screen.FocusSchedule.route -> BottomNavDestination.Focus
        Screen.Settings.route -> BottomNavDestination.Settings
        else -> BottomNavDestination.Home
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val fromTab = initialState.destination.route in mainTabs
                val toTab = targetState.destination.route in mainTabs
                if (fromTab && toTab) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280)
                    )
                }
            },
            exitTransition = {
                val fromTab = initialState.destination.route in mainTabs
                val toTab = targetState.destination.route in mainTabs
                if (fromTab && toTab) {
                    fadeOut(animationSpec = tween(220))
                } else {
                    fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(280)
                    )
                }
            },
            popEnterTransition = {
                val fromTab = initialState.destination.route in mainTabs
                val toTab = targetState.destination.route in mainTabs
                if (fromTab && toTab) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    fadeIn(animationSpec = tween(280)) + slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280)
                    )
                }
            },
            popExitTransition = {
                val fromTab = initialState.destination.route in mainTabs
                val toTab = targetState.destination.route in mainTabs
                if (fromTab && toTab) {
                    fadeOut(animationSpec = tween(220))
                } else {
                    fadeOut(animationSpec = tween(280)) + slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(280)
                    )
                }
            }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateNext = {
                        val destination = if (hasCompletedOnboarding()) Screen.Home.route else Screen.Onboarding.route
                        navController.navigate(destination) {
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
                            popUpTo(Screen.PermissionHub.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToStats = {
                        navController.navigate(Screen.Stats.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFocus = {
                        navController.navigate(Screen.FocusSchedule.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToStates = {
                        navController.navigate(Screen.EmptyAndErrorStates.route)
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
                        navController.navigate(Screen.DetailedActivityLog.route)
                    }
                )
            }

            composable(Screen.DetailedActivityLog.route) {
                DetailedActivityLogScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToWhitelist = {
                        navController.navigate(Screen.AppWhitelist.route)
                    },
                    onNavigateToSecurityLock = {
                        navController.navigate(Screen.SecurityLock.route)
                    }
                )
            }

            composable(Screen.AppWhitelist.route) {
                AppWhitelistScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.SecurityLock.route) {
                SecurityLockScreen(
                    onUnlockSuccess = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.LiveBlurOverlay.route) {
                LiveBlurOverlayContent(
                    onDismiss = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.FocusInterventionOverlay.route) {
                FocusInterventionContent(
                    onExitToHomeClick = {
                        navController.popBackStack()
                    },
                    onDismiss = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.EmptyAndErrorStates.route) {
                EmptyAndErrorStatesScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Global Cyber-Dark Glassmorphic Floating Bottom Bar
        GlobalFloatingBottomBar(
            visible = isMainTab,
            activeDestination = activeDestination,
            onDestinationSelected = { destination ->
                val targetRoute = when (destination) {
                    BottomNavDestination.Home -> Screen.Home.route
                    BottomNavDestination.Stats -> Screen.Stats.route
                    BottomNavDestination.Focus -> Screen.FocusSchedule.route
                    BottomNavDestination.Settings -> Screen.Settings.route
                }
                if (currentRoute != targetRoute) {
                    navController.navigate(targetRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
