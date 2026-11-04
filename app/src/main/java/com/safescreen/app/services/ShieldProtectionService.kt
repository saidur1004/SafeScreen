package com.safescreen.app.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.safescreen.app.MainActivity
import com.safescreen.app.R
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.services.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Foreground Service for SafeScreen persistent on-device content shielding.
 * Compliant with Android 14/15 Foreground Service types (mediaProjection, microphone, specialUse).
 * Stays persistent in background with a low-priority notification without crashing.
 */
@AndroidEntryPoint
class ShieldProtectionService : LifecycleService() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "ShieldProtectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "ShieldProtectionService onStartCommand action=${intent?.action}")

        when (intent?.action) {
            ACTION_START_SHIELD -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val resultData: Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(EXTRA_RESULT_DATA)
                }

                if (resultCode != 0 && resultData != null) {
                    PermissionManager.setMediaProjectionResult(resultCode, resultData)
                    Log.d(TAG, "MediaProjection token cached successfully in PermissionManager")
                }

                startForegroundShield()
            }
            ACTION_STOP_SHIELD -> {
                stopForegroundShield()
            }
        }
        return START_STICKY
    }

    private fun startForegroundShield() {
        _isServiceRunning.value = true
        val notification = createNotification(
            title = "SafeScreen Active",
            content = "On-device AI shield is actively guarding your screen & audio."
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var fgsType = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Android 14+ FGS Type requirements
                    if (PermissionManager.hasMediaProjectionConsent) {
                        fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    }
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    }
                    if (fgsType == 0) {
                        fgsType = ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    }
                    if (PermissionManager.hasMediaProjectionConsent) {
                        fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                    }
                }

                if (fgsType != 0) {
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, fgsType)
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "ShieldProtectionService running in foreground")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}", e)
        }
    }

    private fun stopForegroundShield() {
        Log.d(TAG, "Stopping ShieldProtectionService")
        _isServiceRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(title: String, content: String): Notification {
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, ShieldProtectionService::class.java).apply {
            action = ACTION_STOP_SHIELD
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Shield", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeScreen Protection Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent status while SafeScreen AI Shield is active."
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isServiceRunning.value = false
        Log.d(TAG, "ShieldProtectionService destroyed")
    }

    companion object {
        private const val TAG = "ShieldProtectionService"
        const val CHANNEL_ID = "safescreen_protection_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SHIELD = "com.safescreen.app.action.START_SHIELD"
        const val ACTION_STOP_SHIELD = "com.safescreen.app.action.STOP_SHIELD"
        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning = _isServiceRunning.asStateFlow()

        fun startService(context: Context, resultCode: Int = 0, resultData: Intent? = null) {
            val intent = Intent(context, ShieldProtectionService::class.java).apply {
                action = ACTION_START_SHIELD
                if (resultCode != 0 && resultData != null) {
                    putExtra(EXTRA_RESULT_CODE, resultCode)
                    putExtra(EXTRA_RESULT_DATA, resultData)
                }
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service: ${e.message}", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ShieldProtectionService::class.java).apply {
                action = ACTION_STOP_SHIELD
            }
            context.startService(intent)
        }
    }
}
