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
 * System-level Focus Intervention Overlay Manager using Android WindowManager.
 *
 * Implements Module 07 Prompt 2:
 * - Fullscreen Doomscroll Breaker Overlay intercepting short-video feeds.
 * - TYPE_APPLICATION_OVERLAY permissions and complete lifecycle handling via [OverlayLifecycleOwner].
 * - "Exit to Home Screen" returns user cleanly to Android launcher.
 * - Mindful break timer and emergency PIN bypass options.
 */
object FocusInterventionOverlayManager {

    private const val TAG = "FocusInterventionManager"

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
     * Displays the full-screen Focus Intervention lockout overlay.
     *
     * @param context Application or Activity context
     * @param appName Target app triggering the intervention (e.g. "Instagram Reels")
     * @param limitMinutes Daily limit reached in minutes
     * @param onDismiss Callback invoked when overlay is dismissed
     */
    fun showOverlay(
        context: Context,
        appName: String = "Instagram Reels",
        limitMinutes: Int = 30,
        onDismiss: () -> Unit = {}
    ) {
        mainHandler.post {
            try {
                if (overlayView != null) {
                    Log.d(TAG, "Focus intervention overlay is already displaying")
                    return@post
                }

                if (!canDrawOverlays(context)) {
                    Log.w(TAG, "SYSTEM_ALERT_WINDOW permission not granted")
                    Toast.makeText(
                        context,
                        "Please grant 'Display over other apps' permission to test focus overlay",
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
                        FocusInterventionContent(
                            state = FocusInterventionUiState(
                                appName = appName,
                                limitMinutes = limitMinutes
                            ),
                            onTakeWalkClick = {
                                Log.d(TAG, "User started 5-min mindful walk")
                            },
                            onExitToHomeClick = {
                                exitToHomeScreen(context)
                                onDismiss()
                            },
                            onEmergencyPassClick = {
                                Log.d(TAG, "User requested 2-minute emergency pass")
                            },
                            onDismiss = {
                                hideOverlay(context)
                                onDismiss()
                            }
                        )
                    }
                }

                overlayView = composeView
                windowManager.addView(composeView, layoutParams)
                Log.d(TAG, "FocusInterventionOverlay attached to WindowManager successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach FocusInterventionOverlay to WindowManager", e)
                Toast.makeText(context, "Overlay Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Minimizes the foreground app and returns user safely to the Android device home screen.
     */
    fun exitToHomeScreen(context: Context) {
        mainHandler.post {
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(homeIntent)
                Log.d(TAG, "Navigated user to Android Launcher Home Screen")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to exit to home screen", e)
            } finally {
                hideOverlay(context)
            }
        }
    }

    /**
     * Hides and detaches the focus intervention overlay from WindowManager.
     */
    fun hideOverlay(context: Context) {
        mainHandler.post {
            try {
                overlayView?.let { view ->
                    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                    windowManager.removeView(view)
                    lifecycleOwner?.onDestroy()
                    lifecycleOwner = null
                    overlayView = null
                    Log.d(TAG, "FocusInterventionOverlay removed from WindowManager")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove FocusInterventionOverlay from WindowManager", e)
            }
        }
    }
}
