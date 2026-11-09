package com.safescreen.app.services.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * System-level Floating Overlay Manager using Android WindowManager.
 *
 * Implements strict KafGuard-style targeted Non-Mahram image protection:
 * - Creates, attaches and manages a transparent ComposeView overlay via WindowManager.
 * - FLAG_NOT_TOUCHABLE enables seamless touch pass-through so user can scroll & interact normally.
 * - Supports Full-Page Domain Blackout when extreme adult domains are navigated.
 * - Removes vertical cutouts/offsets via LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES.
 * - Dynamically updates targeted solid black boxes in real-time with motion pruning.
 */
object LiveBlurOverlayManager {

    private const val TAG = "LiveBlurOverlayManager"
    private const val BOX_EXPIRATION_MS = 600L // Stable retention to prevent flickering and maintain continuous solid blackout

    @Volatile
    private var overlayView: ComposeView? = null

    @Volatile
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private val mainHandler = Handler(Looper.getMainLooper())

    private var nextBoxId = 1
    private val activeBoxes = CopyOnWriteArrayList<ShieldBox>()

    private val _activeBoxesFlow = MutableStateFlow<List<ShieldBox>>(emptyList())
    val activeBoxesFlow = _activeBoxesFlow.asStateFlow()

    private val _blockedDomainFlow = MutableStateFlow<String?>(null)
    val blockedDomainFlow = _blockedDomainFlow.asStateFlow()

    private var onDismissBlockAction: (() -> Unit)? = null

    /**
     * Checks whether SafeScreen has permission to draw system overlays.
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Launches Android system settings to grant SYSTEM_ALERT_WINDOW permission.
     */
    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    /**
     * Dynamically updates targeted solid black boxes for detected Non-Mahram faces or explicit areas.
     */
    fun updateTargetedBoxes(
        context: Context,
        newBoxes: List<RectF>,
        label: String = "Non-Mahram Guarded",
        threatConfidence: Float = 0.95f
    ) {
        mainHandler.post {
            val now = SystemClock.elapsedRealtime()

            if (newBoxes.isNotEmpty()) {
                for (newBox in newBoxes) {
                    val match = if (activeBoxes.size == 1 && newBoxes.size == 1) {
                        activeBoxes.firstOrNull()
                    } else {
                        activeBoxes.minByOrNull { existing ->
                            val dx = Math.abs(existing.rect.centerX() - newBox.centerX())
                            val dy = Math.abs(existing.rect.centerY() - newBox.centerY())
                            dx * dx + dy * dy
                        }?.takeIf {
                            val dx = Math.abs(it.rect.centerX() - newBox.centerX())
                            val dy = Math.abs(it.rect.centerY() - newBox.centerY())
                            dx < 0.25f && dy < 0.35f
                        }
                    }

                    if (match != null) {
                        match.rect = newBox
                        match.lastSeenMs = now
                        match.label = label
                    } else {
                        activeBoxes.add(
                            ShieldBox(
                                id = nextBoxId++,
                                rect = newBox,
                                label = label,
                                lastSeenMs = now
                            )
                        )
                    }
                }
            }

            // Prune expired boxes (older than retention window)
            activeBoxes.removeAll { box -> now - box.lastSeenMs > BOX_EXPIRATION_MS }

            if (activeBoxes.isNotEmpty() || _blockedDomainFlow.value != null) {
                ensureOverlayAttached(context)
                _activeBoxesFlow.value = activeBoxes.map { it.copy() }
            } else {
                _activeBoxesFlow.value = emptyList()
            }
        }
    }

    /**
     * Called by AccessibilityService when user scrolls feed to prune ghost/stale boxes instantly.
     */
    fun onUserScrolled() {
        mainHandler.post {
            if (activeBoxes.isNotEmpty()) {
                activeBoxes.clear()
                _activeBoxesFlow.value = emptyList()
            }
        }
    }

    /**
     * Shows a 100% Solid Pitch Black Full-Page Blockout when an adult site is detected.
     */
    fun showFullPageBlock(context: Context, domain: String, onExit: () -> Unit = {}) {
        mainHandler.post {
            onDismissBlockAction = onExit
            _blockedDomainFlow.value = domain
            ensureOverlayAttached(context)

            // Full-page block requires touch events to allow user to tap "Leave Website"
            overlayView?.let { view ->
                val lp = view.layoutParams as? WindowManager.LayoutParams
                if (lp != null && (lp.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) != 0) {
                    lp.flags = lp.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    wm.updateViewLayout(view, lp)
                    Log.i(TAG, "Full-Page Adult Domain Block activated with touch interactivity for $domain")
                }
            }
        }
    }

