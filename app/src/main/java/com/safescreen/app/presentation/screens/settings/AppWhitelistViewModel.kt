package com.safescreen.app.presentation.screens.settings

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.entity.WhitelistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Presentation ViewModel for App Whitelist Screen.
 * Loads installed launcher apps from PackageManager and persists whitelist choices in Room.
 */
@HiltViewModel
class AppWhitelistViewModel @Inject constructor(
    private val whitelistDao: WhitelistDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)

    init {
        refreshInstalledApps()
    }

    private fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                val installedApps = context.packageManager.queryIntentActivities(launcherIntent, 0)
                    .asSequence()
                    .mapNotNull { resolveInfo ->
                        val applicationInfo = resolveInfo.activityInfo?.applicationInfo ?: return@mapNotNull null
                        val packageName = applicationInfo.packageName ?: return@mapNotNull null
                        val appName = runCatching {
                            context.packageManager.getApplicationLabel(applicationInfo).toString()
                        }.getOrDefault(packageName)
                        Triple(packageName, appName, applicationInfo.category.toWhitelistCategory())
                    }
                    .distinctBy { it.first }
                    .filter { it.first != context.packageName }
                    .sortedBy { it.second.lowercase() }
                    .toList()

                // Do not prune saved entries if package visibility unexpectedly returns no apps.
                if (installedApps.isEmpty()) return@launch

                val previousByPackage = whitelistDao.getAllAppsFlow().first().associateBy { it.packageName }
                val newApps = installedApps.mapNotNull { (packageName, appName, category) ->
                    if (packageName in previousByPackage) {
                        whitelistDao.updateInstalledAppInfo(packageName, appName, category)
                        null
                    } else {
                        WhitelistEntity(
                            packageName = packageName,
                            appName = appName,
                            category = category,
                            isExempted = false,
                            isBankingDefault = false,
                            isFlagSecure = false
                        )
                    }
                }
                if (newApps.isNotEmpty()) whitelistDao.insertAppsIgnore(newApps)
                val installedPackages = installedApps.mapTo(HashSet()) { it.first }
                val removedApps = previousByPackage.values.filter { it.packageName !in installedPackages }
                if (removedApps.isNotEmpty()) whitelistDao.deleteApps(removedApps)
            } catch (_: Exception) {
                // Keep the last known whitelist if package enumeration or database sync fails.
            }
        }
    }

    private fun Int.toWhitelistCategory(): String = when (this) {
        ApplicationInfo.CATEGORY_GAME,
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE -> "ENTERTAINMENT"
        ApplicationInfo.CATEGORY_SOCIAL -> "COMMUNICATION"
        ApplicationInfo.CATEGORY_PRODUCTIVITY -> "PRODUCTIVITY"
        else -> "OTHER"
    }

    val uiState: StateFlow<WhitelistUiState> = combine(
        whitelistDao.getAllAppsFlow(),
        _searchQuery,
        _selectedCategory
    ) { dbEntities, liveQuery, selectedCategory ->
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
            else -> Unit
        }
    }

    private fun WhitelistEntity.toUiItem(): WhitelistAppItem {
        val appCat = when (category.uppercase()) {
            "COMMUNICATION" -> AppCategory.COMMUNICATION
            "ENTERTAINMENT" -> AppCategory.ENTERTAINMENT
            "PRODUCTIVITY" -> AppCategory.PRODUCTIVITY
            "OTHER" -> AppCategory.OTHER
            else -> AppCategory.OTHER
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

