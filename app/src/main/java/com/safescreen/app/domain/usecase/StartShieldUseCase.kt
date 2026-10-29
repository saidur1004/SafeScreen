package com.safescreen.app.domain.usecase

import android.content.Context
import android.content.Intent
import android.os.Build
import com.safescreen.app.services.ShieldProtectionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartShieldUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val intent = Intent(context, ShieldProtectionService::class.java).apply {
            action = ShieldProtectionService.ACTION_START_SHIELD
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

