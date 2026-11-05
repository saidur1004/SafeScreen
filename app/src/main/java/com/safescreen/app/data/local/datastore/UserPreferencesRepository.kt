package com.safescreen.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.safescreen.app.presentation.screens.home.BlurStyle
import com.safescreen.app.presentation.screens.settings.PerformanceProfile
import com.safescreen.app.presentation.screens.settings.SensitivityLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.SecureRandom
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single Source of Truth (SSOT) repository for user preferences, shield state,
 * security credentials, and AI performance configuration using Jetpack DataStore.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    data class PinLockoutState(val failedAttempts: Int, val lockoutUntilEpochMillis: Long)

    /**
     * Unified preferences flow emitting full state updates.
     */
    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            mapUserPreferences(preferences)
        }
        .distinctUntilChanged()

    // --- Granular Atomic Observers ---

    val isShieldActiveFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_SHIELD_ACTIVE] ?: true }
        .distinctUntilChanged()

    val isVisualShieldEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_VISUAL_SHIELD_ENABLED] ?: true }
        .distinctUntilChanged()

    val isAudioMuteEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_AUDIO_MUTE_ENABLED] ?: true }
        .distinctUntilChanged()

    val isReelsBlockerEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_REELS_BLOCKER_ENABLED] ?: false }
        .distinctUntilChanged()

    val aiSensitivityFlow: Flow<SensitivityLevel> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            val raw = preferences[PreferencesKeys.AI_SENSITIVITY]
            raw?.let { runCatching { SensitivityLevel.valueOf(it) }.getOrNull() }
                ?: SensitivityLevel.BALANCED
        }
        .distinctUntilChanged()

    val performanceProfileFlow: Flow<PerformanceProfile> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            val raw = preferences[PreferencesKeys.PERFORMANCE_PROFILE]
            raw?.let { runCatching { PerformanceProfile.valueOf(it) }.getOrNull() }
                ?: PerformanceProfile.BALANCED
        }
        .distinctUntilChanged()

    val blurStyleFlow: Flow<BlurStyle> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            val raw = preferences[PreferencesKeys.BLUR_STYLE]
            raw?.let { runCatching { BlurStyle.valueOf(it) }.getOrNull() }
                ?: BlurStyle.FROSTED_GLASS
        }
        .distinctUntilChanged()

    val blurIntensityFlow: Flow<Float> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.BLUR_INTENSITY] ?: 0.85f }
        .distinctUntilChanged()

    val isPinEnrolledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { (it[PreferencesKeys.PIN_HASH_SHA256] ?: "").isNotBlank() }
        .distinctUntilChanged()

    val isPinLockEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_PIN_LOCK_ENABLED] ?: false }
        .distinctUntilChanged()

    val isBiometricEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: true }
        .distinctUntilChanged()

    val isPreventUninstallEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.IS_PREVENT_UNINSTALL_ENABLED] ?: true }
        .distinctUntilChanged()

    val reelsDailyLimitMinutesFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.REELS_DAILY_LIMIT_MINUTES] ?: 30 }
        .distinctUntilChanged()

    val reelsMinutesUsedTodayFlow: Flow<Int> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.REELS_MINUTES_USED_TODAY] ?: 0 }
        .distinctUntilChanged()

    val reelsInterventionStrategyFlow: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.REELS_INTERVENTION_STRATEGY] ?: "HARD_LOCKOUT" }
        .distinctUntilChanged()

    val reelsManagedAppsJsonFlow: Flow<String> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.REELS_MANAGED_APPS_JSON] ?: "" }
        .distinctUntilChanged()

    val hasCompletedOnboardingFlow: Flow<Boolean> = dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false }
        .distinctUntilChanged()

    // --- Mutators & Setters ---

    suspend fun setShieldActive(active: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SHIELD_ACTIVE] = active
        }
    }

    suspend fun setVisualShieldEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_VISUAL_SHIELD_ENABLED] = enabled
        }
    }

    suspend fun setAudioMuteEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AUDIO_MUTE_ENABLED] = enabled
        }
    }

    suspend fun setReelsBlockerEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_REELS_BLOCKER_ENABLED] = enabled
        }
    }

    suspend fun setAiSensitivity(level: SensitivityLevel) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AI_SENSITIVITY] = level.name
        }
    }

    suspend fun setPerformanceProfile(profile: PerformanceProfile) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERFORMANCE_PROFILE] = profile.name
        }
    }

    suspend fun setBlurStyle(style: BlurStyle, intensity: Float? = null) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUR_STYLE] = style.name
            if (intensity != null) {
                preferences[PreferencesKeys.BLUR_INTENSITY] = intensity.coerceIn(0.1f, 1.0f)
            }
        }
    }

    suspend fun setBlurIntensity(intensity: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BLUR_INTENSITY] = intensity.coerceIn(0.1f, 1.0f)
        }
    }

    suspend fun setPin(pin: String, enableLock: Boolean = true) {
        val credential = withContext(Dispatchers.Default) { createPinCredential(pin) }
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_HASH_SHA256] = credential
            preferences[PreferencesKeys.IS_PIN_LOCK_ENABLED] = enableLock
            preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] = 0
            preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] = 0L
        }
    }

    suspend fun setPinLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            val hash = preferences[PreferencesKeys.PIN_HASH_SHA256] ?: ""
            if (enabled && hash.isBlank()) {
                // Security Hardening: Cannot enable PIN lock without prior mandatory enrollment
                return@edit
            }
            preferences[PreferencesKeys.IS_PIN_LOCK_ENABLED] = enabled
        }
    }

    suspend fun clearPin() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PIN_HASH_SHA256)
            preferences[PreferencesKeys.IS_PIN_LOCK_ENABLED] = false
            preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] = 0
            preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] = 0L
        }
    }

    suspend fun verifyPin(pinInput: String, storedPreferences: UserPreferences? = null): Boolean {
        val expectedHash = storedPreferences?.pinHash
            ?: dataStore.data.first()[PreferencesKeys.PIN_HASH_SHA256] ?: ""

        // Security Hardening: Default PIN vulnerability removed.
        // A PIN cannot verify unless a valid hash has been explicitly enrolled by the user.
        if (expectedHash.isBlank()) {
            return false
        }
        val valid = withContext(Dispatchers.Default) { verifyPinCredential(pinInput, expectedHash) }
        if (valid && !expectedHash.startsWith(PIN_CREDENTIAL_PREFIX)) {
            val upgraded = withContext(Dispatchers.Default) { createPinCredential(pinInput) }
            dataStore.edit { preferences ->
                // Avoid overwriting a credential that was changed during verification.
                if (preferences[PreferencesKeys.PIN_HASH_SHA256] == expectedHash) {
                    preferences[PreferencesKeys.PIN_HASH_SHA256] = upgraded
                }
            }
        }
        return valid
    }

    suspend fun getPinLockoutState(nowEpochMillis: Long = System.currentTimeMillis()): PinLockoutState {
        val preferences = dataStore.data.first()
        val attempts = preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] ?: 0
        val until = preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] ?: 0L
        if (until != 0L && until <= nowEpochMillis) {
            clearPinLockout()
            return PinLockoutState(failedAttempts = 0, lockoutUntilEpochMillis = 0L)
        }
        return PinLockoutState(attempts, until)
    }

    suspend fun recordFailedPinAttempt(
        nowEpochMillis: Long = System.currentTimeMillis(),
        maxAttempts: Int = 5,
        lockoutDurationMillis: Long = 30_000L
    ): PinLockoutState {
        var result = PinLockoutState(0, 0L)
        dataStore.edit { preferences ->
            val oldUntil = preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] ?: 0L
            val oldAttempts = preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] ?: 0
            val currentAttempts = if (oldUntil != 0L && oldUntil <= nowEpochMillis) 0 else oldAttempts
            val attempts = currentAttempts + 1
            val until = if (attempts >= maxAttempts) nowEpochMillis + lockoutDurationMillis else 0L
            preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] = attempts
            preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] = until
            result = PinLockoutState(attempts, until)
        }
        return result
    }

    suspend fun clearPinLockout() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_FAILED_ATTEMPTS] = 0
            preferences[PreferencesKeys.PIN_LOCKOUT_UNTIL_EPOCH_MS] = 0L
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setPreventUninstallEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_PREVENT_UNINSTALL_ENABLED] = enabled
        }
    }

    suspend fun setReelsDailyLimitMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REELS_DAILY_LIMIT_MINUTES] = minutes.coerceIn(5, 240)
        }
    }

    suspend fun setReelsMinutesUsedToday(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REELS_MINUTES_USED_TODAY] = minutes.coerceAtLeast(0)
        }
    }

    suspend fun setReelsInterventionStrategy(strategy: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REELS_INTERVENTION_STRATEGY] = strategy
        }
    }

    suspend fun setReelsManagedAppsJson(json: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REELS_MANAGED_APPS_JSON] = json
        }
    }

    suspend fun incrementReelsMinutesUsedToday(minutes: Int = 1) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.REELS_MINUTES_USED_TODAY] ?: 0
            preferences[PreferencesKeys.REELS_MINUTES_USED_TODAY] = current + minutes
        }
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setBatteryOptimizationAccepted(accepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_ACCEPTED_BATTERY_OPTIMIZATION] = accepted
        }
    }

    // --- Private Helpers ---

    private fun mapUserPreferences(preferences: Preferences): UserPreferences {
        val sensitivity = preferences[PreferencesKeys.AI_SENSITIVITY]?.let {
            runCatching { SensitivityLevel.valueOf(it) }.getOrNull()
        } ?: SensitivityLevel.BALANCED

        val profile = preferences[PreferencesKeys.PERFORMANCE_PROFILE]?.let {
            runCatching { PerformanceProfile.valueOf(it) }.getOrNull()
        } ?: PerformanceProfile.BALANCED

        val blur = preferences[PreferencesKeys.BLUR_STYLE]?.let {
            runCatching { BlurStyle.valueOf(it) }.getOrNull()
        } ?: BlurStyle.FROSTED_GLASS

        return UserPreferences(
            isShieldActive = preferences[PreferencesKeys.IS_SHIELD_ACTIVE] ?: true,
            isVisualShieldEnabled = preferences[PreferencesKeys.IS_VISUAL_SHIELD_ENABLED] ?: true,
            isAudioMuteEnabled = preferences[PreferencesKeys.IS_AUDIO_MUTE_ENABLED] ?: true,
            isReelsBlockerEnabled = preferences[PreferencesKeys.IS_REELS_BLOCKER_ENABLED] ?: false,
            aiSensitivity = sensitivity,
            performanceProfile = profile,
            blurStyle = blur,
            blurIntensity = preferences[PreferencesKeys.BLUR_INTENSITY] ?: 0.85f,
            isPinLockEnabled = preferences[PreferencesKeys.IS_PIN_LOCK_ENABLED] ?: false,
            pinHash = preferences[PreferencesKeys.PIN_HASH_SHA256] ?: "",
            isBiometricEnabled = preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: true,
            isPreventUninstallEnabled = preferences[PreferencesKeys.IS_PREVENT_UNINSTALL_ENABLED] ?: true,
            reelsDailyLimitMinutes = preferences[PreferencesKeys.REELS_DAILY_LIMIT_MINUTES] ?: 30,
            reelsMinutesUsedToday = preferences[PreferencesKeys.REELS_MINUTES_USED_TODAY] ?: 0,
            reelsInterventionStrategy = preferences[PreferencesKeys.REELS_INTERVENTION_STRATEGY] ?: "HARD_LOCKOUT",
            reelsManagedAppsJson = preferences[PreferencesKeys.REELS_MANAGED_APPS_JSON] ?: "",
            hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false,
            hasAcceptedBatteryOptimization = preferences[PreferencesKeys.HAS_ACCEPTED_BATTERY_OPTIMIZATION] ?: false
        )
    }

    companion object {
        private const val PIN_CREDENTIAL_PREFIX = "pbkdf2_sha256_v1"
        const val PBKDF2_ITERATIONS = 210_000
        private const val PIN_SALT_BYTES = 16
        private const val PIN_KEY_BITS = 256

        fun hashSha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun createPinCredential(pin: String, salt: ByteArray = ByteArray(PIN_SALT_BYTES).also(SecureRandom()::nextBytes)): String {
            val derived = derivePinKey(pin, salt, PBKDF2_ITERATIONS)
            return "$PIN_CREDENTIAL_PREFIX\$$PBKDF2_ITERATIONS\$${salt.toHex()}\$${derived.toHex()}"
        }

        fun verifyPinCredential(pin: String, credential: String): Boolean {
            if (!credential.startsWith("$PIN_CREDENTIAL_PREFIX\$")) {
                // Compatibility path for existing credentials; successful verification upgrades them.
                return MessageDigest.isEqual(
                    hashSha256(pin).toByteArray(Charsets.UTF_8),
                    credential.toByteArray(Charsets.UTF_8)
                )
            }
            val parts = credential.split('$')
            if (parts.size != 4) return false
            val iterations = parts[1].toIntOrNull() ?: return false
            if (iterations !in 100_000..1_000_000) return false
            val salt = parts[2].fromHex() ?: return false
            val expected = parts[3].fromHex() ?: return false
            val actual = derivePinKey(pin, salt, iterations)
            return MessageDigest.isEqual(actual, expected)
        }

        private fun derivePinKey(pin: String, salt: ByteArray, iterations: Int): ByteArray {
            val spec = PBEKeySpec(pin.toCharArray(), salt, iterations, PIN_KEY_BITS)
            return try {
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).encoded
            } finally {
                spec.clearPassword()
            }
        }

        private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

        private fun String.fromHex(): ByteArray? {
            if (length % 2 != 0) return null
            return runCatching { chunked(2).map { it.toInt(16).toByte() }.toByteArray() }.getOrNull()
        }
    }
}

