package com.safescreen.app.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object PermissionHub : Screen("permission_hub")
    data object Home : Screen("home")
    data object Stats : Screen("stats")
    data object Settings : Screen("settings")
}

