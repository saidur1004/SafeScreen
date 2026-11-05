package com.safescreen.app.services.ai

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.util.Log
import com.safescreen.app.data.ai.audio.YAMNetAudioClassifier
import com.safescreen.app.data.ai.vision.TFLiteVisionClassifier
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.dao.WhitelistDao
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.data.local.entity.ThreatAction
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.ThreatType
import com.safescreen.app.services.audio.AudioPlaybackCaptureEngine
import com.safescreen.app.services.capture.MediaProjectionCaptureEngine
import com.safescreen.app.services.overlay.LiveBlurOverlayManager
import com.safescreen.app.services.tracker.ForegroundAppTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Real-time telemetry data model for UI dashboards and notification updates.
 */
data class LiveThreatTelemetry(
    val isShieldActive: Boolean = true,
    val isVisualThreat: Boolean = false,
    val visualConfidence: Float = 0f,
    val visualLabel: String = "Clean",
    val isAudioThreat: Boolean = false,
    val audioConfidence: Float = 0f,
    val audioCategory: String = "Clean",
    val lastEvaluationTime: Long = 0L,
    val totalThreatsIntercepted: Int = 0
)

/**
 * Dual-Engine AI Shield Coordinator:
 * Orchestrates live frame streaming from [MediaProjectionCaptureEngine] to [TFLiteVisionClassifier]
 * and live audio streaming from [AudioPlaybackCaptureEngine] to [YAMNetAudioClassifier].
 *
 * Implements:
 * 1. Zero-lag real-time inference on Dispatchers.Default.
 * 2. Instant defensive interventions: WindowManager Live Blur overlay & audio mute.
 * 3. On-device local audit logging in Room SQLite database.
 * 4. Reactive whitelist verification to exempt banking/confidential applications.
 */
