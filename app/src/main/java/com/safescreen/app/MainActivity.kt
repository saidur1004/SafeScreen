package com.safescreen.app

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.presentation.navigation.AppNavHost
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.services.ShieldProtectionService
import com.safescreen.app.services.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var pendingProjectionRequest = false

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultData = result.data
        if (result.resultCode == Activity.RESULT_OK && resultData != null) {
            PermissionManager.setMediaProjectionResult(result.resultCode, resultData)
            lifecycleScope.launch {
                userPreferencesRepository.setShieldActive(true)
                ShieldProtectionService.startService(this@MainActivity, result.resultCode, resultData)
            }
        } else {
            PermissionManager.clearMediaProjectionConsent()
            lifecycleScope.launch {
                userPreferencesRepository.setShieldActive(false)
            }
        }
    }

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
                if (PermissionManager.hasMediaProjectionConsent &&
                    PermissionManager.mediaProjectionResultCode == Activity.RESULT_OK &&
                    PermissionManager.mediaProjectionResultData != null
                ) {
                    withContext(Dispatchers.Main) {
                        ShieldProtectionService.startService(
                            this@MainActivity,
                            PermissionManager.mediaProjectionResultCode,
                            PermissionManager.mediaProjectionResultData
                        )
                    }
                } else {
                    // A MediaProjection grant is process-bound. Do not retain a persisted "active" state
                    // after process recreation; ask the user for fresh OS consent first.
                    userPreferencesRepository.setShieldActive(false)
                    withContext(Dispatchers.Main) {
                        requestScreenCaptureConsent()
                    }
                }
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

    override fun onPostResume() {
        super.onPostResume()
        if (pendingProjectionRequest) {
            pendingProjectionRequest = false
            launchScreenCaptureConsent()
        }
    }

    private fun requestScreenCaptureConsent() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            launchScreenCaptureConsent()
        } else {
            pendingProjectionRequest = true
        }
    }

    private fun launchScreenCaptureConsent() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(manager.createScreenCaptureIntent())
    }
}

