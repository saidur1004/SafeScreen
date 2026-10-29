package com.safescreen.app.domain.usecase

import android.content.Context
import android.content.Intent
import com.safescreen.app.services.ShieldProtectionService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopShieldUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val intent = Intent(context, ShieldProtectionService::class.java).apply {
            action = ShieldProtectionService.ACTION_STOP_SHIELD
        }
        context.stopService(intent)
    }
}

