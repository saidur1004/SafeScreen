package com.safescreen.app.presentation.screens.focus

/**
 * Supported or recognized visual brand icon styles.
 */
enum class AppBrandType {
    TIKTOK,
    INSTAGRAM,
    YOUTUBE,
    FACEBOOK,
    REDDIT,
    TWITTER_X,
    SNAPCHAT,
    NETFLIX,
    GENERIC
}

/**
 * Model representing a social/entertainment application under screen-time surveillance.
 */
data class ManagedAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val timeLimitMinutes: Int = 60,
    val isEnabled: Boolean = true,
    val brandType: AppBrandType = AppBrandType.GENERIC
)

/**
 * Methods by which SafeScreen intervenes when a daily limit is breached.
 */
enum class InterventionStrategy(
    val title: String,
    val subtitle: String
) {
    HARD_LOCKOUT("Hard Lockout", "Shows a full-screen focus intervention")
}

/**
 * UI State for the Reels & Digital Wellbeing Control Screen.
 */
data class ReelsUiState(
    val isStrictModeEnabled: Boolean = false,
    val managedApps: List<ManagedAppItem> = defaultManagedApps,
    val selectedStrategy: InterventionStrategy = InterventionStrategy.HARD_LOCKOUT,
    val showAddAppSheet: Boolean = false,
    val availableAppsToAdd: List<ManagedAppItem> = defaultAvailableApps,
    val searchQuery: String = ""
)

/**
 * Initial standard social applications displayed in the management feed.
 */
val defaultManagedApps = listOf(
    ManagedAppItem(
        id = "tiktok",
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        timeLimitMinutes = 90,
        brandType = AppBrandType.TIKTOK
    ),
    ManagedAppItem(
        id = "instagram",
        name = "Instagram",
        packageName = "com.instagram.android",
        timeLimitMinutes = 60,
        brandType = AppBrandType.INSTAGRAM
    ),
    ManagedAppItem(
        id = "youtube",
        name = "YouTube",
        packageName = "com.google.android.youtube",
        timeLimitMinutes = 120,
        brandType = AppBrandType.YOUTUBE
    )
)

/**
 * Initial catalog of installed apps available to be added.
 */
val defaultAvailableApps = listOf(
    ManagedAppItem(
        id = "facebook",
        name = "Facebook",
        packageName = "com.facebook.katana",
        timeLimitMinutes = 45,
        brandType = AppBrandType.FACEBOOK
    )
)

/**
 * Events and intent dispatches emitted from ReelsControlScreen.
 */
sealed interface ReelsUiEvent {
    data object ToggleStrictMode : ReelsUiEvent
    data class UpdateAppTimeLimit(val appId: String, val minutes: Int) : ReelsUiEvent
    data class ToggleAppEnabled(val appId: String, val enabled: Boolean) : ReelsUiEvent
    data class SelectStrategy(val strategy: InterventionStrategy) : ReelsUiEvent
    data object OpenAddAppSheet : ReelsUiEvent
    data object DismissAddAppSheet : ReelsUiEvent
    data class AddApp(val app: ManagedAppItem) : ReelsUiEvent
    data class UpdateSearchQuery(val query: String) : ReelsUiEvent
    data object NavigateBack : ReelsUiEvent
}
