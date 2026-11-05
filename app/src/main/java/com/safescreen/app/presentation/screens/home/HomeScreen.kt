package com.safescreen.app.presentation.screens.home

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.safescreen.app.services.permission.PermissionManager

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.components.NeonSwitch
import com.safescreen.app.presentation.components.ShieldButton
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.services.overlay.FocusInterventionOverlayManager
import com.safescreen.app.services.overlay.LiveBlurOverlayManager

/**
 * Flagship Home Dashboard for SafeScreen (Screen 05 / Module 03).
 *
 * Implements the full Cyber-Dark Glassmorphism aesthetic featuring:
 * - Top brand bar with glowing live status pill & settings action.
 * - Central 3D beveled pulsing ShieldButton with circuit traces.
 * - Live Metric Cards for explicit visuals blurred and music tracks muted.
 * - Reels & Shorts daily usage banner with neon purple progress indicator.
 * - Quick feature toggles for Visual Shield, Audio Mute, and Reels Blocker.
 * - Bottom floating cyber glass navigation pill.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToStats: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToFocus: () -> Unit = {},
    onNavigateToReels: () -> Unit = {},
    onNavigateToBlurPreview: () -> Unit = {},
    onNavigateToStates: () -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val mediaProjectionManager = remember {
        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            PermissionManager.setMediaProjectionResult(result.resultCode, result.data)
            viewModel.onEvent(HomeUiEvent.ToggleShield)
        }
    }

    HomeScreenContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is HomeUiEvent.ToggleShield -> {
                    if (state.isShieldActive) {
                        viewModel.onEvent(event)
                    } else {
                        if (PermissionManager.hasMediaProjectionConsent) {
                            viewModel.onEvent(event)
                        } else {
                            screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                        }
                    }
                }
                is HomeUiEvent.NavigateToStats -> onNavigateToStats()
                is HomeUiEvent.NavigateToSettings -> onNavigateToSettings()
                is HomeUiEvent.NavigateToFocus -> onNavigateToFocus()
                is HomeUiEvent.NavigateToReels -> onNavigateToReels()
                is HomeUiEvent.NavigateToBlurPreview -> {
                    viewModel.onEvent(event)
                    onNavigateToBlurPreview()
                }
                is HomeUiEvent.NavigateToStates -> onNavigateToStates()
                is HomeUiEvent.TriggerLiveBlurDemo -> {
                    LiveBlurOverlayManager.showOverlay(
                        context = context,
                        threatConfidence = 0.98f,
                        threatType = "Harmful Content Blocked"
                    )
                }
                is HomeUiEvent.TriggerFocusInterventionDemo -> {
                    FocusInterventionOverlayManager.showOverlay(
                        context = context,
                        appName = "Instagram Reels",
                        limitMinutes = 30
                    )
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onEvent: (HomeUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // 1. Ambient Top Radial Glow Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = if (uiState.isShieldActive) 0.16f else 0.04f),
                        NeonGreen.copy(alpha = if (uiState.isShieldActive) 0.06f else 0.01f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.22f),
                    radius = size.width * 0.70f
                )
            )
        }

        // 2. Signature Bottom Cyber Silk Curves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 190.dp,
            intensity = if (uiState.isShieldActive) 1.0f else 0.4f
        )

        // 3. Scrollable Main Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // A. Top Bar
            HomeTopBar(
                isShieldActive = uiState.isShieldActive,
                isVisualThreat = uiState.isAiVisualThreat,
                isAudioThreat = uiState.isAiAudioThreat,
                onCustomizeBlurClick = { onEvent(HomeUiEvent.OpenBlurStyleSheet) },
                onSettingsClick = { onEvent(HomeUiEvent.NavigateToSettings) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // B. Hero 3D Shield Button with Circuit Traces & Pulse Aura
            ShieldButton(
                isActive = uiState.isShieldActive,
                onToggle = { onEvent(HomeUiEvent.ToggleShield) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // C. Live Metric Cards Grid (Explicit Blurred & Music Muted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Card 1: Explicit Blurred
                MetricStatCard(
                    title = "Explicit Blurred",
                    value = "${uiState.blurredTodayCount}",
                    subtitle = "Today's interventions",
                    icon = Icons.Default.VisibilityOff,
                    accentColor = NeonPink,
                    onClick = { onEvent(HomeUiEvent.NavigateToStats) },
                    modifier = Modifier.weight(1f)
                )

                // Card 2: Music Muted
                MetricStatCard(
                    title = "Music Muted",
                    value = "${uiState.mutedTodayCount}",
                    subtitle = "Audio tracks silenced",
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    accentColor = NeonCyan,
                    onClick = { onEvent(HomeUiEvent.NavigateToStats) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D. Focus Widget: Reels & Shorts Daily Usage Banner
            FocusUsageBanner(
                minutesUsed = uiState.reelsMinutesUsed,
                minutesLimit = uiState.reelsMinutesLimit,
                percentage = uiState.reelsPercentage,
                onClick = { onEvent(HomeUiEvent.NavigateToReels) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // E. Quick Feature Toggles (Visual Shield, Audio Mute, Reels Blocker)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Toggle 1: Visual Shield
                QuickFeatureToggleCard(
                    title = "Visual\nShield",
                    icon = Icons.Default.Visibility,
                    iconTint = NeonGreen,
                    isEnabled = uiState.isVisualShieldEnabled,
                    activeSwitchColor = NeonCyan,
                    onToggle = { onEvent(HomeUiEvent.SetVisualShield(it)) },
                    onCardClick = { onEvent(HomeUiEvent.OpenBlurStyleSheet) },
                    modifier = Modifier.weight(1f)
                )

                // Toggle 2: Audio Mute
                QuickFeatureToggleCard(
                    title = "Audio\nMute",
                    icon = Icons.AutoMirrored.Filled.VolumeOff,
                    iconTint = NeonCyan,
                    isEnabled = uiState.isAudioMuteEnabled,
                    activeSwitchColor = NeonCyan,
                    onToggle = { onEvent(HomeUiEvent.SetAudioMute(it)) },
                    modifier = Modifier.weight(1f)
                )

                // Toggle 3: Reels Blocker
                QuickFeatureToggleCard(
                    title = "Reels\nBlocker",
                    icon = Icons.Default.HourglassTop,
                    iconTint = NeonPurple,
                    isEnabled = uiState.isReelsBlockerEnabled,
                    activeSwitchColor = NeonPurple,
                    onToggle = { onEvent(HomeUiEvent.SetReelsBlocker(it)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // F. System Overlay Simulator Cards (Module 07 Demo Triggers)
            LiveBlurOverlaySimulatorCard(
                onTriggerClick = { onEvent(HomeUiEvent.TriggerLiveBlurDemo) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FocusInterventionSimulatorCard(
                onTriggerClick = { onEvent(HomeUiEvent.TriggerFocusInterventionDemo) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            FallbackStatesSimulatorCard(
                onTriggerClick = { onEvent(HomeUiEvent.NavigateToStates) }
            )

            // Extra bottom spacing to ensure content is never obscured by the floating nav bar
            Spacer(modifier = Modifier.height(105.dp))
        }

        // 4. Blur Style Customization Bottom Sheet (Screen 06 / Module 03)
        if (uiState.showBlurPreviewSheet) {
            BlurStylePreviewSheet(
                initialStyle = uiState.currentBlurStyle,
                initialIntensity = uiState.currentBlurIntensity,
                onDismiss = { onEvent(HomeUiEvent.DismissBlurStyleSheet) },
                onApply = { style, intensity ->
                    onEvent(HomeUiEvent.ApplyBlurStyle(style, intensity))
                }
            )
        }
    }
}

/**
 * Top brand bar with glowing shield icon, title, active pill badge, palette button, and settings button.
 */
