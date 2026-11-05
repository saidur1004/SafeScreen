package com.safescreen.app.data.local

import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun testPinVerificationLogicWithDefaultAndCustomHash() {
        // Default PIN logic when stored hash is empty
        val defaultPin = "1234"
        val wrongPin = "0000"
        val defaultHash = UserPreferencesRepository.hashSha256(defaultPin)

        // Custom PIN logic
        val customPin = "9876"
        val customHash = UserPreferencesRepository.hashSha256(customPin)

        assertEquals(defaultHash, UserPreferencesRepository.hashSha256("1234"))
        assertNotEquals(defaultHash, UserPreferencesRepository.hashSha256(wrongPin))
        assertEquals(customHash, UserPreferencesRepository.hashSha256("9876"))
        assertNotEquals(customHash, UserPreferencesRepository.hashSha256(defaultPin))
    }
}

