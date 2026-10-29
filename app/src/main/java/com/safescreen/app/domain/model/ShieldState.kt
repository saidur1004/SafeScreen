package com.safescreen.app.domain.model

sealed interface ShieldState {
    data object Inactive : ShieldState
    data object Active : ShieldState
    data class ThreatDetected(val message: String, val timestamp: Long = System.currentTimeMillis()) : ShieldState
    data object Paused : ShieldState
}

