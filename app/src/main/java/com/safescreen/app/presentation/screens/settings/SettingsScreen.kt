package com.safescreen.app.presentation.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.components.NeonSwitch
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

/**
 * Main Settings & Security Configuration Screen.
 *
 * Implements Module 06 Prompt 1:
 * - AI Detection Engine sensitivity and FPS performance profiles
 * - Security & biometric lock safeguards
 * - App exclusion whitelist navigation
 * - Data & offline database cache clearance
 * - About, version, and policy links
 */
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToWhitelist: () -> Unit = {},
    state: SettingsUiState = SettingsUiState(),
    onEvent: (SettingsUiEvent) -> Unit = {}
) {
    val context = LocalContext.current
    var localState by remember { mutableStateOf(state) }

    val handleEvent: (SettingsUiEvent) -> Unit = { event ->
        when (event) {
            is SettingsUiEvent.SetSensitivity -> {
                localState = localState.copy(sensitivityLevel = event.level)
                onEvent(event)
            }
            is SettingsUiEvent.SetPerformanceProfile -> {
                localState = localState.copy(performanceProfile = event.profile)
                onEvent(event)
            }
            is SettingsUiEvent.SetBiometricLock -> {
                localState = localState.copy(isBiometricLockEnabled = event.enabled)
                onEvent(event)
            }
            is SettingsUiEvent.SetPreventUninstall -> {
                localState = localState.copy(isPreventUninstallEnabled = event.enabled)
                onEvent(event)
            }
            is SettingsUiEvent.RequestClearCache -> {
                localState = localState.copy(showClearCacheDialog = true)
                onEvent(event)
            }
            is SettingsUiEvent.DismissClearCacheDialog -> {
                localState = localState.copy(showClearCacheDialog = false)
                onEvent(event)
            }
            is SettingsUiEvent.ConfirmClearCache -> {
                localState = localState.copy(showClearCacheDialog = false, isCacheCleared = true)
                Toast.makeText(context, "Offline ML cache cleared successfully", Toast.LENGTH_SHORT).show()
                onEvent(event)
            }
            is SettingsUiEvent.NavigateToWhitelist -> {
                onNavigateToWhitelist()
                onEvent(event)
            }
            is SettingsUiEvent.NavigateBack -> {
                onNavigateBack()
                onEvent(event)
            }
            is SettingsUiEvent.OpenPrivacyPolicy -> {
                Toast.makeText(context, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show()
                onEvent(event)
            }
            is SettingsUiEvent.OpenLicenses -> {
                Toast.makeText(context, "SafeScreen Open-Source Licenses", Toast.LENGTH_SHORT).show()
                onEvent(event)
            }
        }
    }

    SettingsContent(
        state = localState,
        onEvent = handleEvent
    )
}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Dynamic Cyber Waves Ambient Background
        CyberBackgroundWaves(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Brand & Header Section
            item {
                Spacer(modifier = Modifier.height(12.dp))
                SettingsHeader(
                    onBackClick = { onEvent(SettingsUiEvent.NavigateBack) }
                )
            }

            // 2. Section 1: AI Detection Engine Card
            item {
                AiDetectionEngineCard(
                    currentSensitivity = state.sensitivityLevel,
                    onSelectSensitivity = { onEvent(SettingsUiEvent.SetSensitivity(it)) },
                    currentProfile = state.performanceProfile,
                    onSelectProfile = { onEvent(SettingsUiEvent.SetPerformanceProfile(it)) }
                )
            }

            // 3. Section 2: Security & Protection Card
            item {
                SecurityProtectionCard(
                    isBiometricLock = state.isBiometricLockEnabled,
                    onBiometricLockChange = { onEvent(SettingsUiEvent.SetBiometricLock(it)) },
                    isPreventUninstall = state.isPreventUninstallEnabled,
                    onPreventUninstallChange = { onEvent(SettingsUiEvent.SetPreventUninstall(it)) }
                )
            }

            // 4. Section 3: App Exclusions Card
            item {
                AppExclusionsCard(
                    whitelistCount = state.whitelistCount,
                    onClick = { onEvent(SettingsUiEvent.NavigateToWhitelist) }
                )
            }

            // 5. Section 4: Data & Storage Card
            item {
                DataAndStorageCard(
                    onClearCacheClick = { onEvent(SettingsUiEvent.RequestClearCache) }
                )
            }

            // 6. Section 5: About & Licenses Card
            item {
                AboutCard(
                    appVersion = state.appVersion,
                    onPrivacyPolicyClick = { onEvent(SettingsUiEvent.OpenPrivacyPolicy) },
                    onLicensesClick = { onEvent(SettingsUiEvent.OpenLicenses) }
                )
            }

            // Bottom Spacer to scroll past GlobalFloatingBottomBar cleanly
            item {
                Spacer(modifier = Modifier.height(115.dp))
            }
        }

        // Clear Cache Confirmation Dialog
        if (state.showClearCacheDialog) {
            ClearCacheDialog(
                onConfirm = { onEvent(SettingsUiEvent.ConfirmClearCache) },
                onDismiss = { onEvent(SettingsUiEvent.DismissClearCacheDialog) }
            )
        }
    }
}

/**
 * Top Header with brand badge, bold title, subtitle, and 3D glowing shield gear icon.
 */
