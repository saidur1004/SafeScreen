package com.safescreen.app.services.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * System-level Floating Overlay Manager using Android WindowManager.
 *
 * Implements Module 07 Prompt 1:
 * - Creates, attaches and manages ComposeView overlay via WindowManager.
 * - Manages TYPE_APPLICATION_OVERLAY permissions and lifecycle.
 * - Thread-safe main-looper dispatching for real-time background ML threat triggers.
 */
object LiveBlurOverlayManager {

    private const val TAG = "LiveBlurOverlayManager"

    @Volatile
    private var overlayView: ComposeView? = null

    @Volatile
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    private val mainHandler = Handler(Looper.getMainLooper())

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
     * Displays the live 100% solid blackout / Non-Mahram WindowManager overlay.
     *
     * @param context Application or Activity context
     * @param threatConfidence Threat detection confidence score (0.0 to 1.0)
     * @param threatType Label of detected threat category
     * @param latencyMs Latency of AI inference in milliseconds
     * @param normalizedBoxes Normalized bounding boxes (0..1) of detected faces/threats
     * @param onDismiss Callback invoked when the user dismisses the overlay
     */
    fun showOverlay(
        context: Context,
        threatConfidence: Float = 0.98f,
        threatType: String = "Harmful Content Blocked",
        latencyMs: Int = 38,
        normalizedBoxes: List<android.graphics.RectF> = emptyList(),
        onDismiss: () -> Unit = {}
    ) {
        mainHandler.post {
            try {
                if (overlayView != null) {
                    Log.d(TAG, "Overlay is already displaying")
                    return@post
                }

                if (!canDrawOverlays(context)) {
                    Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted")
                    Toast.makeText(
                        context,
                        "Please grant 'Display over other apps' permission to test overlay",
                        Toast.LENGTH_LONG
                    ).show()
                    requestOverlayPermission(context)
                    return@post
                }

                val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

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
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT
                )

                val owner = OverlayLifecycleOwner().apply { onCreate() }
                lifecycleOwner = owner

                val composeView = ComposeView(context).apply {
                    setViewTreeLifecycleOwner(owner)
                    setViewTreeSavedStateRegistryOwner(owner)
                    setViewTreeViewModelStoreOwner(owner)
                    setContent {
                        LiveBlurOverlayContent(
                            state = LiveBlurUiState(
                                threatConfidence = threatConfidence,
                                threatType = threatType,
                                latencyMs = latencyMs,
                                normalizedBoxes = normalizedBoxes
                            ),
                            onDismiss = {
                                hideOverlay(context)
                                onDismiss()
                            },
                            onLeaveApp = {
                                hideOverlay(context)
                                onDismiss()
                                try {
                                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                        addCategory(Intent.CATEGORY_HOME)
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(homeIntent)
                                } catch (e: Exception) {
                                    Log.e(TAG, "Failed to navigate to Home screen", e)
                                }
                            }
                        )
                    }
                }

                overlayView = composeView
                windowManager.addView(composeView, layoutParams)
                Log.d(TAG, "LiveBlurOverlayView attached to WindowManager successfully with 100% Solid Blackout")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach LiveBlurOverlayView to WindowManager", e)
                Toast.makeText(context, "Overlay Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                Log.d(TAG, "LiveBlurOverlayView removed from WindowManager")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove LiveBlurOverlayView", e)
            }
        }
    }

    /**
     * Returns true if the blur overlay is currently attached and showing.
     */
    fun isShowing(): Boolean = overlayView != null
}