    /**
     * Dismisses the Full-Page Domain Block and returns to normal passive mode.
     */
    fun dismissFullPageBlock(context: Context) {
        mainHandler.post {
            _blockedDomainFlow.value = null
            onDismissBlockAction = null

            overlayView?.let { view ->
                val lp = view.layoutParams as? WindowManager.LayoutParams
                if (lp != null && (lp.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE) == 0) {
                    lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    wm.updateViewLayout(view, lp)
                }
            }

            if (activeBoxes.isEmpty()) {
                hideOverlay(context)
            }
        }
    }

    /**
     * Called when a clean frame without threats is processed; gently expires old boxes.
     */
    fun onCleanFrameReceived(context: Context) {
        mainHandler.post {
            val now = SystemClock.elapsedRealtime()
            activeBoxes.removeAll { box -> now - box.lastSeenMs > BOX_EXPIRATION_MS }
            _activeBoxesFlow.value = activeBoxes.map { it.copy() }
            if (activeBoxes.isEmpty() && _blockedDomainFlow.value == null) {
                // Keep overlay lightweight or detach if idle
            }
        }
    }

    /**
     * Attaches the transparent WindowManager overlay if not already active.
     */
    private fun ensureOverlayAttached(context: Context) {
        if (overlayView != null) return

        if (!canDrawOverlays(context)) {
            Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        try {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            // Critical: FLAG_NOT_TOUCHABLE allows ALL touch events to pass through to underlying apps!
            val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            val owner = OverlayLifecycleOwner().apply { onCreate() }
            lifecycleOwner = owner

            val composeView = ComposeView(context).apply {
                setViewTreeLifecycleOwner(owner)
                setViewTreeSavedStateRegistryOwner(owner)
                setViewTreeViewModelStoreOwner(owner)
                setContent {
                    val boxes by _activeBoxesFlow.collectAsState()
                    val blockedDomain by _blockedDomainFlow.collectAsState()
                    LiveBlurOverlayContent(
                        boxes = boxes,
                        blockedDomain = blockedDomain,
                        onDismissBlock = {
                            val action = onDismissBlockAction
                            dismissFullPageBlock(context)
                            action?.invoke()
                        }
                    )
                }
            }

            overlayView = composeView
            windowManager.addView(composeView, layoutParams)
            Log.d(TAG, "Targeted LiveBlurOverlayView attached with cutout-aligned coordinates")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach Targeted LiveBlurOverlayView to WindowManager", e)
        }
    }

    /**
     * Legacy / Direct helper to show a targeted box or demo.
     */
    fun showOverlay(
        context: Context,
        threatConfidence: Float = 0.98f,
        threatType: String = "Non-Mahram Guarded",
        latencyMs: Int = 38,
        normalizedBoxes: List<RectF> = emptyList(),
        onDismiss: () -> Unit = {}
    ) {
        val boxes = if (normalizedBoxes.isNotEmpty()) {
            normalizedBoxes
        } else {
            listOf(RectF(0.15f, 0.25f, 0.85f, 0.65f))
        }
        updateTargetedBoxes(context, boxes, threatType, threatConfidence)
    }

    /**
     * Clears all boxes and detaches the overlay view.
     */
    fun clearAllBoxes(context: Context) {
        mainHandler.post {
            activeBoxes.clear()
            _activeBoxesFlow.value = emptyList()
            _blockedDomainFlow.value = null
            hideOverlay(context)
        }
    }

    /**
     * Hides and detaches the blur overlay from WindowManager.
     */
    fun hideOverlay(context: Context) {
        mainHandler.post {
            try {
                val view = overlayView ?: return@post
                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.removeView(view)

                lifecycleOwner?.onDestroy()
                lifecycleOwner = null
                overlayView = null
                Log.d(TAG, "Targeted LiveBlurOverlayView removed from WindowManager")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove LiveBlurOverlayView", e)
            }
        }
    }

    /**
     * Returns true if the blur overlay is currently attached.
     */
    fun isShowing(): Boolean = overlayView != null
}