@Composable
private fun HomeTopBar(
    isShieldActive: Boolean,
    isVisualThreat: Boolean = false,
    isAudioThreat: Boolean = false,
    onCustomizeBlurClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "topBadgePulse")
    val dotPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulseScale"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Emblem & Title
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF132238),
                                Color(0xFF0A1320)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.7f),
                                NeonGreen.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "SafeScreen Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Row {
                Text(
                    text = "Safe",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "Screen",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        fontSize = 22.sp
                    )
                )
            }
        }

        // Live Status Pill & Settings Button
        val isThreatDetected = isShieldActive && (isVisualThreat || isAudioThreat)
        val pillColor = when {
            !isShieldActive -> TextMuted
            isThreatDetected -> NeonPink
            else -> NeonGreen
        }
        val pillText = when {
            !isShieldActive -> "DISARMED"
            isVisualThreat -> "NSFW BLURRED"
            isAudioThreat -> "AUDIO MUTED"
            else -> "AI SHIELD ACTIVE"
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Status Pill Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xCC0D1726))
                    .border(
                        width = 1.dp,
                        color = pillColor.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .scale(if (isShieldActive) dotPulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(pillColor)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = pillText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.5.sp,
                            color = pillColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Palette / Blur Customization Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x990D1726))
                    .border(
                        width = 1.dp,
                        color = NeonCyan.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onCustomizeBlurClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Customize Blur Style",
                    tint = NeonCyan,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Settings Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x990D1726))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

