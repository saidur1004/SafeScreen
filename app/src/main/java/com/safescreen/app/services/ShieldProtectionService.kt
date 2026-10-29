package com.safescreen.app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.safescreen.app.MainActivity
import com.safescreen.app.R
import com.safescreen.app.data.tflite.TFLiteClassifier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShieldProtectionService : LifecycleService() {

    @Inject
    lateinit var classifier: TFLiteClassifier

    private var isShieldActive = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START_SHIELD -> startShield()
            ACTION_STOP_SHIELD -> stopShield()
        }
        return START_STICKY
    }

    private fun startShield() {
        if (isShieldActive) return
        isShieldActive = true

        val notification = createNotification("SafeScreen Active", "Real-time AI screen & audio shield is active.")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopShield() {
        isShieldActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeScreen Protection Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when real-time content protection is active."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isShieldActive = false
    }

    companion object {
        const val CHANNEL_ID = "safescreen_protection_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_SHIELD = "com.safescreen.app.action.START_SHIELD"
        const val ACTION_STOP_SHIELD = "com.safescreen.app.action.STOP_SHIELD"
    }
}

