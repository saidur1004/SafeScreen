package com.safescreen.app.presentation.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.entity.WhitelistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation ViewModel for App Whitelist Screen.
 * Fully dynamic: Reads/writes directly to Room DB WhitelistDao with 300ms debounced search.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class AppWhitelistViewModel @Inject constructor(
    private val whitelistDao: WhitelistDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)

    // Debounce search query by 300ms to eliminate DB churn during rapid typing
    private val debouncedSearchQuery = _searchQuery.debounce(300)

    val uiState: StateFlow<WhitelistUiState> = combine(
        whitelistDao.getAllAppsFlow(),
        _searchQuery,
        debouncedSearchQuery,
        _selectedCategory
    ) { dbEntities, liveQuery, debouncedQuery, selectedCategory ->
        val appItems = dbEntities.map { entity ->
            entity.toUiItem()
        }

        WhitelistUiState(
            searchQuery = liveQuery,
            selectedCategoryFilter = selectedCategory,
            apps = appItems,
            isLoading = false
        )
    }.catch { error ->
        emit(
            WhitelistUiState(
                apps = emptyList(),
                isLoading = false
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WhitelistUiState(isLoading = true)
    )

    fun onEvent(event: WhitelistUiEvent) {
        when (event) {
            is WhitelistUiEvent.UpdateSearchQuery -> {
                _searchQuery.value = event.query
            }
            is WhitelistUiEvent.ClearSearch -> {
                _searchQuery.value = ""
            }
            is WhitelistUiEvent.SelectCategoryFilter -> {
                _selectedCategory.value = event.category
            }
            is WhitelistUiEvent.ToggleAppWhitelist -> {
                viewModelScope.launch {
                    whitelistDao.updateExemptionStatus(
                        packageName = event.appId,
                        isExempted = event.isWhitelisted
                    )
                }
            }
            is WhitelistUiEvent.WhitelistAllBankingApps -> {
                viewModelScope.launch {
                    val allApps = whitelistDao.getAllAppsFlow().first()
                    allApps.filter { it.category.equals("BANKING", ignoreCase = true) }
                        .forEach { app ->
                            whitelistDao.updateExemptionStatus(app.packageName, true)
                        }
                }
            }
            else -> Unit
        }
    }

    private fun WhitelistEntity.toUiItem(): WhitelistAppItem {
        val appCat = when (category.uppercase()) {
            "BANKING" -> AppCategory.BANKING
            "COMMUNICATION" -> AppCategory.COMMUNICATION
            "ENTERTAINMENT" -> AppCategory.ENTERTAINMENT
            "PRODUCTIVITY" -> AppCategory.PRODUCTIVITY
            else -> AppCategory.ALL
        }

        return WhitelistAppItem(
            id = packageName,
            name = appName,
            packageName = packageName,
            category = appCat,
            isWhitelisted = isExempted,
            isFlagSecure = isFlagSecure,
            accentColor = Color(accentColorHex),
            iconLetters = appName.take(2).uppercase()
        )
    }
}
