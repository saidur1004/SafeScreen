package com.safescreen.app.presentation.screens.focus

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.components.NeonSwitch
import com.safescreen.app.presentation.components.TimeLimitSlider
import com.safescreen.app.presentation.components.formatMinutes
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary

/**
 * Reels & Shorts Control Screen (Module 04 / Screen 07).
 *
 * Provides granular control over short-video platforms with:
 * - Top back navigation & header.
 * - Hero illustration with 3D shield & phone graphic.
 * - Global "Strict Mode" toggle.
 * - Manage Apps feed with custom time limit sliders.
 * - Prominent glowing dashed "+ Add Custom App" trigger & ModalBottomSheet.
 * - Intervention Strategy selector (Soft Warning, Hard Lockout, Friction Delay).
 */
@Composable
fun ReelsControlScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {}
) {
    var state by remember { mutableStateOf(ReelsUiState()) }

    ReelsControlContent(
        uiState = state,
        onNavigateToSchedule = onNavigateToSchedule,
        onEvent = { event ->
            when (event) {
                is ReelsUiEvent.ToggleStrictMode -> {
                    state = state.copy(isStrictModeEnabled = !state.isStrictModeEnabled)
                }
                is ReelsUiEvent.UpdateAppTimeLimit -> {
                    state = state.copy(
                        managedApps = state.managedApps.map {
                            if (it.id == event.appId) it.copy(timeLimitMinutes = event.minutes) else it
                        }
                    )
                }
                is ReelsUiEvent.ToggleAppEnabled -> {
                    state = state.copy(
                        managedApps = state.managedApps.map {
                            if (it.id == event.appId) it.copy(isEnabled = event.enabled) else it
                        }
                    )
                }
                is ReelsUiEvent.SelectStrategy -> {
                    state = state.copy(selectedStrategy = event.strategy)
                }
                is ReelsUiEvent.OpenAddAppSheet -> {
                    state = state.copy(showAddAppSheet = true)
                }
                is ReelsUiEvent.DismissAddAppSheet -> {
                    state = state.copy(showAddAppSheet = false)
                }
                is ReelsUiEvent.AddApp -> {
                    state = state.copy(
                        managedApps = state.managedApps + event.app,
                        availableAppsToAdd = state.availableAppsToAdd.filter { it.id != event.app.id },
                        showAddAppSheet = false
                    )
                }
                is ReelsUiEvent.UpdateSearchQuery -> {
                    state = state.copy(searchQuery = event.query)
                }
                is ReelsUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    )
}

@Composable
fun ReelsControlContent(
    uiState: ReelsUiState,
    onEvent: (ReelsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSchedule: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Bottom Ambient Waves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 200.dp,
            intensity = 0.95f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Top Bar: Back Arrow + Focus Schedules Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x990D1726))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { onEvent(ReelsUiEvent.NavigateBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Reels & Shorts Control",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                )

                // Top-Right Schedules Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.12f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.40f), CircleShape)
                        .clickable { onNavigateToSchedule() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Focus Schedules",
                        tint = NeonCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Hero Header Section with 3D Phone & Shield Illustration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.35f)) {
                    Text(
                        text = "Reels & Digital\nWellbeing Control",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 24.sp,
                            lineHeight = 30.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Manage screen time and block unwanted apps for a healthier digital life.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    )
                }

                // 3D Hero Illustration Canvas (Phone + Shield + Hourglass + Clock)
                HeroIllustrationCanvas(
                    modifier = Modifier
                        .weight(0.95f)
                        .height(130.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Strict Mode Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (uiState.isStrictModeEnabled) NeonCyan.copy(alpha = 0.50f) else Color.White.copy(alpha = 0.15f),
                backgroundColor = Color(0xFF0F1B2F),
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shield Icon Squircle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.50f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Strict Mode",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Strict Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Block apps once the time limit is reached.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    // Strict Mode Toggle Switch
                    NeonSwitch(
                        checked = uiState.isStrictModeEnabled,
                        onCheckedChange = { onEvent(ReelsUiEvent.ToggleStrictMode) },
                        activeColor = NeonCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Manage Apps Section Header
            Text(
                text = "Manage Apps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Set time limits for social media and entertainment apps.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Managed Apps Feed
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                uiState.managedApps.forEach { app ->
                    ManagedAppCard(
                        app = app,
                        onTimeLimitChange = { onEvent(ReelsUiEvent.UpdateAppTimeLimit(app.id, it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 6. CRUCIAL NEW FEATURE: "+ Add Custom App" Glowing Dashed Button
            AddCustomAppTriggerButton(
                onClick = { onEvent(ReelsUiEvent.OpenAddAppSheet) }
            )

            Spacer(modifier = Modifier.height(26.dp))

            // 7. Intervention Strategy Section
            Text(
                text = "Intervention Strategy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Choose how SafeScreen should respond when time expires.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Radio-Style Strategy Selection Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InterventionStrategyCard(
                    strategy = InterventionStrategy.SOFT_WARNING,
                    icon = Icons.Default.NotificationsActive,
                    iconTint = NeonCyan,
                    isSelected = uiState.selectedStrategy == InterventionStrategy.SOFT_WARNING,
                    onClick = { onEvent(ReelsUiEvent.SelectStrategy(InterventionStrategy.SOFT_WARNING)) },
                    modifier = Modifier.weight(1f)
                )

                InterventionStrategyCard(
                    strategy = InterventionStrategy.HARD_LOCKOUT,
                    icon = Icons.Default.Lock,
                    iconTint = NeonPurple,
                    isSelected = uiState.selectedStrategy == InterventionStrategy.HARD_LOCKOUT,
                    onClick = { onEvent(ReelsUiEvent.SelectStrategy(InterventionStrategy.HARD_LOCKOUT)) },
                    modifier = Modifier.weight(1f)
                )

                InterventionStrategyCard(
                    strategy = InterventionStrategy.FRICTION_DELAY,
                    icon = Icons.Default.HourglassTop,
                    iconTint = NeonGreen,
                    isSelected = uiState.selectedStrategy == InterventionStrategy.FRICTION_DELAY,
                    onClick = { onEvent(ReelsUiEvent.SelectStrategy(InterventionStrategy.FRICTION_DELAY)) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // 8. "Add Custom App" Modal Bottom Sheet
        if (uiState.showAddAppSheet) {
            AddCustomAppBottomSheet(
                availableApps = uiState.availableAppsToAdd,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { onEvent(ReelsUiEvent.UpdateSearchQuery(it)) },
                onAddApp = { onEvent(ReelsUiEvent.AddApp(it)) },
                onDismiss = { onEvent(ReelsUiEvent.DismissAddAppSheet) }
            )
        }
    }
}

/**
 * Card representing a monitored application with brand icon, title, time readout,
 * and a tactile time limit slider.
 */
@Composable
private fun ManagedAppCard(
    app: ManagedAppItem,
    onTimeLimitChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Brand Icon
                AppBrandIcon(brandType = app.brandType, appName = app.name)

                Spacer(modifier = Modifier.width(14.dp))

                // App Name
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Time Badge with Clock Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D1726))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = formatMinutes(app.timeLimitMinutes),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.5.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tactile Time Limit Slider
            TimeLimitSlider(
                valueMinutes = app.timeLimitMinutes,
                onValueChange = onTimeLimitChange,
                minMinutes = 15,
                maxMinutes = 180,
                stepMinutes = 15,
                activeColor = NeonCyan
            )
        }
    }
}

/**
 * Brand-accurate Squircle icon renderer for social applications.
 */
@Composable
fun AppBrandIcon(
    brandType: AppBrandType,
    appName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (brandType) {
            AppBrandType.TIKTOK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF010101)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "TikTok",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AppBrandType.INSTAGRAM -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF833AB4),
                                    Color(0xFFFD1D1D),
                                    Color(0xFFFCB045)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Instagram",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AppBrandType.YOUTUBE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF0000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            AppBrandType.FACEBOOK -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1877F2)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "f",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                    )
                }
            }

            AppBrandType.REDDIT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFF4500)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "r/",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
            }

            AppBrandType.TWITTER_X -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F1419)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "𝕏",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    )
                }
            }

            AppBrandType.SNAPCHAT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFFC00)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "Snapchat",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AppBrandType.NETFLIX -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF141414)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE50914),
                            fontSize = 22.sp
                        )
                    )
                }
            }

            AppBrandType.GENERIC -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E2838)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }
            }
        }
    }
}

