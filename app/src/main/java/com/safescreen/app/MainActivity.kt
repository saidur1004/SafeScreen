package com.safescreen.app

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.presentation.navigation.AppNavHost
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.services.ShieldProtectionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var whitelistDao: WhitelistDao

    @Inject
    lateinit var threatLogDao: ThreatLogDao

    @Inject
    lateinit var focusScheduleDao: FocusScheduleDao

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verify Room Database & DataStore initialization in background
        lifecycleScope.launch(Dispatchers.IO) {
            val exempted = whitelistDao.getExemptedPackageNames()
            val schedules = focusScheduleDao.getAllSchedulesFlow().first()
            val userPrefs = userPreferencesRepository.userPreferencesFlow.first()

            Log.d("SafeScreenDB", "Pre-populated Banking Apps (${exempted.size}): $exempted")
            Log.d("SafeScreenDB", "Pre-populated Focus Schedules (${schedules.size}): ${schedules.map { it.title }}")
            Log.d("SafeScreenDataStore", "Active UserPreferences: ShieldActive=${userPrefs.isShieldActive}, Visual=${userPrefs.isVisualShieldEnabled}, Audio=${userPrefs.isAudioMuteEnabled}, Reels=${userPrefs.isReelsBlockerEnabled}, Sensitivity=${userPrefs.aiSensitivity}, FPS=${userPrefs.performanceProfile}, BlurStyle=${userPrefs.blurStyle}")

            if (userPrefs.isShieldActive && userPrefs.hasCompletedOnboarding) {
                ShieldProtectionService.startService(this@MainActivity)
            }
        }

        setContent {
            val userPrefs by userPreferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(
                initialValue = null
            )

            SafeScreenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepBlack
                ) {
                    AppNavHost(
                        hasCompletedOnboarding = { userPrefs?.hasCompletedOnboarding ?: false },
                        isPinLockEnabled = { userPrefs?.isPinLockEnabled ?: false }
                    )
                }
            }
        }
    }
}

