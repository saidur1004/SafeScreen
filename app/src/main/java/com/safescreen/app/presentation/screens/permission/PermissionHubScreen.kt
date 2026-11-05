package com.safescreen.app.presentation.screens.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safescreen.app.services.ShieldProtectionService
import com.safescreen.app.services.permission.PermissionManager
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextSecondary

/**
 * Types of Android System Permissions monitored by SafeScreen.
 */
enum class PermissionType {
    OVERLAY,
    SCREEN_CAPTURE,
    AUDIO_CAPTURE,
    ACCESSIBILITY,
    NOTIFICATION
}

/**
 * State representation for individual permission item.
 */
data class PermissionItemState(
    val type: PermissionType,
    val title: String,
    val systemCodeName: String,
    val description: String,
    val isGranted: Boolean,
    val iconTint: Color,
    val actionLabel: String = "Grant Access"
)

/**
 * Overall UiState for PermissionHubScreen.
 */
data class PermissionHubUiState(
    val permissions: List<PermissionItemState> = defaultPermissions,
    val isBatteryOptimizedExempted: Boolean = false,
    val showBatteryDialog: Boolean = false,
    val isLoading: Boolean = false
) {
    val areAllGranted: Boolean
        get() = permissions.all { it.isGranted }
}

/**
 * Default initial permissions matching Image 2 reference:
 * - "Overlay" is Granted
 * - "Screen Capture" is Action Required
 * - "Audio" is Action Required
 * - "Accessibility" is Action Required
 * - "Foreground Notification" is Granted
 */
val defaultPermissions = listOf(
    PermissionItemState(
        type = PermissionType.OVERLAY,
        title = "Display Over Other Apps",
        systemCodeName = "SYSTEM_ALERT_WINDOW",
        description = "Allows drawing instant blur overlays over explicit content.",
        isGranted = false,
        iconTint = NeonCyan,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.SCREEN_CAPTURE,
        title = "Screen Capture",
        systemCodeName = "MediaProjection",
        description = "Enables local AI frame inspection. No screenshots are ever saved.",
        isGranted = false,
        iconTint = NeonPurple,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.AUDIO_CAPTURE,
        title = "Audio Playback Capture",
        systemCodeName = "RECORD_AUDIO",
        description = "Allows detecting and muting background music tracks.",
        isGranted = false,
        iconTint = NeonGreen,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.ACCESSIBILITY,
        title = "Accessibility Service",
        systemCodeName = "SafeScreenAccessibilityService",
        description = "Detects Reels & Shorts feeds to enforce your screen-time limits.",
        isGranted = false,
        iconTint = NeonPurple,
        actionLabel = "Enable"
    ),
    PermissionItemState(
        type = PermissionType.NOTIFICATION,
        title = "Foreground Notification",
        systemCodeName = "POST_NOTIFICATIONS",
        description = "Keeps the real-time AI shield persistent without being killed by Android.",
        isGranted = false,
        iconTint = NeonGreen,
        actionLabel = "Grant Access"
    )
)

/**
 * UI Events emitted from PermissionHubScreen.
 */
sealed interface PermissionHubUiEvent {
    data class RequestPermission(val type: PermissionType) : PermissionHubUiEvent
    data object OpenBatteryDialog : PermissionHubUiEvent
    data object DismissBatteryDialog : PermissionHubUiEvent
    data object AllowUnrestrictedBattery : PermissionHubUiEvent
    data object EnterShieldHub : PermissionHubUiEvent
    data object NavigateBack : PermissionHubUiEvent
}

/**
 * Stateful production entry point backed by the permission hub ViewModel.
 */
