package com.safescreen.app.services

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.safescreen.app.MainActivity
import com.safescreen.app.R
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.services.audio.AudioPlaybackCaptureEngine
import com.safescreen.app.services.capture.MediaProjectionCaptureEngine
import com.safescreen.app.services.permission.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

    @Inject
    lateinit var captureEngine: MediaProjectionCaptureEngine

    @Inject
    lateinit var audioCaptureEngine: AudioPlaybackCaptureEngine

    @Inject
    lateinit var aiShieldCoordinator: com.safescreen.app.services.ai.AiShieldCoordinator

    private var isScreenReceiverRegistered = false
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen turned OFF. Pausing frame decimation to preserve battery.")
                    captureEngine.setScreenInteractive(false)
                    if (com.safescreen.app.services.overlay.LiveBlurOverlayManager.isShowing()) {
                        com.safescreen.app.services.overlay.LiveBlurOverlayManager.hideOverlay(this@ShieldProtectionService)
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen turned ON. Resuming AI frame capture.")
                    captureEngine.setScreenInteractive(true)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        captureEngine.onProjectionStoppedListener = {
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                if (_isServiceRunning.value) {
                    Log.w(TAG, "MediaProjection stopped; stopping shield and clearing active state")
                    stopForegroundShield()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        try {
            registerReceiver(screenStateReceiver, filter)
            isScreenReceiverRegistered = true
            Log.d(TAG, "ScreenStateReceiver registered for background battery preservation")
        } catch (e: Exception) {
            Log.e(TAG, "Failed registering ScreenStateReceiver: ${e.message}")
        }

        Log.d(TAG, "ShieldProtectionService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "ShieldProtectionService onStartCommand action=${intent?.action}")

        if (intent == null || intent.action == null) {
            Log.w(TAG, "ShieldProtectionService restarted with null intent/action (process restart). Gracefully stopping service to prevent MediaProjection token reuse exception.")
            stopForegroundShield()
            return START_NOT_STICKY
        }

        when (intent.action) {
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
            ACTION_TEST_VISUAL_THREAT -> {
                Log.d(TAG, "Triggering test visual threat")
                aiShieldCoordinator.testTriggerVisualThreat()
            }
            ACTION_TEST_AUDIO_THREAT -> {
                Log.d(TAG, "Triggering test audio threat")
                aiShieldCoordinator.testTriggerAudioThreat()
            }
            else -> {
                Log.w(TAG, "Ignoring unknown service action: ${intent.action}")
                stopForegroundShield()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundShield() {
        val resultCode = PermissionManager.mediaProjectionResultCode
        val resultData = PermissionManager.mediaProjectionResultData
        if (!PermissionManager.hasMediaProjectionConsent ||
            resultCode != Activity.RESULT_OK || resultData == null
        ) {
            Log.w(TAG, "Cannot activate shield without fresh MediaProjection consent")
            stopForegroundShield()
            return
        }

        try {
            val startingNotification = createNotification(
                title = "SafeScreen Starting",
                content = "Waiting for screen capture to initialize."
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var fgsType = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    // Android 14+ FGS Type requirements
                    fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
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
                    fgsType = fgsType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                }

                if (fgsType != 0) {
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, startingNotification, fgsType)
                } else {
                    startForeground(NOTIFICATION_ID, startingNotification)
                }
            } else {
                startForeground(NOTIFICATION_ID, startingNotification)
            }

            // Capture must be established before the service advertises itself as active.
            if (!captureEngine.startCapture(resultCode, resultData) || !captureEngine.isCapturing) {
                Log.e(TAG, "Screen capture failed to start; refusing active shield state")
                stopForegroundShield()
                return
            }
            val mediaProjectionSession = captureEngine.activeMediaProjection
            if (mediaProjectionSession == null) {
                Log.e(TAG, "MediaProjection session missing after capture startup")
                stopForegroundShield()
                return
            }
            _isServiceRunning.value = true

            // Start audio playback capture (16kHz Mono PCM for YAMNet) when permission allows.
            val isAudioGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (isAudioGranted) {
                audioCaptureEngine.startCapture(mediaProjectionSession)
            }

            // 3. Start Dual-Engine AI Shield live observation pipeline
            aiShieldCoordinator.startObserving()
            val activeNotification = createNotification(
                title = "SafeScreen Active",
                content = "On-device AI shield is actively guarding your screen & audio."
            )
            getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, activeNotification)
            serviceScope.launch { userPreferencesRepository.setShieldActive(true) }
            Log.i(TAG, "ShieldProtectionService active with MediaProjection capture")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start foreground service: ${e.message}", e)
            stopForegroundShield()
        }
    }

    private fun stopForegroundShield() {
        Log.d(TAG, "Stopping ShieldProtectionService")
        _isServiceRunning.value = false
        aiShieldCoordinator.stopObserving()
        audioCaptureEngine.stopCapture()
        captureEngine.stopCapture()
        PermissionManager.clearMediaProjectionConsent()
        stopForeground(STOP_FOREGROUND_REMOVE)
        serviceScope.launch { userPreferencesRepository.setShieldActive(false) }
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
        if (isScreenReceiverRegistered) {
            try {
                unregisterReceiver(screenStateReceiver)
                isScreenReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering ScreenStateReceiver: ${e.message}")
            }
        }
        captureEngine.onProjectionStoppedListener = null
        _isServiceRunning.value = false
        aiShieldCoordinator.stopObserving()
        audioCaptureEngine.stopCapture()
        captureEngine.stopCapture()
        PermissionManager.clearMediaProjectionConsent()
        serviceScope.launch { userPreferencesRepository.setShieldActive(false) }
        Log.d(TAG, "ShieldProtectionService destroyed")
    }

    companion object {
        private const val TAG = "ShieldProtectionService"
        const val CHANNEL_ID = "safescreen_protection_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SHIELD = "com.safescreen.app.action.START_SHIELD"
        const val ACTION_STOP_SHIELD = "com.safescreen.app.action.STOP_SHIELD"
        const val ACTION_TEST_VISUAL_THREAT = "com.safescreen.app.action.TEST_VISUAL_THREAT"
        const val ACTION_TEST_AUDIO_THREAT = "com.safescreen.app.action.TEST_AUDIO_THREAT"
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
