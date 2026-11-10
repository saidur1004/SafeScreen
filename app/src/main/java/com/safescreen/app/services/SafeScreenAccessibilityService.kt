package com.safescreen.app.services

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import com.safescreen.app.MainActivity
import com.safescreen.app.R
import com.safescreen.app.data.local.datastore.UserPreferencesRepository
import com.safescreen.app.services.accessibility.ReelsAccessibilityObserver
import com.safescreen.app.services.overlay.LiveBlurOverlayManager
import com.safescreen.app.services.tracker.ForegroundAppTracker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Accessibility Service for SafeScreen:
 * 1. 24/7 Watchdog: Ensures MediaProjection and ShieldProtectionService remain armed.
 * 2. Adult Domain Blocker: Inspects browser URL bar and initiates solid full-page blackout.
 * 3. Motion-Pruning Scroll Listener: Prunes ghost bounding boxes on user scrolling.
 * 4. Short-video reel observer (Instagram, YouTube Shorts, TikTok, Facebook Reels).
 * 5. Active foreground application package tracker.
 */
@AndroidEntryPoint
class SafeScreenAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var reelsObserver: ReelsAccessibilityObserver

    @Inject
    lateinit var appTracker: ForegroundAppTracker

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccessibilityServiceEntryPoint {
        fun reelsObserver(): ReelsAccessibilityObserver
        fun appTracker(): ForegroundAppTracker
        fun userPreferencesRepository(): UserPreferencesRepository
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastWatchdogAlertMs = 0L

    private fun getObserver(): ReelsAccessibilityObserver? {
        if (::reelsObserver.isInitialized) {
            return reelsObserver
        }
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            reelsObserver = entryPoint.reelsObserver()
            reelsObserver
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve ReelsAccessibilityObserver via EntryPoint", e)
            null
        }
    }

    private fun getTracker(): ForegroundAppTracker? {
        if (::appTracker.isInitialized) {
            return appTracker
        }
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            appTracker = entryPoint.appTracker()
            appTracker
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve ForegroundAppTracker via EntryPoint", e)
            null
        }
    }

    private fun getPrefs(): UserPreferencesRepository? {
        if (::userPreferencesRepository.isInitialized) {
            return userPreferencesRepository
        }
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                AccessibilityServiceEntryPoint::class.java
            )
            userPreferencesRepository = entryPoint.userPreferencesRepository()
            userPreferencesRepository
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve UserPreferencesRepository via EntryPoint", e)
            null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // 1. Extract and track active foreground application package name
        val packageName = event.packageName?.toString()
        if (!packageName.isNullOrBlank() && packageName != applicationContext.packageName) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                ForegroundAppTracker.currentForegroundPackage == null) {
                getTracker()?.updateForegroundApp(packageName)
                ForegroundAppTracker.update(packageName)
                checkWatchdogStatus(packageName)

                // If user leaves the browser, dismiss full-page blocking overlay for non-browser apps
                if (!isBrowserApp(packageName)) {
                    LiveBlurOverlayManager.dismissFullPageBlock(applicationContext)
                }
            }
        }

        // 3. Inspect Browser Address Bar for Extreme Adult Domains
        if (!packageName.isNullOrBlank() && isBrowserApp(packageName)) {
            try {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    inspectBrowserUrl(rootNode, packageName)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed inspecting browser URL node: ${e.message}")
            }
        }

        // 4. Delegate to ReelsAccessibilityObserver for short-video inspection
        val observer = getObserver() ?: return

        try {
            val rootNode = rootInActiveWindow
            observer.onAccessibilityEvent(event, rootNode)
        } catch (e: Exception) {
            Log.e(TAG, "Error processing accessibility event", e)
        }
    }

    /**
     * Checks if the package belongs to a supported web browser.
     */
    private fun isBrowserApp(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return BROWSER_PACKAGES.contains(lower) ||
                lower.contains("chrome") ||
                lower.contains("browser") ||
                lower.contains("firefox") ||
                lower.contains("opera")
    }

    /**
     * Inspects browser address bar to detect and block explicit adult domains and search queries immediately.
     */
    private fun inspectBrowserUrl(rootNode: AccessibilityNodeInfo, packageName: String) {
        val urlNode = findUrlBarNode(rootNode) ?: return
        val text = urlNode.text?.toString()?.trim()?.lowercase() ?: return
        if (text.isEmpty()) return

        val matchedDomain = ADULT_DOMAINS.firstOrNull { text.contains(it) }
        val matchedPattern = if (matchedDomain == null) {
            ADULT_SEARCH_PATTERNS.firstOrNull { it.containsMatchIn(text) }?.pattern
        } else null

        if (matchedDomain != null || matchedPattern != null) {
            val detectedThreat = matchedDomain ?: "ADULT CONTENT"
            Log.w(TAG, "Strict Block Triggered: Adult keyword/domain '$detectedThreat' detected in $packageName ($text)")
            val displayDomain = detectedThreat.replace("\\b", "").uppercase()

            LiveBlurOverlayManager.showFullPageBlock(
                context = applicationContext,
                domain = displayDomain
            ) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }

            // Dispatches back action to steer browser away immediately behind the solid blackout
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    /**
     * Locates the URL bar node in supported browsers.
     */
    private fun findUrlBarNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val knownIds = listOf(
            "com.android.chrome:id/url_bar",
            "org.chromium.chrome:id/url_bar",
            "com.brave.browser:id/url_bar",
            "com.microsoft.emmx:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "org.mozilla.firefox:id/toolbar_url_view",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            "com.opera.browser:id/url_field"
        )

        for (id in knownIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                return nodes[0]
            }
        }

        return searchNodeByPredicate(root, maxDepth = 6) { node ->
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            viewId.endsWith("url_bar") || viewId.endsWith("location_bar_edit_text") || viewId.endsWith("toolbar_url_view")
        }
    }

    private fun searchNodeByPredicate(
        node: AccessibilityNodeInfo,
        depth: Int = 0,
        maxDepth: Int = 6,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (depth > maxDepth) return null
        if (predicate(node)) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = searchNodeByPredicate(child, depth + 1, maxDepth, predicate)
            if (result != null) return result
        }
        return null
    }

    /**
     * 24/7 Watchdog: If user intended Shield to be active, but it was stopped or killed,
     * alerts the user to re-enable protection (KafGuard/Covenant Eyes pattern).
     */
    private fun checkWatchdogStatus(currentAppPackage: String) {
        // Ignore internal or safe settings navigation
        if (currentAppPackage == packageName ||
            currentAppPackage.contains("settings") ||
            currentAppPackage.contains("launcher")
        ) {
            return
        }

        serviceScope.launch {
            val prefs = getPrefs() ?: return@launch
            val isShieldActive = prefs.isShieldActiveFlow.firstOrNull() ?: false
            val isRunning = ShieldProtectionService.isServiceRunning.value

            if (isShieldActive && !isRunning) {
                val now = System.currentTimeMillis()
                if (now - lastWatchdogAlertMs > 15_000L) { // Throttle watchdog prompts to every 15 seconds
                    lastWatchdogAlertMs = now
                    Log.w(TAG, "Watchdog Alert: SafeScreen Shield was interrupted while user is in $currentAppPackage! Prompting re-arm.")
                    showWatchdogNotification()

                    // Automatically bring SafeScreen to foreground to request re-arming
                    try {
                        val rearmIntent = Intent(this@SafeScreenAccessibilityService, MainActivity::class.java).apply {
                            action = MainActivity.ACTION_REQUEST_SHIELD
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(rearmIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed launching rearm intent from watchdog", e)
                    }
                }
            }
        }
    }

    private fun showWatchdogNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "safescreen_watchdog_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SafeScreen Protection Watchdog",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Monitors and ensures SafeScreen AI protection stays active 24/7"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_REQUEST_SHIELD
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            2002,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_shield_notification)
            .setContentTitle("SafeScreen Shield Interrupted")
            .setContentText("Tap to re-enable 24/7 on-device Islamic protection.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WATCHDOG_NOTIF_ID, notification)
    }

    override fun onInterrupt() {
        Log.d(TAG, "SafeScreenAccessibilityService interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        Log.i(TAG, "SafeScreenAccessibilityService connected successfully with Watchdog & URL Blocker")
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            getObserver()?.stopTracking()
            getTracker()?.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking on destroy", e)
        }
        Log.i(TAG, "SafeScreenAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "SafeScreenA11y"
        private const val WATCHDOG_NOTIF_ID = 9001
        var isRunning: Boolean = false
            private set

        private val BROWSER_PACKAGES = setOf(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.chrome.canary",
            "com.brave.browser",
            "com.microsoft.emmx",
            "com.sec.android.app.sbrowser",
            "org.mozilla.firefox",
            "org.mozilla.firefox_beta",
            "org.mozilla.fenix",
            "com.opera.browser",
            "com.opera.mini.native",
            "com.opera.touch",
            "com.opera.gx",
            "com.duckduckgo.mobile.android",
            "com.ucmobile.intl",
            "com.kiwibrowser.browser",
            "com.vivaldi.browser"
        )

        private val ADULT_DOMAINS = listOf(
            "pornhub", "xvideos", "xnxx", "xhamster", "redtube",
            "youporn", "chaturbate", "onlyfans", "stripchat", "bongacams",
            "cam4", "livejasmin", "spankbang", "eporner", "beeg",
            "tube8", "thumbzilla", "txxx", "hqporner", "motherless",
            "brazzers", "heavy-r", "porngo", "erome", "rule34",
            "fapello", "porntrex", "daftsex", "youjizz", "tnaflix",
            "nuvid", "javhd", "missav", "javmost", "jable",
            "nhentai", "hanime", "luscious", "danbooru", "gelbooru",
            "porndig", "pornone", "empflix", "czechcasting", "realitykings",
            "bangbros", "naughtyamerica", "banglachoti", "chotikahini",
            "desipapa", "fsiblog", "indianporn", "xcafe", "madthumbs"
        )

        private val ADULT_SEARCH_PATTERNS = listOf(
            "\\bporn\\b", "\\bporno\\b", "\\bxxx\\b", "\\bhentai\\b",
            "\\bnsfw\\b", "\\bsex video\\b", "\\badult video\\b",
            "\\bnude photo\\b", "\\bbangla choti\\b", "\\bchoti golpo\\b"
        ).map { it.toRegex(RegexOption.IGNORE_CASE) }
    }
}