/**
 * CRUCIAL FEATURE: Glowing Dashed-Border "+ Add Custom App" Button with Light Rays.
 */
@Composable
private fun AddCustomAppTriggerButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Radiating Cyber Rays Canvas
            Canvas(modifier = Modifier.size(width = 30.dp, height = 36.dp)) {
                val rayWidth = 1.8.dp.toPx()
                drawLine(
                    color = NeonCyan,
                    start = Offset(size.width, size.height * 0.15f),
                    end = Offset(size.width * 0.2f, 0f),
                    strokeWidth = rayWidth
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(size.width, size.height * 0.50f),
                    end = Offset(0f, size.height * 0.50f),
                    strokeWidth = rayWidth
                )
                drawLine(
                    color = NeonCyan,
                    start = Offset(size.width, size.height * 0.85f),
                    end = Offset(size.width * 0.2f, size.height),
                    strokeWidth = rayWidth
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Dashed Glowing Capsule Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x22131D2D))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Dashed Border Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = Stroke(
                        width = 1.6.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                NeonCyan,
                                NeonPurple
                            )
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx()),
                        style = stroke
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom App",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Custom App",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Radiating Cyber Rays Canvas (Symmetrical)
            Canvas(modifier = Modifier.size(width = 30.dp, height = 36.dp)) {
                val rayWidth = 1.8.dp.toPx()
                drawLine(
                    color = NeonPurple,
                    start = Offset(0f, size.height * 0.15f),
                    end = Offset(size.width * 0.8f, 0f),
                    strokeWidth = rayWidth
                )
                drawLine(
                    color = NeonPurple,
                    start = Offset(0f, size.height * 0.50f),
                    end = Offset(size.width, size.height * 0.50f),
                    strokeWidth = rayWidth
                )
                drawLine(
                    color = NeonPurple,
                    start = Offset(0f, size.height * 0.85f),
                    end = Offset(size.width * 0.8f, size.height),
                    strokeWidth = rayWidth
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Select any installed app to block",
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

/**
 * Radio-style intervention strategy card.
 */
@Composable
private fun InterventionStrategyCard(
    strategy: InterventionStrategy,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) iconTint else Color.White.copy(alpha = 0.12f)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101927))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Radio Icon Indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = if (isSelected) 0.20f else 0.08f))
                    .border(1.dp, iconTint.copy(alpha = if (isSelected) 0.60f else 0.25f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = strategy.title,
                    tint = iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = strategy.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) iconTint else TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = strategy.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mini Radio Dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) iconTint else Color(0xFF1E2838))
                    .border(1.dp, if (isSelected) iconTint else Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(DeepBlack)
                    )
                }
            }
        }
    }
}

