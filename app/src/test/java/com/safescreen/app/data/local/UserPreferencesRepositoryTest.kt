package com.safescreen.app.data.local

import com.safescreen.app.data.local.datastore.UserPreferences
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPreferencesRepositoryTest {

    @Test
    fun testSha256Hashing() {
        val pin = "1234"
        val hash = UserPreferencesRepository.hashSha256(pin)
        // Standard SHA-256 of "1234"
        assertEquals("03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", hash)

        val wrongPin = "4321"
        val wrongHash = UserPreferencesRepository.hashSha256(wrongPin)
        assertNotEquals(hash, wrongHash)
    }

    @Test
    fun testDeterministicHashing() {
        val input = "SafeScreen2026"
        val hash1 = UserPreferencesRepository.hashSha256(input)
        val hash2 = UserPreferencesRepository.hashSha256(input)
        assertEquals(hash1, hash2)
    }

    @Test
    fun testDefaultPinVulnerabilityEliminated() {
        // When pinHash is blank, isPinSet must be false
        val defaultPrefs = UserPreferences(pinHash = "")
        assertFalse("PIN must not be reported as set when hash is blank", defaultPrefs.isPinSet)

        // When pin is enrolled, isPinSet must be true
        val enrolledPrefs = UserPreferences(pinHash = UserPreferencesRepository.hashSha256("2580"))
        assertTrue("PIN must be reported as set when valid hash is present", enrolledPrefs.isPinSet)
    }

    @Test
    fun testPinVerificationLogicWithEnrolledAndCustomHash() {
        val enrolledPin = "9876"
        val enrolledHash = UserPreferencesRepository.hashSha256(enrolledPin)

        val inputHash = UserPreferencesRepository.hashSha256("9876")
        val wrongHash = UserPreferencesRepository.hashSha256("1234")

        assertEquals(enrolledHash, inputHash)
        assertNotEquals(enrolledHash, wrongHash)
    }
}
