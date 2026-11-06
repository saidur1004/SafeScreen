package com.safescreen.app.services.accessibility

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.data.local.dao.ThreatLogDao
import com.safescreen.app.data.local.entity.ThreatAction
import com.safescreen.app.data.local.entity.ThreatLogEntity
import com.safescreen.app.data.local.entity.ThreatType
import com.safescreen.app.services.overlay.FocusInterventionOverlayManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Platform identifiers for short-form video feeds.
 */
enum class ReelPlatform(val displayName: String, val packageNames: Set<String>) {
    INSTAGRAM(
        displayName = "Instagram Reels",
        packageNames = setOf("com.instagram.android")
    ),
    YOUTUBE(
        displayName = "YouTube Shorts",
        packageNames = setOf("com.google.android.youtube")
    ),
    TIKTOK(
        displayName = "TikTok",
        packageNames = setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")
    ),
    FACEBOOK(
        displayName = "Facebook Reels",
        packageNames = setOf("com.facebook.katana")
    ),
    CHROME(
        displayName = "Web Shorts",
        packageNames = setOf("com.android.chrome")
    );

    companion object {
        fun fromPackageName(packageName: String): ReelPlatform? {
            return entries.firstOrNull { it.packageNames.contains(packageName) }
        }
    }
}

/**
 * ReelsAccessibilityObserver: High-efficiency, zero-lag observer for detecting and
 * tracking short-form video consumption (Instagram Reels, YouTube Shorts, TikTok, Facebook Reels).
 *
 * Implements Phase 4.3 requirements:
 * 1. Debounced accessibility node traversal (max depth 5, max 45 nodes) to maintain Zero GC.
 * 2. Active viewing screen-time ticker synced with DataStore (reelsMinutesUsedToday).
 * 3. Autonomous intervention overlay triggering via [FocusInterventionOverlayManager] when limits are exceeded.
 */
