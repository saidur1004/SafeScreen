package com.safescreen.app.presentation.screens.permission

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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DarkSurface
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.GlassBorder
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

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
    val icon: ImageVector,
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
        isGranted = true,
        icon = Icons.Default.Layers,
        iconTint = NeonCyan,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.SCREEN_CAPTURE,
        title = "Screen Capture",
        systemCodeName = "MediaProjection",
        description = "Enables local AI frame inspection. No screenshots are ever saved.",
        isGranted = false,
        icon = Icons.Default.Screenshot,
        iconTint = NeonPurple,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.AUDIO_CAPTURE,
        title = "Audio Playback Capture",
        systemCodeName = "RECORD_AUDIO",
        description = "Allows detecting and muting background music tracks.",
        isGranted = false,
        icon = Icons.Default.Mic,
        iconTint = NeonGreen,
        actionLabel = "Grant Access"
    ),
    PermissionItemState(
        type = PermissionType.ACCESSIBILITY,
        title = "Accessibility Service",
        systemCodeName = "SafeScreenAccessibilityService",
        description = "Detects Reels & Shorts feeds to enforce your screen-time limits.",
        isGranted = false,
        icon = Icons.Default.AccessibilityNew,
        iconTint = NeonPurple,
        actionLabel = "Enable"
    ),
    PermissionItemState(
        type = PermissionType.NOTIFICATION,
        title = "Foreground Notification",
        systemCodeName = "POST_NOTIFICATIONS",
        description = "Keeps the real-time AI shield persistent without being killed by Android.",
        isGranted = true,
        icon = Icons.Default.Notifications,
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
 * Stateful entry point with default internal dummy state handler for preview and prototype testing.
 */
@Composable
fun PermissionHubScreen(
    onNavigateBack: () -> Unit = {},
    onAllPermissionsGranted: () -> Unit = {}
) {
    var state by remember { mutableStateOf(PermissionHubUiState()) }

    PermissionHubContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is PermissionHubUiEvent.RequestPermission -> {
                    state = state.copy(
                        permissions = state.permissions.map { item ->
                            if (item.type == event.type) item.copy(isGranted = true) else item
                        }
                    )
                }
                PermissionHubUiEvent.OpenBatteryDialog -> {
                    state = state.copy(showBatteryDialog = true)
                }
                PermissionHubUiEvent.DismissBatteryDialog -> {
                    state = state.copy(showBatteryDialog = false)
                }
                PermissionHubUiEvent.AllowUnrestrictedBattery -> {
                    state = state.copy(
                        showBatteryDialog = false,
                        isBatteryOptimizedExempted = true
                    )
                }
                PermissionHubUiEvent.EnterShieldHub -> {
                    if (state.areAllGranted) {
                        onAllPermissionsGranted()
                    }
                }
                PermissionHubUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
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
                    .padding(top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Frosted Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        .clickable { onEvent(PermissionHubUiEvent.NavigateBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Title
                Text(
                    text = "System Permissions",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                )

                // Right Circular Shield Button (Triggers Battery Dialog)
                Box(
                    modifier = Modifier
                        .size(42.dp)
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Hero Badge & Explanation Notice
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        // Glowing 3D Shield Emblem with Padlock
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(110.dp)
                        ) {
                            // Radial background aura
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                NeonCyan.copy(alpha = 0.40f),
                                                NeonGreen.copy(alpha = 0.12f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Beveled Shield Graphic
                            Canvas(modifier = Modifier.size(width = 68.dp, height = 78.dp)) {
                                val w = size.width
                                val h = size.height
                                val shieldPath = Path().apply {
                                    moveTo(w * 0.5f, 0f)
                                    cubicTo(w * 0.75f, 0f, w * 0.95f, h * 0.08f, w, h * 0.18f)
                                    cubicTo(w, h * 0.65f, w * 0.75f, h * 0.88f, w * 0.5f, h)
                                    cubicTo(w * 0.25f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.18f)
                                    cubicTo(w * 0.05f, h * 0.08f, w * 0.25f, 0f, w * 0.5f, 0f)
                                    close()
                                }
                                drawPath(
                                    path = shieldPath,
                                    brush = Brush.verticalGradient(listOf(Color(0xFF132238), Color(0xFF0A131F))),
                                    style = Fill
                                )
                                drawPath(
                                    path = shieldPath,
                                    brush = Brush.horizontalGradient(listOf(NeonCyan, NeonGreen)),
                                    style = Stroke(width = 2.5.dp.toPx())
                                )
                            }

                            // Center Padlock
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy Shield",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Explanatory Subtitle
                        Text(
                            text = "To inspect screen frames and audio on-device, SafeScreen requires specific Android permissions.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "Your data is strictly private.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Interactive Battery Optimization Banner Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (uiState.isBatteryOptimizedExempted) {
                                        NeonGreen.copy(alpha = 0.12f)
                                    } else {
                                        WarningYellow.copy(alpha = 0.10f)
                                    }
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (uiState.isBatteryOptimizedExempted) {
                                        NeonGreen.copy(alpha = 0.45f)
                                    } else {
                                        WarningYellow.copy(alpha = 0.40f)
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onEvent(PermissionHubUiEvent.OpenBatteryDialog) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = if (uiState.isBatteryOptimizedExempted) Icons.Default.Check else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (uiState.isBatteryOptimizedExempted) NeonGreen else WarningYellow,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isBatteryOptimizedExempted) {
                                    "Unrestricted Battery: Active ✔"
                                } else {
                                    "Unrestricted Battery Access (Tap to configure)"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (uiState.isBatteryOptimizedExempted) NeonGreen else WarningYellow
                                )
                            )
                        }
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
                    .padding(vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(
                            if (uiState.areAllGranted) {
                                Brush.horizontalGradient(listOf(NeonCyan, NeonGreen))
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF131D2D).copy(alpha = 0.9f),
                                        Color(0xFF0C1320).copy(alpha = 0.7f)
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (uiState.areAllGranted) NeonCyan else Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(27.dp)
                        )
                        .clickable(enabled = uiState.areAllGranted) {
                            onEvent(PermissionHubUiEvent.EnterShieldHub)
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = if (uiState.areAllGranted) DeepBlack else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enter Shield Hub",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 16.sp,
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
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Icon Frame with Neon Aura
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.iconTint.copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = item.iconTint.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Radial glow backing
                Canvas(modifier = Modifier.size(48.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                item.iconTint.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                }

                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title, Code Name & Description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color.White
                    )
                )

                Text(
                    text = item.systemCodeName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = TextMuted,
                        letterSpacing = 0.3.sp
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary
                    )
                )
            }

            // Right Status: "✔ Granted" badge OR "Grant Access" Action Button
            if (item.isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF003822))
                        .border(
                            width = 1.dp,
                            color = NeonGreen.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Granted",
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = DeepBlack
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = item.actionLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DeepBlack
                        )
                    )
                }
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

@Preview(name = "Permission Hub - All Granted", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun PermissionHubScreenAllGrantedPreview() {
    val allGrantedPermissions = defaultPermissions.map { it.copy(isGranted = true) }
    SafeScreenTheme {
        PermissionHubContent(
            uiState = PermissionHubUiState(
                permissions = allGrantedPermissions,
                isBatteryOptimizedExempted = true
            ),
            onEvent = {}
        )
    }
}
