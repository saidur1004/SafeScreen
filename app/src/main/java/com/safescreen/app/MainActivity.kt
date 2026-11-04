package com.safescreen.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.safescreen.app.data.local.dao.FocusScheduleDao
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.presentation.navigation.AppNavHost
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.SafeScreenTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var whitelistDao: WhitelistDao

    @Inject
    lateinit var threatLogDao: ThreatLogDao

    @Inject
    lateinit var focusScheduleDao: FocusScheduleDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verify Room Database initialization and pre-population in background
        lifecycleScope.launch(Dispatchers.IO) {
            val exempted = whitelistDao.getExemptedPackageNames()
            val schedules = focusScheduleDao.getAllSchedulesFlow().first()
            Log.d("SafeScreenDB", "Pre-populated Banking Apps (${exempted.size}): $exempted")
            Log.d("SafeScreenDB", "Pre-populated Focus Schedules (${schedules.size}): ${schedules.map { it.title }}")
        }

        setContent {
            SafeScreenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepBlack
                ) {
                    AppNavHost()
                }
            }
        }
    }
}