@Composable
fun PermissionHubScreen(
    onNavigateBack: () -> Unit = {},
    onAllPermissionsGranted: () -> Unit = {},
    viewModel: PermissionHubViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions(context)
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions(context)
    }

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissions(context)
    }

    val accessibilityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissions(context)
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.onScreenCaptureGranted(result.resultCode, result.data)
        }
        viewModel.refreshPermissions(context)
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissions(context)
    }

    // Query OS for initial real permission status on launch
    LaunchedEffect(Unit) {
        viewModel.refreshPermissions(context)
    }

    // Refresh OS permissions when returning to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    PermissionHubContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is PermissionHubUiEvent.RequestPermission -> {
                    when (event.type) {
                        PermissionType.OVERLAY -> {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            overlayLauncher.launch(intent)
                        }
                        PermissionType.SCREEN_CAPTURE -> {
                            val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        }
                        PermissionType.AUDIO_CAPTURE -> {
                            audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        PermissionType.ACCESSIBILITY -> {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            accessibilityLauncher.launch(intent)
                        }
                        PermissionType.NOTIFICATION -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }
                PermissionHubUiEvent.OpenBatteryDialog -> {
                    viewModel.openBatteryDialog()
                }
                PermissionHubUiEvent.DismissBatteryDialog -> {
                    viewModel.dismissBatteryDialog()
                }
                PermissionHubUiEvent.AllowUnrestrictedBattery -> {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:${context.packageName}")
                    )
                    batteryLauncher.launch(intent)
                    viewModel.onAllowUnrestrictedBattery(context)
                    viewModel.refreshPermissions(context)

                    // Fix: Check real permissions before allowing app entry
                    if (state.areAllGranted) {
                        ShieldProtectionService.startService(
                            context,
                            PermissionManager.mediaProjectionResultCode,
                            PermissionManager.mediaProjectionResultData
                        )
                        onAllPermissionsGranted()
                    } else {
                        Toast.makeText(
                            context,
                            "Battery optimization updated. Please grant remaining permissions to proceed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                PermissionHubUiEvent.EnterShieldHub -> {
                    viewModel.refreshPermissions(context)
                    if (state.areAllGranted) {
                        viewModel.onAllowUnrestrictedBattery(context)
                        ShieldProtectionService.startService(
                            context,
                            PermissionManager.mediaProjectionResultCode,
                            PermissionManager.mediaProjectionResultData
                        )
                        onAllPermissionsGranted()
                    } else {
                        Toast.makeText(
                            context,
                            "Action Required: Please grant all required permissions to activate protection shield.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                PermissionHubUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    )
}

@Composable
fun PermissionHubScreen(
    onNavigateBack: () -> Unit = {},
    onAllPermissionsGranted: () -> Unit = {},
    uiState: PermissionHubUiState,
    onEvent: (PermissionHubUiEvent) -> Unit
) {
    PermissionHubContent(
        uiState = uiState,
        onEvent = onEvent
    )
}

/**
 * Pure stateless Composable adhering to State Hoisting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionHubContent(
    uiState: PermissionHubUiState,
    onEvent: (PermissionHubUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Bottom Ambient Waves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 180.dp,
            intensity = 0.9f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Frosted Back Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable { onEvent(PermissionHubUiEvent.NavigateBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Center Title
                Text(
                    text = "System Permissions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
                    )
                )

                // Right Circular Shield Button (Triggers Battery Dialog)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), CircleShape)
                        .clickable { onEvent(PermissionHubUiEvent.OpenBatteryDialog) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Security Shield / Battery Access",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 0.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Header Hero Emblem & Explanation Notice
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, bottom = 2.dp)
                    ) {
                        // Hyper-Luminous Angular Heraldic 3D Shield Hero with Horizontal Light Streak
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(76.dp)
                        ) {
                            // Radial background aura
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                NeonCyan.copy(alpha = 0.50f),
                                                NeonGreen.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // 3D Beveled Shield Graphic + Light Streak
                            Canvas(modifier = Modifier.size(width = 54.dp, height = 64.dp)) {
                                val w = size.width
                                val h = size.height

                                val shieldPath = Path().apply {
                                    moveTo(w * 0.5f, 0f)
                                    lineTo(w * 0.82f, 0f)
                                    lineTo(w, h * 0.18f)
                                    cubicTo(w, h * 0.65f, w * 0.78f, h * 0.88f, w * 0.5f, h)
                                    cubicTo(w * 0.22f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.18f)
                                    lineTo(w * 0.18f, 0f)
                                    close()
                                }

                                // 1. Deep Metallic Dark Background Fill
                                drawPath(
                                    path = shieldPath,
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF13283E), Color(0xFF091422))
                                    ),
                                    style = Fill
                                )

                                // 2. Central Ambient Turquoise Glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(NeonCyan.copy(alpha = 0.55f), Color.Transparent),
                                        center = Offset(w * 0.5f, h * 0.52f),
                                        radius = w * 0.45f
                                    )
                                )

                                // 3. Thick 3D Beveled Outer Luminous Rim (Cyan to Bio-Green)
                                drawPath(
                                    path = shieldPath,
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00E5FF),
                                            Color(0xFF00F2FE),
                                            Color(0xFF00FFA3),
                                            Color(0xFF00E676)
                                        )
                                    ),
                                    style = Stroke(width = 3.5.dp.toPx())
                                )

                                // 4. Luminous Horizontal Light Streak / Flare
                                drawLine(
                                    brush = Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            NeonCyan.copy(alpha = 0.95f),
                                            Color.White,
                                            NeonCyan.copy(alpha = 0.95f),
                                            Color.Transparent
                                        )
                                    ),
                                    start = Offset(-18.dp.toPx(), h * 0.46f),
                                    end = Offset(w + 18.dp.toPx(), h * 0.46f),
                                    strokeWidth = 2.0.dp.toPx()
                                )

                                // 5. Glossy Padlock
                                val lockCenterX = w * 0.5f
                                val lockCenterY = h * 0.53f

                                val shackleR = 5.5.dp.toPx()
                                val shackleTop = lockCenterY - 11.dp.toPx()
                                val shPath = Path().apply {
                                    moveTo(lockCenterX - shackleR, lockCenterY - 2.dp.toPx())
                                    lineTo(lockCenterX - shackleR, shackleTop + shackleR)
                                    arcTo(
                                        rect = androidx.compose.ui.geometry.Rect(
                                            left = lockCenterX - shackleR,
                                            top = shackleTop,
                                            right = lockCenterX + shackleR,
                                            bottom = shackleTop + 2 * shackleR
                                        ),
                                        startAngleDegrees = 180f,
                                        sweepAngleDegrees = 180f,
                                        forceMoveTo = false
                                    )
                                    lineTo(lockCenterX + shackleR, lockCenterY - 2.dp.toPx())
                                }
                                drawPath(
                                    path = shPath,
                                    brush = Brush.verticalGradient(listOf(Color.White, Color(0xFF00E5FF))),
                                    style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
                                )

                                val bWidth = 16.dp.toPx()
                                val bHeight = 13.dp.toPx()
                                val bLeft = lockCenterX - bWidth / 2f
                                val bTop = lockCenterY - 2.dp.toPx()
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFF00F2FE), Color(0xFF0077B6))
                                    ),
                                    topLeft = Offset(bLeft, bTop),
                                    size = Size(bWidth, bHeight),
                                    cornerRadius = CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx())
                                )
                                drawCircle(
                                    color = Color(0xFF0A192F),
                                    radius = 1.6.dp.toPx(),
                                    center = Offset(lockCenterX, bTop + 5.dp.toPx())
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Explanatory Subtitle
                        Text(
                            text = "To inspect screen frames and audio on-device, SafeScreen requires specific Android permissions.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(1.dp))

                        Text(
                            text = "Your data is strictly private.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Dynamic Permission Cards List
                items(
                    items = uiState.permissions,
                    key = { it.type.name }
                ) { permission ->
                    PermissionCard(
                        item = permission,
                        onGrantClick = {
                            onEvent(PermissionHubUiEvent.RequestPermission(permission.type))
                        }
                    )
                }
            }

            // Bottom Sticky Action Bar: "Enter Shield Hub"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (uiState.areAllGranted) {
                                Brush.horizontalGradient(listOf(NeonCyan, NeonGreen))
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF131D2D).copy(alpha = 0.95f),
                                        Color(0xFF0C1320).copy(alpha = 0.85f)
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (uiState.areAllGranted) NeonCyan else Color.White.copy(alpha = 0.18f),
                            RoundedCornerShape(24.dp)
                        )
                        .clickable {
                            onEvent(PermissionHubUiEvent.EnterShieldHub)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (uiState.areAllGranted) DeepBlack else TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enter Shield Hub",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.areAllGranted) DeepBlack else TextSecondary
                        )
                    )
                }
            }
        }

        // ModalBottomSheet for Battery Optimization
        if (uiState.showBatteryDialog) {
            BatteryOptimizationDialog(
                onDismiss = { onEvent(PermissionHubUiEvent.DismissBatteryDialog) },
                onAllowUnrestricted = { onEvent(PermissionHubUiEvent.AllowUnrestrictedBattery) }
            )
        }
    }
}

/**
 * Reusable Glassmorphic Card for each system permission item.
 */
@Composable
private fun PermissionCard(
    item: PermissionItemState,
    onGrantClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // High-End Custom 3D Neon Permission Icon Frame
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(item.iconTint.copy(alpha = 0.10f))
                    .border(
                        width = 1.dp,
                        color = item.iconTint.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                PermissionCustomIcon(type = item.type, tint = item.iconTint)
            }

            // Title, Code Name & Description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = item.systemCodeName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        color = TextMuted,
                        letterSpacing = 0.2.sp
                    )
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = TextSecondary
                    )
                )
            }

            // Right Status Button: Sleek "(✔) Granted" pill OR "Grant Access" Action Button
            if (item.isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF003822).copy(alpha = 0.85f))
                        .border(
                            width = 1.dp,
                            color = NeonGreen.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 11.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(NeonGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Granted",
                            tint = DeepBlack,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Granted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = NeonGreen
                        )
                    )
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = item.actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = DeepBlack
                        )
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Custom 3D Glowing Icon for each permission type.
 */