@Singleton
class AiShieldCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val visionClassifier: TFLiteVisionClassifier,
    private val audioClassifier: YAMNetAudioClassifier,
    private val captureEngine: MediaProjectionCaptureEngine,
    private val audioCaptureEngine: AudioPlaybackCaptureEngine,
    private val threatLogDao: ThreatLogDao,
    private val whitelistDao: WhitelistDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val foregroundAppTracker: ForegroundAppTracker
) {

    private val coordinatorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _telemetry = MutableStateFlow(LiveThreatTelemetry())
    val telemetry: StateFlow<LiveThreatTelemetry> = _telemetry.asStateFlow()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var isShieldActive = true
    private var isVisualShieldEnabled = true
    private var isAudioMuteEnabled = true

    @Volatile
    private var exemptedPackages: Set<String> = emptySet()

    private var frameProcessingJob: Job? = null
    private var audioProcessingJob: Job? = null

    private var consecutiveSafeFrames = 0
    private var isAudioCurrentlyMuted = false
    private var preMuteVolume: Int = -1
    private var lastThreatLogTimestamp = 0L

    init {
        // 1. Collect user preference toggles
        coordinatorScope.launch {
            combine(
                preferencesRepository.isShieldActiveFlow,
                preferencesRepository.isVisualShieldEnabledFlow,
                preferencesRepository.isAudioMuteEnabledFlow
            ) { shield, visual, audio ->
                Triple(shield, visual, audio)
            }.collectLatest { (shield, visual, audio) ->
                isShieldActive = shield
                isVisualShieldEnabled = visual
                isAudioMuteEnabled = audio

                _telemetry.update { it.copy(isShieldActive = shield) }

                if (!shield) {
                    dismissAllInterventions()
                }
            }
        }

        // 2. Reactively observe exempted whitelist packages (Zero-GC memory cache)
        coordinatorScope.launch {
            whitelistDao.getExemptedPackageNamesFlow().collectLatest { list ->
                exemptedPackages = list.toSet()
                Log.d(TAG, "Updated in-memory exempted whitelist packages: $exemptedPackages")
            }
        }
    }

    /**
     * Starts dual-engine AI observation pipelines.
     */
    @Synchronized
    fun startObserving() {
        if (frameProcessingJob?.isActive == true && audioProcessingJob?.isActive == true) return

        Log.i(TAG, "Starting Dual-Engine AI Shield Coordinator pipelines")

        // 1. Visual NSFW Frame Pipeline
        frameProcessingJob = coordinatorScope.launch {
            captureEngine.frameFlow.collect { bitmap ->
                try {
                    if (!isShieldActive || !isVisualShieldEnabled) {
                        if (LiveBlurOverlayManager.isShowing()) {
                            LiveBlurOverlayManager.hideOverlay(context)
                        }
                        return@collect
                    }

                    // Check active foreground application exemption (Whitelist Protection)
                    val currentPkg = foregroundAppTracker.currentPackage ?: ForegroundAppTracker.currentForegroundPackage
                    // Unknown foreground package is deliberately fail-safe: continue visual inspection.
                    if (currentPkg != null && (exemptedPackages.contains(currentPkg) || currentPkg == context.packageName)) {
                        if (LiveBlurOverlayManager.isShowing()) {
                            LiveBlurOverlayManager.hideOverlay(context)
                        }
                        return@collect
                    }

                    val result = visionClassifier.classifyFrame(bitmap)

                    _telemetry.update {
                        it.copy(
                            isVisualThreat = result.isNSFW,
                            visualConfidence = result.confidence,
                            visualLabel = result.topLabel,
                            lastEvaluationTime = SystemClock.uptimeMillis()
                        )
                    }

                    if (result.isNSFW) {
                        consecutiveSafeFrames = 0
                        handleVisualThreatDetected(result.confidence, result.topLabel)
                    } else {
                        consecutiveSafeFrames++
                        if (consecutiveSafeFrames >= SAFE_FRAME_HYSTERESIS && LiveBlurOverlayManager.isShowing()) {
                            LiveBlurOverlayManager.hideOverlay(context)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in visual frame inference pipeline", e)
                } finally {
                    if (!bitmap.isRecycled) bitmap.recycle()
                }
            }
        }

        // 2. Explicit Audio Window Pipeline
        audioProcessingJob = coordinatorScope.launch {
            audioCaptureEngine.audioFlow.collect { audioWindow ->
                if (!isShieldActive || !isAudioMuteEnabled) {
                    unmuteAudioIfMuted()
                    return@collect
                }

                // Check active foreground application exemption (Whitelist Protection)
                val currentPkg = foregroundAppTracker.currentPackage ?: ForegroundAppTracker.currentForegroundPackage
                // Unknown foreground package is deliberately fail-safe: continue audio inspection.
                if (currentPkg != null && (exemptedPackages.contains(currentPkg) || currentPkg == context.packageName)) {
                    unmuteAudioIfMuted()
                    return@collect
                }

                try {
                    val result = audioClassifier.classifyAudio(audioWindow)

                    _telemetry.update {
                        it.copy(
                            isAudioThreat = result.isExplicit,
                            audioConfidence = result.confidence,
                            audioCategory = result.topCategory,
                            lastEvaluationTime = SystemClock.uptimeMillis()
                        )
                    }

                    if (result.isExplicit) {
                        handleAudioThreatDetected(result.confidence, result.topCategory)
                    } else {
                        unmuteAudioIfMuted()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in audio classification pipeline", e)
                }
            }
        }
    }

    /**
     * Stops observation pipelines and cleans up overlays and audio state.
     */
    @Synchronized
    fun stopObserving() {
        Log.i(TAG, "Stopping Dual-Engine AI Shield Coordinator pipelines")
        frameProcessingJob?.cancel()
        frameProcessingJob = null
        audioProcessingJob?.cancel()
        audioProcessingJob = null
        dismissAllInterventions()
    }

    private fun handleVisualThreatDetected(confidence: Float, label: String) {
        LiveBlurOverlayManager.showOverlay(
            context = context,
            threatConfidence = confidence,
            threatType = "NSFW Content Detected ($label)"
        )

        logThreatIncident(
            type = ThreatType.VISUAL_NSFW,
            action = ThreatAction.BLURRED,
            severity = confidence,
            details = "NSFW $label filtered with ${(confidence * 100).toInt()}% confidence"
        )
    }

    private fun handleAudioThreatDetected(confidence: Float, category: String) {
        if (!isAudioCurrentlyMuted) {
            try {
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVol > 0) {
                    preMuteVolume = currentVol
                }
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                isAudioCurrentlyMuted = true
                Log.w(TAG, "Explicit audio detected: $category (${(confidence * 100).toInt()}%). Audio stream muted! Saved pre-mute volume: $preMuteVolume")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mute audio stream", e)
            }
        }

        logThreatIncident(
            type = ThreatType.AUDIO_EXPLICIT,
            action = ThreatAction.MUTED,
            severity = confidence,
            details = "Explicit audio ($category) muted with ${(confidence * 100).toInt()}% confidence"
        )
    }

    private fun unmuteAudioIfMuted() {
        if (isAudioCurrentlyMuted) {
            try {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                // If stream remained at 0 or muted, explicitly restore pre-mute volume
                if (preMuteVolume > 0) {
                    val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    if (currentVol == 0) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preMuteVolume, 0)
                    }
                }
                isAudioCurrentlyMuted = false
                Log.d(TAG, "Audio stream restored to normal (restored volume: $preMuteVolume)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore audio stream", e)
            } finally {
                preMuteVolume = -1
            }
        }
    }

    private fun dismissAllInterventions() {
        if (LiveBlurOverlayManager.isShowing()) {
            LiveBlurOverlayManager.hideOverlay(context)
        }
        unmuteAudioIfMuted()
    }

    /**
     * Records incident into SQLite Room DB with 2.5-second rate-limiting.
     * Records the genuine foreground package name and resolved application name.
     */
    private fun logThreatIncident(type: ThreatType, action: ThreatAction, severity: Float, details: String) {
        val now = System.currentTimeMillis()
        if (now - lastThreatLogTimestamp < LOG_DEBOUNCE_MS) return
        lastThreatLogTimestamp = now

        val currentPkg = foregroundAppTracker.currentPackage
            ?: ForegroundAppTracker.currentForegroundPackage
            ?: "foreground.active.session"
        val resolvedAppName = resolveAppName(currentPkg, type)

        coordinatorScope.launch(Dispatchers.IO) {
            try {
                val entity = ThreatLogEntity(
                    timestamp = now,
                    threatType = type,
                    appPackageName = currentPkg,
                    appName = resolvedAppName,
                    severity = severity,
                    actionTaken = action,
                    details = details
                )
                threatLogDao.insertThreatLog(entity)
                _telemetry.update { it.copy(totalThreatsIntercepted = it.totalThreatsIntercepted + 1) }
                Log.i(TAG, "Recorded threat incident in Room DB: $type, app=$resolvedAppName ($currentPkg), action=$action")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record threat incident", e)
            }
        }
    }

    private fun resolveAppName(packageName: String, type: ThreatType): String {
        if (packageName == "foreground.active.session" || packageName.isBlank()) {
            return if (type == ThreatType.VISUAL_NSFW) "Visual Shield" else "Audio Shield"
        }
        return try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            when {
                packageName.contains("chrome", ignoreCase = true) -> "Chrome"
                packageName.contains("youtube", ignoreCase = true) -> "YouTube"
                packageName.contains("instagram", ignoreCase = true) -> "Instagram"
                packageName.contains("tiktok", ignoreCase = true) -> "TikTok"
                packageName.contains("twitter", ignoreCase = true) -> "X (Twitter)"
                packageName.contains("facebook", ignoreCase = true) -> "Facebook"
                else -> packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
            }
        }
    }

    companion object {
        private const val TAG = "SafeScreenCoordinator"
        private const val SAFE_FRAME_HYSTERESIS = 2
        private const val LOG_DEBOUNCE_MS = 2500L
    }
}

