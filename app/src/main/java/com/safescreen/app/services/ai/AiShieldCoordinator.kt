package com.safescreen.app.services.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
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
    private var consecutiveSafeAudioWindows = 0
    private var isAudioCurrentlyMuted = false
    private var preMuteVolume: Int = -1
    private var audioFocusRequest: AudioFocusRequest? = null
    private var lastThreatLogTimestamp = 0L
    private var lastVisualThreatPackage: String? = null
    private var visualSnoozeUntil = 0L

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

        // 1. Visual NSFW & Non-Mahram Frame Pipeline
        frameProcessingJob = coordinatorScope.launch {
            captureEngine.frameFlow.collect { bitmap ->
                try {
                    if (!isShieldActive || !isVisualShieldEnabled) {
                        LiveBlurOverlayManager.clearAllBoxes(context)
                        lastVisualThreatPackage = null
                        return@collect
                    }

                    // Check active foreground application exemption (Whitelist Protection)
                    val currentPkg = foregroundAppTracker.currentPackage ?: ForegroundAppTracker.currentForegroundPackage

                    // User-whitelisted apps: clear shields and bypass
                    if (currentPkg != null && exemptedPackages.contains(currentPkg)) {
                        LiveBlurOverlayManager.clearAllBoxes(context)
                        return@collect
                    }

                    val result = visionClassifier.classifyFrame(bitmap)

                    _telemetry.update {
                        it.copy(
                            isVisualThreat = result.isThreat,
                            visualConfidence = result.confidence,
                            visualLabel = result.topLabel,
                            lastEvaluationTime = SystemClock.uptimeMillis()
                        )
                    }

                    if (result.isThreat && result.normalizedBoxes.isNotEmpty()) {
                        handleVisualThreatDetected(
                            confidence = result.confidence,
                            label = result.topLabel,
                            latencyMs = result.inferenceTimeMs.toInt(),
                            normalizedBoxes = result.normalizedBoxes,
                            isExtremePorn = result.pornScore >= 0.70f
                        )
                    } else {
                        LiveBlurOverlayManager.onCleanFrameReceived(context)
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
                // If user is inside an explicitly whitelisted application (other than SafeScreen), allow audio
                if (currentPkg != null && exemptedPackages.contains(currentPkg) && currentPkg != context.packageName) {
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
                        consecutiveSafeAudioWindows = 0
                        handleAudioThreatDetected(result.confidence, result.topCategory)
                    } else {
                        if (isAudioCurrentlyMuted) {
                            consecutiveSafeAudioWindows++
                            if (consecutiveSafeAudioWindows >= SAFE_AUDIO_HYSTERESIS_WINDOWS) {
                                unmuteAudioIfMuted()
                                consecutiveSafeAudioWindows = 0
                            }
                        }
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

    fun testTriggerVisualThreat() {
        handleVisualThreatDetected(0.96f, "NSFW Provocative")
    }

    fun testTriggerAudioThreat() {
        handleAudioThreatDetected(0.92f, "Music Track")
    }

    private fun isBrowserPackage(pkg: String): Boolean {
        val lower = pkg.lowercase()
        return lower.contains("chrome") ||
                lower.contains("browser") ||
                lower.contains("firefox") ||
                lower.contains("opera") ||
                lower == "com.brave.browser" ||
                lower == "com.microsoft.emmx" ||
                lower == "com.sec.android.app.sbrowser" ||
                lower == "com.duckduckgo.mobile.android"
    }

    private fun handleVisualThreatDetected(
        confidence: Float,
        label: String,
        latencyMs: Int = 38,
        normalizedBoxes: List<android.graphics.RectF> = emptyList(),
        isExtremePorn: Boolean = false
    ) {
        val currentPkg = foregroundAppTracker.currentPackage ?: ForegroundAppTracker.currentForegroundPackage
        lastVisualThreatPackage = currentPkg

        // If user is inside a web browser and an extreme adult video/page is playing, block the full page
        if (isExtremePorn && currentPkg != null && isBrowserPackage(currentPkg)) {
            Log.w(TAG, "Extreme adult visual content detected in browser ($currentPkg). Triggering full-page block.")
            LiveBlurOverlayManager.showFullPageBlock(
                context = context,
                domain = "EXPLICIT WEBPAGE BLOCKED"
            )
            return
        }

        LiveBlurOverlayManager.updateTargetedBoxes(
            context = context,
            newBoxes = normalizedBoxes,
            label = label,
            threatConfidence = confidence
        )

        logThreatIncident(
            type = ThreatType.VISUAL_NSFW,
            action = ThreatAction.BLURRED,
            severity = confidence,
            details = "$label targeted blackout applied with ${(confidence * 100).toInt()}% confidence"
        )
    }

    private fun handleAudioThreatDetected(confidence: Float, category: String) {
        if (!isAudioCurrentlyMuted) {
            try {
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                if (currentVol > 0) {
                    preMuteVolume = currentVol
                }

                // 1. Transient Audio Focus Hijack: Forces Android AudioFlinger to duck/silence all media players
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val playbackAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                    val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(playbackAttributes)
                        .setAcceptsDelayedFocusGain(false)
                        .setOnAudioFocusChangeListener { /* maintain silence */ }
                        .build()
                    audioFocusRequest = request
                    audioManager.requestAudioFocus(request)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                }

                // 2. Hardware stream volume zeroing + MUTE flag
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)

                // 3. Dispatch media pause keyevent to halt external media players (YouTube, Spotify, Chrome)
                audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
                audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))

                isAudioCurrentlyMuted = true
                Log.w(
                    TAG,
                    "Audio Threat Detected: $category (${(confidence * 100).toInt()}%). Audio stream silenced! Saved pre-mute volume: $preMuteVolume"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to mute audio stream", e)
            }
        }

        logThreatIncident(
            type = ThreatType.AUDIO_EXPLICIT,
            action = ThreatAction.MUTED,
            severity = confidence,
            details = "Audio threat ($category) muted with ${(confidence * 100).toInt()}% confidence"
        )
    }

    private fun unmuteAudioIfMuted() {
        if (isAudioCurrentlyMuted) {
            try {
                // 1. Release Audio Focus
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                    audioFocusRequest = null
                } else {
                    @Suppress("DEPRECATION")
                    audioManager.abandonAudioFocus(null)
                }

                // 2. Release hardware MUTE flag
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                if (preMuteVolume > 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, preMuteVolume, 0)
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
        LiveBlurOverlayManager.clearAllBoxes(context)
        lastVisualThreatPackage = null
        consecutiveSafeAudioWindows = 0
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
        private const val SAFE_AUDIO_HYSTERESIS_WINDOWS = 10
        private const val VISUAL_SNOOZE_MS = 3500L
        private const val LOG_DEBOUNCE_MS = 2500L
    }
}

