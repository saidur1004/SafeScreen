package com.safescreen.app.services.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.safescreen.app.MainActivity
import com.safescreen.app.R
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Handles device boot-up (BOOT_COMPLETED) and package update (MY_PACKAGE_REPLACED).
 * In accordance with Android 14+ (API 34/35) MediaProjection security regulations,
 * MediaProjection cannot be initiated without direct user interaction.
 *
 * If the user previously had the protection shield active, this receiver posts an
 * immediate high-priority alert notification prompting the user to re-arm the shield.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.i(TAG, "BootCompletedReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)

            scope.launch {
                try {
                    val prefs = userPreferencesRepository.userPreferencesFlow.first()
                    if (prefs.isShieldActive) {
                        Log.i(TAG, "Device rebooted with active shield preference. Posting reactivate prompt notification.")
                        showReactivateNotification(context)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking preferences on boot: ${e.message}", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private fun showReactivateNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SafeScreen Protection Status",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when device reboots to ensure continuous shield protection."
            }
            manager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setContentTitle("SafeScreen Protection Paused")
            .setContentText("Device restarted. Tap to reactivate your real-time AI shield.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
        private const val CHANNEL_ID = "safescreen_boot_channel"
        private const val NOTIFICATION_ID = 2001
        private const val REQUEST_CODE = 2002
    }
}