/**
 * Metric stat card displaying interventions count and category branding.
 */
@Composable
private fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        ),
        borderColor = accentColor.copy(alpha = 0.35f),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column {
            // Icon squircle and chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = accentColor.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextSecondary,
                    fontSize = 12.5.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 30.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 11.sp
                )
            )
        }
    }
}

/**
 * Banner card displaying Reels & Shorts daily usage with a neon purple progress bar.
 */
@Composable
private fun FocusUsageBanner(
    minutesUsed: Int,
    minutesLimit: Int,
    percentage: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        borderColor = NeonPurple.copy(alpha = 0.35f),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Purple Video Icon Squircle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF241539))
                    .border(
                        width = 1.dp,
                        color = NeonPurple.copy(alpha = 0.50f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Reels & Shorts Usage",
                    tint = NeonPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Center details: Title, limits, and progress bar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Reels & Shorts Usage:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$minutesUsed / $minutesLimit mins used today",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        fontSize = 13.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(7.dp))

                // Custom Sleek Neon Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E1630))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        NeonPurple,
                                        Color(0xFFC084FC)
                                    )
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Percentage value on the far right
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 15.sp
                )
            )
        }
    }
}

/**
 * Compact feature toggle card with icon, title, and smooth custom neon switch.
 */
@Composable
private fun QuickFeatureToggleCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    isEnabled: Boolean,
    activeSwitchColor: Color,
    onToggle: (Boolean) -> Unit,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                if (onCardClick != null) {
                    onCardClick()
                } else {
                    onToggle(!isEnabled)
                }
            }
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Neon Switch
            NeonSwitch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                activeColor = activeSwitchColor
            )
        }
    }
}

/**
 * Demo trigger card for Module 07 Live Blur WindowManager System Overlay.
 */
@Composable
private fun LiveBlurOverlaySimulatorCard(
    onTriggerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        borderColor = NeonCyan.copy(alpha = 0.35f),
        backgroundColor = Color(0xFF0F1B2C)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Live Blur Overlay",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Test WindowManager AI mask",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                NeonCyan,
                                Color(0xFF00E5FF)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTriggerClick
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Test Overlay",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Demo trigger card for Module 07 Focus Intervention WindowManager System Overlay.
 */
@Composable
private fun FocusInterventionSimulatorCard(
    onTriggerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        borderColor = NeonPurple.copy(alpha = 0.45f),
        backgroundColor = Color(0xFF151028)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPurple.copy(alpha = 0.18f))
                        .border(1.dp, NeonPurple.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFFE879F9),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Focus Intervention",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Test Reels & Shorts lockout",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFFC026D3)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTriggerClick
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Test Focus",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

/**
 * Demo trigger card for Module 08 Fallback, Warning & Empty States.
 */
@Composable
private fun FallbackStatesSimulatorCard(
    onTriggerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        borderColor = NeonGreen.copy(alpha = 0.40f),
        backgroundColor = Color(0xFF0C1918)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonGreen.copy(alpha = 0.18f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.50f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "System Fallback States",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Clean, Paused & AI Error states",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF00FFA3),
                                Color(0xFF00E676)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onTriggerClick
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Test States",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun HomeScreenActivePreview() {
    HomeScreenContent(
        uiState = HomeUiState(isShieldActive = true),
        onEvent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun HomeScreenDisarmedPreview() {
    HomeScreenContent(
        uiState = HomeUiState(isShieldActive = false),
        onEvent = {}
    )
}