@Composable
private fun SettingsHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Mini brand indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.15f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SafeScreen Security",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan,
                    letterSpacing = 1.sp
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 28.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Customize your protection and security",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                )
            }

            // 3D Glowing Shield Tune Emblem
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = NeonCyan.copy(alpha = 0.35f),
                        spotColor = NeonCyan
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF132236),
                                Color(0xFF09121F)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                NeonCyan.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings Icon",
                    tint = NeonCyan,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/**
 * Card 1: AI Detection Engine (Sensitivity & Performance FPS profiles).
 */
@Composable
private fun AiDetectionEngineCard(
    currentSensitivity: SensitivityLevel,
    onSelectSensitivity: (SensitivityLevel) -> Unit,
    currentProfile: PerformanceProfile,
    onSelectProfile: (PerformanceProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionIconBadge(icon = Icons.Default.Memory)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "AI Detection Engine",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Control how strictly SafeScreen works",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subheading: Sensitivity Level
            Text(
                text = "SENSITIVITY LEVEL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Segmented Pill for Sensitivity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF080E18))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SensitivityLevel.entries.forEach { level ->
                    val isSelected = level == currentSensitivity
                    SegmentPillButton(
                        label = level.displayName,
                        isSelected = isSelected,
                        onClick = { onSelectSensitivity(level) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subheading: Performance Profile
            Text(
                text = "PERFORMANCE PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Segmented Pill for Performance Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF080E18))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PerformanceProfile.entries.forEach { profile ->
                    val isSelected = profile == currentProfile
                    SegmentPillButton(
                        label = profile.label,
                        isSelected = isSelected,
                        isMultiLine = true,
                        onClick = { onSelectProfile(profile) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Individual segmented pill button.
 * Selected items render solid Neon Cyan with bold black text.
 */
@Composable
private fun SegmentPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMultiLine: Boolean = false
) {
    Box(
        modifier = modifier
            .height(if (isMultiLine) 48.dp else 36.dp)
            .shadow(
                elevation = if (isSelected) 8.dp else 0.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = NeonCyan.copy(alpha = 0.4f),
                spotColor = NeonCyan
            )
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(
                            NeonCyan,
                            Color(0xFF00E5FF)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1726),
                            Color(0xFF090E18)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) NeonCyan.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else Color(0xFF94A3B8),
                fontSize = if (isMultiLine) 11.sp else 12.sp,
                lineHeight = if (isMultiLine) 14.sp else 16.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Card 2: Security & Protection (PIN/Biometric & Prevent Uninstall).
 */
@Composable
private fun SecurityProtectionCard(
    isBiometricLock: Boolean,
    onBiometricLockChange: (Boolean) -> Unit,
    isPreventUninstall: Boolean,
    onPreventUninstallChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionIconBadge(icon = Icons.Default.Security)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Security & Protection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Safeguard settings from unauthorized bypass",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Row 1: PIN & Biometric Lock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "PIN & Biometric Lock",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Require authentication to modify settings or disarm",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
                NeonSwitch(
                    checked = isBiometricLock,
                    onCheckedChange = onBiometricLockChange
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.06f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Prevent Uninstallation Assistance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = "Prevent Uninstallation",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Block unauthorized uninstallation attempts",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
                NeonSwitch(
                    checked = isPreventUninstall,
                    onCheckedChange = onPreventUninstallChange
                )
            }
        }
    }
}

/**
 * Card 3: App Exclusions Card (Navigates to AppWhitelistScreen).
 */
@Composable
private fun AppExclusionsCard(
    whitelistCount: Int,
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
        contentPadding = PaddingValues(16.dp)
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
                SectionIconBadge(icon = Icons.Default.Apps)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Manage App Whitelist",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$whitelistCount apps excluded from shield",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate to Whitelist",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Card 4: Data & Storage Card (Clear Offline Database Cache).
 */
@Composable
private fun DataAndStorageCard(
    onClearCacheClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionIconBadge(icon = Icons.Default.Storage)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Data & Storage",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                    )
                    Text(
                        text = "Local offline database and threat cache",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capsule Button: Clear Cache
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFFEF4444).copy(alpha = 0.12f),
                                Color(0xFFDC2626).copy(alpha = 0.06f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Color(0xFFEF4444).copy(alpha = 0.45f),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClearCacheClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear Cache",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Clear Offline Database Cache",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFF6B6B),
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Card 5: About SafeScreen Card (Version, Privacy Policy & Open-source licenses).
 */
@Composable
private fun AboutCard(
    appVersion: String,
    onPrivacyPolicyClick: () -> Unit,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SectionIconBadge(icon = Icons.Default.Info)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SafeScreen $appVersion",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "100% On-Device Neural Core Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.06f),
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPrivacyPolicyClick
                    )
                )

                Text(
                    text = "•",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp
                )

                Text(
                    text = "Open-Source Licenses",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onLicensesClick
                    )
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Reusable icon badge for card titles.
 */
@Composable
private fun SectionIconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        NeonCyan.copy(alpha = 0.16f),
                        NeonCyan.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                1.dp,
                NeonCyan.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Cyber-Dark Glassmorphic Dialog for Clear Cache confirmation.
 */
@Composable
private fun ClearCacheDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clear Threat Cache?",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        text = {
            Text(
                text = "This will reset all offline threat detection signatures and cached logs. Your protection preferences and whitelist rules will remain unchanged.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF6B6B))
            ) {
                Text(
                    text = "Clear Cache",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color(0xFF0F1726),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.border(
            1.dp,
            Color.White.copy(alpha = 0.15f),
            RoundedCornerShape(20.dp)
        )
    )
}
