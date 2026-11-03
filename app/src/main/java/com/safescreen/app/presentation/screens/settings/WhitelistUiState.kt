package com.safescreen.app.presentation.screens.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Category of installed application for whitelist recommendations.
 */
enum class AppCategory(val displayName: String) {
    ALL("All Apps"),
    BANKING("Banking & Finance"),
    COMMUNICATION("Communication"),
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity")
}

/**
 * Data model for an installed application in the Whitelist management list.
 */
@Immutable
data class WhitelistAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val category: AppCategory,
    val isWhitelisted: Boolean,
    val isFlagSecure: Boolean = false,
    val accentColor: Color = Color(0xFF00F0FF),
    val iconLetters: String = name.take(2).uppercase()
)

/**
 * UI State for AppWhitelistScreen.
 */
@Immutable
data class WhitelistUiState(
    val searchQuery: String = "",
    val selectedCategoryFilter: AppCategory = AppCategory.ALL,
    val apps: List<WhitelistAppItem> = defaultAppsList(),
    val isLoading: Boolean = false
) {
    val filteredApps: List<WhitelistAppItem>
        get() = apps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                    app.name.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategoryFilter == AppCategory.ALL || app.category == selectedCategoryFilter
            matchesSearch && matchesCategory
        }

    val whitelistedCount: Int
        get() = apps.count { it.isWhitelisted }

    val totalCount: Int
        get() = apps.size
}

/**
 * UI Events emitted from AppWhitelistScreen.
 */
sealed interface WhitelistUiEvent {
    data class UpdateSearchQuery(val query: String) : WhitelistUiEvent
    data class SelectCategoryFilter(val category: AppCategory) : WhitelistUiEvent
    data class ToggleAppWhitelist(val appId: String, val isWhitelisted: Boolean) : WhitelistUiEvent
    data object WhitelistAllBankingApps : WhitelistUiEvent
    data object ClearSearch : WhitelistUiEvent
    data object NavigateBack : WhitelistUiEvent
}

/**
 * Standard simulated realistic installed application list with 5 pre-whitelisted banking apps.
 */
fun defaultAppsList(): List<WhitelistAppItem> = listOf(
    WhitelistAppItem(
        id = "paypal",
        name = "PayPal",
        packageName = "com.paypal.android.p2pmobile",
        category = AppCategory.BANKING,
        isWhitelisted = true,
        isFlagSecure = true,
        accentColor = Color(0xFF0079C1),
        iconLetters = "PP"
    ),
    WhitelistAppItem(
        id = "bofa",
        name = "Bank of America",
        packageName = "com.infonow.bofa",
        category = AppCategory.BANKING,
        isWhitelisted = true,
        isFlagSecure = true,
        accentColor = Color(0xFFE31837),
        iconLetters = "BA"
    ),
    WhitelistAppItem(
        id = "gpay",
        name = "Google Pay & Wallet",
        packageName = "com.google.android.apps.walletnfcrel",
        category = AppCategory.BANKING,
        isWhitelisted = true,
        isFlagSecure = true,
        accentColor = Color(0xFF34A853),
        iconLetters = "GP"
    ),
    WhitelistAppItem(
        id = "chase",
        name = "Chase Mobile",
        packageName = "com.chase.sig.android",
        category = AppCategory.BANKING,
        isWhitelisted = true,
        isFlagSecure = true,
        accentColor = Color(0xFF117ACA),
        iconLetters = "CH"
    ),
    WhitelistAppItem(
        id = "revolut",
        name = "Revolut",
        packageName = "com.revolut.revolut",
        category = AppCategory.BANKING,
        isWhitelisted = true,
        isFlagSecure = true,
        accentColor = Color(0xFF0075FF),
        iconLetters = "RV"
    ),
    WhitelistAppItem(
        id = "chrome",
        name = "Google Chrome",
        packageName = "com.android.chrome",
        category = AppCategory.PRODUCTIVITY,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFFEA4335),
        iconLetters = "CR"
    ),
    WhitelistAppItem(
        id = "instagram",
        name = "Instagram",
        packageName = "com.instagram.android",
        category = AppCategory.ENTERTAINMENT,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFFE1306C),
        iconLetters = "IG"
    ),
    WhitelistAppItem(
        id = "youtube",
        name = "YouTube",
        packageName = "com.google.android.youtube",
        category = AppCategory.ENTERTAINMENT,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFFFF0000),
        iconLetters = "YT"
    ),
    WhitelistAppItem(
        id = "tiktok",
        name = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        category = AppCategory.ENTERTAINMENT,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFF00F2FE),
        iconLetters = "TT"
    ),
    WhitelistAppItem(
        id = "whatsapp",
        name = "WhatsApp Messenger",
        packageName = "com.whatsapp",
        category = AppCategory.COMMUNICATION,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFF25D366),
        iconLetters = "WA"
    ),
    WhitelistAppItem(
        id = "telegram",
        name = "Telegram",
        packageName = "org.telegram.messenger",
        category = AppCategory.COMMUNICATION,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFF24A1DE),
        iconLetters = "TG"
    ),
    WhitelistAppItem(
        id = "photos",
        name = "Google Photos",
        packageName = "com.google.android.apps.photos",
        category = AppCategory.PRODUCTIVITY,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFFFFBA08),
        iconLetters = "PH"
    ),
    WhitelistAppItem(
        id = "slack",
        name = "Slack",
        packageName = "com.Slack",
        category = AppCategory.COMMUNICATION,
        isWhitelisted = false,
        isFlagSecure = false,
        accentColor = Color(0xFF611F69),
        iconLetters = "SL"
    )
)

