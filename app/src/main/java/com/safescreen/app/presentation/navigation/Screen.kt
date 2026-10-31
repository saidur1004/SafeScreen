package com.safescreen.app.presentation.navigation

/**
 * SafeScreen Navigation Routes
 *
 * Defines all 16 destination routes and dialog/sheet identifiers across the 8 functional modules.
 */
sealed class Screen(val route: String) {

    // Module 01: Onboarding Flow
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")

    // Module 02: Permissions Management
    data object PermissionHub : Screen("permission_hub")
    data object BatteryOptimizationDialog : Screen("battery_optimization_dialog")

    // Module 03: Core Home & Dashboard
    data object Home : Screen("home")
    data object BlurStylePreviewSheet : Screen("blur_style_preview_sheet")

    // Module 04: Reels & Digital Wellbeing
    data object ReelsControl : Screen("reels_control")
    data object FocusSchedule : Screen("focus_schedule")

    // Module 05: Analytics & Stats
    data object Stats : Screen("stats")
    data object DetailedActivityLog : Screen("detailed_activity_log")

    // Module 06: Settings & Security
    data object Settings : Screen("settings")
    data object AppWhitelist : Screen("app_whitelist")
    data object SecurityLock : Screen("security_lock")

    // Module 07: System Overlays (WindowManager Views)
    data object LiveBlurOverlay : Screen("live_blur_overlay")
    data object FocusInterventionOverlay : Screen("focus_intervention_overlay")

    // Module 08: Error, Empty & Fallback States
    data object EmptyAndErrorStates : Screen("empty_and_error_states")
}
