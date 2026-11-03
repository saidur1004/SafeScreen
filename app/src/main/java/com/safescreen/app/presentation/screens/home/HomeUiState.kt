package com.safescreen.app.presentation.screens.home

/**
 * UI State for the Main Home Dashboard.
 */
data class HomeUiState(
    val isShieldActive: Boolean = true,
    val blurredTodayCount: Int = 38,
    val mutedTodayCount: Int = 14,
    val reelsMinutesUsed: Int = 18,
    val reelsMinutesLimit: Int = 30,
    val isVisualShieldEnabled: Boolean = true,
    val isAudioMuteEnabled: Boolean = true,
    val isReelsBlockerEnabled: Boolean = false,
    val activeBottomNav: BottomNavDestination = BottomNavDestination.Home
) {
    val reelsPercentage: Float
        get() = if (reelsMinutesLimit > 0) {
            (reelsMinutesUsed.toFloat() / reelsMinutesLimit.toFloat()).coerceIn(0f, 1f)
        } else 0f
}

/**
 * Bottom Navigation destinations available from the floating pill.
 */
enum class BottomNavDestination {
    Home,
    Stats,
    Focus,
    Settings
}

/**
 * User interactions and events dispatched from the Home Dashboard.
 */
sealed interface HomeUiEvent {
    data object ToggleShield : HomeUiEvent
    data class SetVisualShield(val enabled: Boolean) : HomeUiEvent
    data class SetAudioMute(val enabled: Boolean) : HomeUiEvent
    data class SetReelsBlocker(val enabled: Boolean) : HomeUiEvent
    data object NavigateToStats : HomeUiEvent
    data object NavigateToSettings : HomeUiEvent
    data object NavigateToFocus : HomeUiEvent
    data object NavigateToBlurPreview : HomeUiEvent
}