/**
 * "Add Custom App" Modal Bottom Sheet for selecting any installed application.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCustomAppBottomSheet(
    availableApps: List<ManagedAppItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAddApp: (ManagedAppItem) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredApps = remember(availableApps, searchQuery) {
        if (searchQuery.isBlank()) {
            availableApps
        } else {
            availableApps.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(NeonPurple.copy(alpha = 0.15f))
                        .border(1.dp, NeonPurple.copy(alpha = 0.40f), RoundedCornerShape(11.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add App",
                        tint = NeonPurple,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Add Custom App to Block",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose any installed app to enforce daily limits.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101927))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(NeonCyan),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search installed apps...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available Apps List
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No applications found.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    items(filteredApps, key = { it.id }) { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F1826))
                                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            AppBrandIcon(brandType = app.brandType, appName = app.name)

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            // Add Button Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(NeonCyan.copy(alpha = 0.15f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.50f), RoundedCornerShape(18.dp))
                                    .clickable { onAddApp(app) }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+ Add",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Done Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(Color(0xFF1E2838))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(25.dp))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

/**
 * 3D Hero Graphic Canvas depicting a glowing phone, an orbital ring, a shield with hourglass, and a ticking clock.
 */
@Composable
private fun HeroIllustrationCanvas(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Phone Silhouette leaning back
        val phoneW = w * 0.48f
        val phoneH = h * 0.85f
        val phoneLeft = w * 0.40f
        val phoneTop = h * 0.08f

        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF1E3A5F),
                    Color(0xFF0F172A)
                )
            ),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        drawRoundRect(
            color = NeonCyan.copy(alpha = 0.45f),
            topLeft = Offset(phoneLeft, phoneTop),
            size = Size(phoneW, phoneH),
            cornerRadius = CornerRadius(14.dp.toPx()),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // 2. Orbital Neon Glow Ring around the shield
        drawOval(
            brush = Brush.linearGradient(
                listOf(NeonCyan, NeonPurple, Color.Transparent)
            ),
            topLeft = Offset(w * 0.05f, h * 0.40f),
            size = Size(w * 0.90f, h * 0.45f),
            style = Stroke(width = 1.4.dp.toPx())
        )

        // 3. Futuristic 3D Blue Shield
        val shieldW = w * 0.46f
        val shieldH = h * 0.70f
        val shieldLeft = w * 0.08f
        val shieldTop = h * 0.15f

        val shieldPath = Path().apply {
            moveTo(shieldLeft + shieldW * 0.5f, shieldTop)
            cubicTo(
                shieldLeft + shieldW * 0.75f, shieldTop,
                shieldLeft + shieldW * 0.95f, shieldTop + shieldH * 0.08f,
                shieldLeft + shieldW, shieldTop + shieldH * 0.18f
            )
            cubicTo(
                shieldLeft + shieldW, shieldTop + shieldH * 0.65f,
                shieldLeft + shieldW * 0.75f, shieldTop + shieldH * 0.88f,
                shieldLeft + shieldW * 0.5f, shieldTop + shieldH
            )
            cubicTo(
                shieldLeft + shieldW * 0.25f, shieldTop + shieldH * 0.88f,
                shieldLeft, shieldTop + shieldH * 0.65f,
                shieldLeft, shieldTop + shieldH * 0.18f
            )
            cubicTo(
                shieldLeft + shieldW * 0.05f, shieldTop + shieldH * 0.08f,
                shieldLeft + shieldW * 0.25f, shieldTop,
                shieldLeft + shieldW * 0.5f, shieldTop
            )
            close()
        }

        drawPath(
            path = shieldPath,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFF0284C7),
                    Color(0xFF0369A1),
                    Color(0xFF075985)
                )
            )
        )
        drawPath(
            path = shieldPath,
            brush = Brush.horizontalGradient(
                listOf(NeonCyan, NeonPurple)
            ),
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Hourglass icon inside the shield
        val hgX = shieldLeft + shieldW * 0.5f
        val hgY = shieldTop + shieldH * 0.48f
        val hgSize = 13.dp.toPx()

        val hourglassPath = Path().apply {
            moveTo(hgX - hgSize, hgY - hgSize)
            lineTo(hgX + hgSize, hgY - hgSize)
            lineTo(hgX, hgY)
            lineTo(hgX + hgSize, hgY + hgSize)
            lineTo(hgX - hgSize, hgY + hgSize)
            lineTo(hgX, hgY)
            close()
        }
        drawPath(hourglassPath, color = Color.White, style = Stroke(width = 2.dp.toPx()))

        // 4. Glowing Clock Orb in bottom right
        val clockRadius = 18.dp.toPx()
        val clockCenter = Offset(w * 0.76f, h * 0.68f)

        drawCircle(
            color = Color(0xFF0B1728),
            radius = clockRadius,
            center = clockCenter
        )
        drawCircle(
            color = NeonCyan,
            radius = clockRadius,
            center = clockCenter,
            style = Stroke(width = 1.6.dp.toPx())
        )

        // Clock Hands
        drawLine(
            color = Color.White,
            start = clockCenter,
            end = Offset(clockCenter.x, clockCenter.y - clockRadius * 0.60f),
            strokeWidth = 1.8.dp.toPx()
        )
        drawLine(
            color = NeonCyan,
            start = clockCenter,
            end = Offset(clockCenter.x + clockRadius * 0.45f, clockCenter.y),
            strokeWidth = 1.6.dp.toPx()
        )

        // Ambient Sparkles (✦)
        drawCircle(color = NeonCyan, radius = 1.8.dp.toPx(), center = Offset(w * 0.12f, h * 0.10f))
        drawCircle(color = NeonPurple, radius = 2.dp.toPx(), center = Offset(w * 0.90f, h * 0.22f))
        drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = Offset(w * 0.48f, h * 0.92f))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun ReelsControlScreenPreview() {
    ReelsControlContent(
        uiState = ReelsUiState(),
        onEvent = {}
    )
}