@Composable
private fun PermissionCustomIcon(type: PermissionType, tint: Color) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val w = size.width
        val h = size.height

        when (type) {
            PermissionType.OVERLAY -> {
                // 3D Layered Isometric Cyan Squares
                val p1 = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.35f)
                    lineTo(w * 0.5f, h * 0.55f)
                    lineTo(w * 0.15f, h * 0.35f)
                    close()
                }
                drawPath(path = p1, color = tint.copy(alpha = 0.9f), style = Stroke(width = 1.8.dp.toPx()))

                val p2 = Path().apply {
                    moveTo(w * 0.15f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.75f)
                    lineTo(w * 0.85f, h * 0.55f)
                }
                drawPath(path = p2, color = tint.copy(alpha = 0.65f), style = Stroke(width = 1.8.dp.toPx()))

                val p3 = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f)
                    lineTo(w * 0.5f, h * 0.95f)
                    lineTo(w * 0.85f, h * 0.75f)
                }
                drawPath(path = p3, color = tint.copy(alpha = 0.40f), style = Stroke(width = 1.8.dp.toPx()))
            }
            PermissionType.SCREEN_CAPTURE -> {
                // Glowing Purple Viewfinder Screen Scan
                val inset = 3.dp.toPx()
                val arm = 6.dp.toPx()
                val strW = 2.dp.toPx()

                // Corner ticks
                drawLine(tint, Offset(inset, inset), Offset(inset + arm, inset), strokeWidth = strW, cap = StrokeCap.Round)
                drawLine(tint, Offset(inset, inset), Offset(inset, inset + arm), strokeWidth = strW, cap = StrokeCap.Round)

                drawLine(tint, Offset(w - inset, inset), Offset(w - inset - arm, inset), strokeWidth = strW, cap = StrokeCap.Round)
                drawLine(tint, Offset(w - inset, inset), Offset(w - inset, inset + arm), strokeWidth = strW, cap = StrokeCap.Round)

                drawLine(tint, Offset(inset, h - inset), Offset(inset + arm, h - inset), strokeWidth = strW, cap = StrokeCap.Round)
                drawLine(tint, Offset(inset, h - inset), Offset(inset, h - inset - arm), strokeWidth = strW, cap = StrokeCap.Round)

                drawLine(tint, Offset(w - inset, h - inset), Offset(w - inset - arm, h - inset), strokeWidth = strW, cap = StrokeCap.Round)
                drawLine(tint, Offset(w - inset, h - inset), Offset(w - inset, h - inset - arm), strokeWidth = strW, cap = StrokeCap.Round)

                // Center mini frame
                drawRoundRect(
                    color = tint.copy(alpha = 0.8f),
                    topLeft = Offset(w * 0.28f, h * 0.28f),
                    size = Size(w * 0.44f, h * 0.44f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            PermissionType.AUDIO_CAPTURE -> {
                // Glowing Emerald Microphone
                val micWidth = 10.dp.toPx()
                val micHeight = 16.dp.toPx()
                val micLeft = (w - micWidth) / 2f
                val micTop = h * 0.18f

                drawRoundRect(
                    color = tint,
                    topLeft = Offset(micLeft, micTop),
                    size = Size(micWidth, micHeight),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )

                // U-cup cradle
                val cradlePath = Path().apply {
                    moveTo(micLeft - 4.dp.toPx(), micTop + 7.dp.toPx())
                    cubicTo(
                        micLeft - 4.dp.toPx(), micTop + micHeight + 6.dp.toPx(),
                        micLeft + micWidth + 4.dp.toPx(), micTop + micHeight + 6.dp.toPx(),
                        micLeft + micWidth + 4.dp.toPx(), micTop + 7.dp.toPx()
                    )
                }
                drawPath(path = cradlePath, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round))

                // Stem & base
                drawLine(tint, Offset(w * 0.5f, micTop + micHeight + 6.dp.toPx()), Offset(w * 0.5f, h * 0.86f), strokeWidth = 2.dp.toPx())
                drawLine(tint, Offset(w * 0.32f, h * 0.86f), Offset(w * 0.68f, h * 0.86f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
            PermissionType.ACCESSIBILITY -> {
                // Glowing Violet Person / Accessibility Glyph
                // Head
                drawCircle(color = tint, radius = 3.5.dp.toPx(), center = Offset(w * 0.5f, h * 0.22f))
                // Arms (curved arc)
                val armsPath = Path().apply {
                    moveTo(w * 0.18f, h * 0.45f)
                    cubicTo(w * 0.35f, h * 0.35f, w * 0.65f, h * 0.35f, w * 0.82f, h * 0.45f)
                }
                drawPath(path = armsPath, color = tint, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                // Body & Legs
                drawLine(tint, Offset(w * 0.5f, h * 0.36f), Offset(w * 0.5f, h * 0.60f), strokeWidth = 2.2.dp.toPx())
                drawLine(tint, Offset(w * 0.5f, h * 0.60f), Offset(w * 0.30f, h * 0.88f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                drawLine(tint, Offset(w * 0.5f, h * 0.60f), Offset(w * 0.70f, h * 0.88f), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            }
            PermissionType.NOTIFICATION -> {
                // Glowing Green Bell
                val bellPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.18f)
                    cubicTo(w * 0.72f, h * 0.18f, w * 0.82f, h * 0.55f, w * 0.88f, h * 0.70f)
                    lineTo(w * 0.12f, h * 0.70f)
                    cubicTo(w * 0.18f, h * 0.55f, w * 0.28f, h * 0.18f, w * 0.5f, h * 0.18f)
                    close()
                }
                drawPath(path = bellPath, color = tint, style = Stroke(width = 2.dp.toPx()))
                // Clapper
                drawCircle(color = tint, radius = 2.5.dp.toPx(), center = Offset(w * 0.5f, h * 0.82f))
            }
        }
    }
}

@Preview(name = "Permission Hub - Initial State", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun PermissionHubScreenInitialPreview() {
    SafeScreenTheme {
        PermissionHubContent(
            uiState = PermissionHubUiState(),
            onEvent = {}
        )
    }
}
