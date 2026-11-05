package com.safescreen.app.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.safescreen.app.data.local.datastore.PreferencesKeys
import com.safescreen.app.data.local.datastore.UserPreferences
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun pinCredentialUsesSaltedPbkdf2AndVerifiesConstantly() {
        val first = UserPreferencesRepository.createPinCredential("2580")
        val second = UserPreferencesRepository.createPinCredential("2580")

        assertTrue(first.startsWith("pbkdf2_sha256_v1$"))
        assertTrue(UserPreferencesRepository.verifyPinCredential("2580", first))
        assertFalse(UserPreferencesRepository.verifyPinCredential("1234", first))
        assertTrue("Random per-PIN salt should produce distinct credentials", first != second)
    }

    @Test
    fun legacySha256CredentialStillVerifiesForMigration() {
        val legacy = UserPreferencesRepository.hashSha256("9876")

        assertTrue(UserPreferencesRepository.verifyPinCredential("9876", legacy))
        assertFalse(UserPreferencesRepository.verifyPinCredential("1234", legacy))
    }

    @Test
    fun lockoutAttemptsAndExpiryAreStoredInDataStore() = runBlocking {
        val storeFile = File(temporaryFolder.root, "preferences.preferences_pb")
        val store = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { storeFile }
        )
        val repository = UserPreferencesRepository(store)
        val now = 10_000_000L

        repeat(4) {
            val state = repository.recordFailedPinAttempt(nowEpochMillis = now)
            assertEquals(it + 1, state.failedAttempts)
            assertEquals(0L, state.lockoutUntilEpochMillis)
        }
        val locked = repository.recordFailedPinAttempt(nowEpochMillis = now)
        assertEquals(5, locked.failedAttempts)
        assertEquals(now + 30_000L, locked.lockoutUntilEpochMillis)

        val restored = repository.getPinLockoutState(nowEpochMillis = now + 1_000L)
        assertEquals(5, restored.failedAttempts)
        assertEquals(now + 30_000L, restored.lockoutUntilEpochMillis)

        val expired = repository.getPinLockoutState(nowEpochMillis = now + 30_000L)
        assertEquals(0, expired.failedAttempts)
        assertEquals(0L, expired.lockoutUntilEpochMillis)
    }

    @Test
    fun legacyCredentialIsUpgradedAfterSuccessfulRepositoryVerification() = runBlocking {
        val store = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { File(temporaryFolder.root, "legacy.preferences_pb") }
        )
        val legacy = UserPreferencesRepository.hashSha256("4321")
        store.edit { it[PreferencesKeys.PIN_HASH_SHA256] = legacy }
        val repository = UserPreferencesRepository(store)

        assertTrue(repository.verifyPin("4321"))
        val upgraded = store.data.first()[PreferencesKeys.PIN_HASH_SHA256].orEmpty()
        assertTrue(upgraded.startsWith("pbkdf2_sha256_v1$"))
        assertTrue(repository.verifyPin("4321"))
    }

    @Test
    fun pinSetDefaultsRemainSecure() {
        assertFalse(UserPreferences(pinHash = "").isPinSet)
        assertTrue(UserPreferences(pinHash = "credential").isPinSet)
    }
}