@Singleton
class ReelsAccessibilityObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: UserPreferencesRepository,
    private val threatLogDao: ThreatLogDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isReelActive = MutableStateFlow(false)
    val isReelActive: StateFlow<Boolean> = _isReelActive.asStateFlow()

    private val _activePlatform = MutableStateFlow<ReelPlatform?>(null)
    val activePlatform: StateFlow<ReelPlatform?> = _activePlatform.asStateFlow()

    private var isBlockerEnabled = false
    private var dailyLimitMinutes = 30
    private var minutesUsedToday = 0
    private var managedAppPolicies = defaultManagedAppPolicies()
    private var activePackageName: String? = null
    private var activeLimitMinutes = 30
    private var limitLoggedForCurrentFeed = false

    private var tickerJob: Job? = null
    private var lastInspectionTime = 0L
    private var activeSecondsCount = 0
    private var snoozeUntilRealtime = 0L

    /**
     * Snoozes full-screen intervention overlays for a specified duration (e.g. emergency pass or dismiss).
     */
    fun snoozeIntervention(durationMillis: Long = 120_000L) {
        snoozeUntilRealtime = SystemClock.elapsedRealtime() + durationMillis
        Log.d(TAG, "Focus intervention snoozed for ${durationMillis / 1000}s")
    }

    init {
        // Collect DataStore preferences reactively
        scope.launch {
            combine(
                preferencesRepository.isReelsBlockerEnabledFlow,
                preferencesRepository.reelsDailyLimitMinutesFlow,
                preferencesRepository.reelsMinutesUsedTodayFlow,
                preferencesRepository.reelsManagedAppsJsonFlow
            ) { enabled, limit, used, managedJson ->
                ReelsPreferences(enabled, limit, used, parseManagedAppPolicies(managedJson, limit))
            }.collect { prefs ->
                val wasActivePackageEnabled = activePackageName?.let {
                    managedAppPolicies[it]?.isEnabled == true
                } ?: true
                isBlockerEnabled = prefs.isBlockerEnabled
                dailyLimitMinutes = prefs.dailyLimitMinutes
                minutesUsedToday = prefs.minutesUsedToday
                managedAppPolicies = prefs.managedAppPolicies
                activePackageName?.let { managedAppPolicies[it] }?.let {
                    activeLimitMinutes = it.timeLimitMinutes
                }

                if (_isReelActive.value && wasActivePackageEnabled &&
                    activePackageName?.let { managedAppPolicies[it]?.isEnabled != true } == true
                ) {
                    setReelState(false, null)
                    FocusInterventionOverlayManager.hideOverlay(context)
                }
                if (_isReelActive.value && !isBlockerEnabled) {
                    FocusInterventionOverlayManager.hideOverlay(context)
                }

                Log.d(TAG, "Reels prefs updated: blockerEnabled=$isBlockerEnabled, dailyLimit=${dailyLimitMinutes}m, used=${minutesUsedToday}m")

                // Check intervention condition immediately upon preference update
                checkAndTriggerIntervention()
            }
        }
    }

    /**
     * Handles incoming accessibility events from [SafeScreenAccessibilityService].
     *
     * @param event The accessibility event dispatched by Android OS.
     * @param rootNode The root node in the active window (can be null).
     */
    fun onAccessibilityEvent(event: AccessibilityEvent?, rootNode: AccessibilityNodeInfo?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        val platform = ReelPlatform.fromPackageName(packageName)

        if (platform == null) {
            // User left target social apps -> deactivate reels tracking if active
            if (_isReelActive.value) {
                setReelState(false, null)
            }
            return
        }

        val policy = managedAppPolicies[packageName]
        if (policy == null || !policy.isEnabled) {
            if (_isReelActive.value) setReelState(false, null)
            return
        }

        val eventType = event.eventType
        val isWindowChange = (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        val isContentChange = (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)

        if (!isWindowChange && !isContentChange) return

        val now = SystemClock.uptimeMillis()
        // Debounce node inspection: max 1 full traversal per 300ms unless window state changed
        if (!isWindowChange && (now - lastInspectionTime < DEBOUNCE_INTERVAL_MS)) {
            return
        }
        lastInspectionTime = now

        // Check if the current window/node hierarchy matches a short-video reel feed
        val isReelFeed = isNodeHierarchyInReelFeed(platform, rootNode)

        if (isReelFeed) {
            if (!_isReelActive.value || _activePlatform.value != platform) {
                Log.i(TAG, "Detected active short-video feed: ${platform.displayName}")
                activeLimitMinutes = policy.timeLimitMinutes
                setReelState(true, platform, packageName)
            }
        } else {
            if (_isReelActive.value && _activePlatform.value == platform) {
                Log.d(TAG, "Exited short-video feed in ${platform.displayName}")
                setReelState(false, null)
            }
        }
    }

    /**
     * Inspects the accessibility node hierarchy with depth and count bounds to ensure zero UI lag.
     */
    private fun isNodeHierarchyInReelFeed(platform: ReelPlatform, rootNode: AccessibilityNodeInfo?): Boolean {
        if (rootNode == null) return false

        // TikTok is entirely short-form video
        if (platform == ReelPlatform.TIKTOK) {
            return true
        }

        val counter = intArrayOf(0)
        return searchHierarchy(rootNode, platform, depth = 0, counter = counter)
    }

    private fun searchHierarchy(
        node: AccessibilityNodeInfo,
        platform: ReelPlatform,
        depth: Int,
        counter: IntArray
    ): Boolean {
        if (depth > MAX_INSPECTION_DEPTH || counter[0] >= MAX_INSPECTION_NODES) return false
        counter[0]++

        if (matchesReelSignature(platform, node)) {
            return true
        }

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = node.getChild(i) ?: continue
            val found = try {
                searchHierarchy(child, platform, depth + 1, counter)
            } finally {
                @Suppress("DEPRECATION")
                child.recycle()
            }
            if (found) return true
        }

        return false
    }

    /**
     * Matches known platform view IDs and descriptors for short-video players.
     */
    private fun matchesReelSignature(platform: ReelPlatform, node: AccessibilityNodeInfo): Boolean {
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""
        val className = node.className?.toString()?.lowercase() ?: ""

        return when (platform) {
            ReelPlatform.INSTAGRAM -> {
                viewId.contains("clips") ||
                        viewId.contains("reel") ||
                        contentDesc.contains("reels") ||
                        contentDesc.contains("reel by") ||
                        text == "reels"
            }
            ReelPlatform.YOUTUBE -> {
                viewId.contains("shorts") ||
                        viewId.contains("reel") ||
                        contentDesc.contains("shorts") ||
                        contentDesc.contains("short") ||
                        contentDesc.contains("remix") ||
                        className.contains("reel") ||
                        text == "shorts"
            }
            ReelPlatform.FACEBOOK -> {
                viewId.contains("reels") ||
                        viewId.contains("reel") ||
                        viewId.contains("fb_shorts") ||
                        contentDesc.contains("reels") ||
                        contentDesc.contains("reel") ||
                        text.contains("reels") ||
                        text.contains("watch reels")
            }
            ReelPlatform.CHROME -> {
                text.contains("youtube.com/shorts") ||
                        text.contains("instagram.com/reels") ||
                        text.contains("facebook.com/reel") ||
                        text.contains("tiktok.com") ||
                        contentDesc.contains("youtube.com/shorts") ||
                        contentDesc.contains("instagram.com/reels") ||
                        contentDesc.contains("facebook.com/reel") ||
                        contentDesc.contains("tiktok.com")
            }
            ReelPlatform.TIKTOK -> true
        }
    }

    /**
     * Updates active reel state and toggles the screen-time ticker.
     */
    private fun setReelState(active: Boolean, platform: ReelPlatform?, packageName: String? = null) {
        if (active && packageName != activePackageName) {
            limitLoggedForCurrentFeed = false
        }
        _isReelActive.value = active
        _activePlatform.value = platform
        activePackageName = packageName

        if (active) {
            startTicker()
            checkAndTriggerIntervention()
        } else {
            stopTicker()
            limitLoggedForCurrentFeed = false
        }
    }

    /**
     * Starts a 1-second interval ticker while short videos are on screen.
     */
    private fun startTicker() {
        if (tickerJob?.isActive == true) return

        tickerJob = scope.launch {
            Log.d(TAG, "Reels screen-time ticker started")
            while (isActive && _isReelActive.value) {
                delay(1000L)
                activeSecondsCount++

                if (activeSecondsCount >= 60) {
                    activeSecondsCount = 0
                    preferencesRepository.incrementReelsMinutesUsedToday(1)
                    Log.d(TAG, "Reels minute recorded. New total used: ${minutesUsedToday + 1}m")
                    checkAndTriggerIntervention()
                }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
        activeSecondsCount = 0
        Log.d(TAG, "Reels screen-time ticker stopped")
    }

    /**
     * Evaluates daily limit against usage and displays the full-screen intervention overlay if reached.
     */
    private fun checkAndTriggerIntervention() {
        if (!_isReelActive.value) return

        val now = SystemClock.elapsedRealtime()
        if (now < snoozeUntilRealtime) {
            Log.d(TAG, "Focus intervention currently snoozed (${(snoozeUntilRealtime - now) / 1000}s remaining)")
            return
        }

        if (isBlockerEnabled && minutesUsedToday >= activeLimitMinutes) {
            val appTitle = _activePlatform.value?.displayName ?: "Reels / Shorts"
            Log.w(
                TAG,
                "Focus Intervention Alert: $appTitle limit reached ($minutesUsedToday/$activeLimitMinutes min). Showing overlay!"
            )

            if (!limitLoggedForCurrentFeed) {
                limitLoggedForCurrentFeed = true
                val packageName = activePackageName ?: return
                val usedAtIntervention = minutesUsedToday
                val limitAtIntervention = activeLimitMinutes
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        threatLogDao.insertThreatLog(
                            ThreatLogEntity(
                                timestamp = System.currentTimeMillis(),
                                threatType = ThreatType.REELS_LIMIT,
                                appPackageName = packageName,
                                appName = appTitle,
                                severity = 1f,
                                actionTaken = ThreatAction.BLOCKED,
                                details = "Daily limit reached ($usedAtIntervention/$limitAtIntervention min)"
                            )
                        )
                    }.onFailure { Log.e(TAG, "Failed to log Reels limit event", it) }
                }
            }

            FocusInterventionOverlayManager.showOverlay(
                context = context,
                appName = appTitle,
                limitMinutes = activeLimitMinutes,
                onDismiss = {
                    Log.d(TAG, "FocusInterventionOverlay dismissed by user, snoozing for 2 minutes")
                    snoozeIntervention(120_000L)
                }
            )
        }
    }

    /**
     * Clean up resources on service destruction.
     */
    fun stopTracking() {
        setReelState(false, null)
        FocusInterventionOverlayManager.hideOverlay(context)
    }

    companion object {
        private const val TAG = "SafeScreenReels"
        private const val DEBOUNCE_INTERVAL_MS = 250L
        private const val MAX_INSPECTION_DEPTH = 25
        private const val MAX_INSPECTION_NODES = 300
    }

    private data class ManagedAppPolicy(val isEnabled: Boolean, val timeLimitMinutes: Int)

    private data class ReelsPreferences(
        val isBlockerEnabled: Boolean,
        val dailyLimitMinutes: Int,
        val minutesUsedToday: Int,
        val managedAppPolicies: Map<String, ManagedAppPolicy>
    )

    private fun defaultManagedAppPolicies(limitMinutes: Int = 30) = mapOf(
        "com.instagram.android" to ManagedAppPolicy(true, limitMinutes),
        "com.google.android.youtube" to ManagedAppPolicy(true, limitMinutes),
        "com.facebook.katana" to ManagedAppPolicy(true, limitMinutes),
        "com.zhiliaoapp.musically" to ManagedAppPolicy(true, limitMinutes),
        "com.ss.android.ugc.trill" to ManagedAppPolicy(true, limitMinutes),
        "com.android.chrome" to ManagedAppPolicy(true, limitMinutes)
    )

    private fun parseManagedAppPolicies(json: String, fallbackLimit: Int): Map<String, ManagedAppPolicy> {
        if (json.isBlank()) return defaultManagedAppPolicies(fallbackLimit)
        return runCatching {
            val array = JSONArray(json)
            buildMap {
                for (index in 0 until array.length()) {
                    val app = array.getJSONObject(index)
                    val packageName = app.optString("pkg").takeIf { it.isNotBlank() } ?: continue
                    put(
                        packageName,
                        ManagedAppPolicy(
                            isEnabled = app.optBoolean("enabled", true),
                            timeLimitMinutes = app.optInt("limit", fallbackLimit).coerceIn(5, 240)
                        )
                    )
                }
            }.ifEmpty { defaultManagedAppPolicies(fallbackLimit) }
        }.getOrElse {
            Log.e(TAG, "Invalid managed-app settings; using supported defaults", it)
            defaultManagedAppPolicies(fallbackLimit)
        }
    }
}
