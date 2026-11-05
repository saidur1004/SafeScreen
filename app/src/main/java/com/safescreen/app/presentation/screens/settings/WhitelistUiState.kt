package com.safescreen.app.presentation.screens.settings

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Category of installed application for whitelist recommendations.
 */
enum class AppCategory(val displayName: String) {
    ALL("All Apps"),
    COMMUNICATION("Communication"),
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    OTHER("Other")
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
    val apps: List<WhitelistAppItem> = emptyList(),
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
    data object ClearSearch : WhitelistUiEvent
    data object NavigateBack : WhitelistUiEvent
}

