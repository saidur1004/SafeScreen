package com.safescreen.app.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Type-safe preferences keys for Jetpack DataStore.
 * Centralized Single Source of Truth (SSOT) key definitions.
 */
object PreferencesKeys {

    // Master Shield & Sub-module Protection Switches
    val IS_SHIELD_ACTIVE = booleanPreferencesKey("is_shield_active")
    val IS_VISUAL_SHIELD_ENABLED = booleanPreferencesKey("is_visual_shield_enabled")
    val IS_AUDIO_MUTE_ENABLED = booleanPreferencesKey("is_audio_mute_enabled")
    val IS_REELS_BLOCKER_ENABLED = booleanPreferencesKey("is_reels_blocker_enabled")

    // AI & Performance Settings
    val AI_SENSITIVITY = stringPreferencesKey("ai_sensitivity") // "STRICT", "BALANCED", "RELAXED"
    val PERFORMANCE_PROFILE = stringPreferencesKey("performance_profile") // "BATTERY_SAVER", "BALANCED", "HIGH_PRECISION"

    // Blur Style & Visual Obfuscation
    val BLUR_STYLE = stringPreferencesKey("blur_style") // "FROSTED_GLASS", "PIXELATED", "OPAQUE"
    val BLUR_INTENSITY = floatPreferencesKey("blur_intensity") // 0.1f - 1.0f

    // Security & App Lock
    val IS_PIN_LOCK_ENABLED = booleanPreferencesKey("is_pin_lock_enabled")
    val PIN_HASH_SHA256 = stringPreferencesKey("pin_hash_sha256")
    val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
    val IS_PREVENT_UNINSTALL_ENABLED = booleanPreferencesKey("is_prevent_uninstall_enabled")

    // Reels & Digital Wellbeing
    val REELS_DAILY_LIMIT_MINUTES = intPreferencesKey("reels_daily_limit_minutes")
    val REELS_MINUTES_USED_TODAY = intPreferencesKey("reels_minutes_used_today")

    // Onboarding & Permissions Onboarding State
    val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    val HAS_ACCEPTED_BATTERY_OPTIMIZATION = booleanPreferencesKey("has_accepted_battery_optimization")
}
