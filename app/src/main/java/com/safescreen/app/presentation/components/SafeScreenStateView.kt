package com.safescreen.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * Supported State Types for Module 08 (Screen 16: Error, Warning & Empty States).
 */
enum class SafeScreenStateType {
    ALL_CLEAN,          // Screen 1: All Clean & Clear (Bio-green shield, checkmark, 0 stats)
    SHIELD_PAUSED,      // Screen 2: Shield Paused / Permission Lost (Amber warning shield, Restore Permissions)
    AI_CORE_ERROR,      // Screen 3: AI Core Uninitialized / Missing Model (Neural Brain, Reload Core)
    EMPTY_LOG           // Screen 4: Detailed Activity Log Empty State (Folder with checkmark)
}

/**
 * Unified Decoupled State Composable for SafeScreen.
 *
 * Implements Module 08 (Screen 16):
 * - Reusable across HomeScreen, DetailedActivityLogScreen, and Fallback Handlers.
 * - 100% Cyber-Dark Glassmorphic design matching design references.
 */
@Composable
fun SafeScreenStateView(
    stateType: SafeScreenStateType,
    onActionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack),
        contentAlignment = Alignment.Center
    ) {
        when (stateType) {
            SafeScreenStateType.ALL_CLEAN -> AllCleanClearStateContent(modifier = Modifier.fillMaxSize())
            SafeScreenStateType.SHIELD_PAUSED -> ShieldPausedStateContent(onRestorePermissionsClick = onActionClick, modifier = Modifier.fillMaxSize())
            SafeScreenStateType.AI_CORE_ERROR -> AiCoreErrorStateContent(onReloadCoreClick = onActionClick, modifier = Modifier.fillMaxSize())
            SafeScreenStateType.EMPTY_LOG -> EmptyActivityLogContent(onRefreshClick = onActionClick, modifier = Modifier.fillMaxSize())
        }
    }
}

// ===================================================================================
// 1. ALL CLEAN & CLEAR STATE (Screen 1 - Reference: SafeScreen Neon Status Dashboard.png Left)
// ===================================================================================

@Composable
fun AllCleanClearStateContent(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CleanPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Bottom Ambient Cyber Waves
        StateWaveCanvas(
            primaryColor = NeonCyan,
            secondaryColor = NeonGreen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(180.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Hero: 3D Metallic Bio-Green Shield with Checkmark
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Outer Radial Ambient Aura & Ring Light
                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = size.width
                    val h = size.height

                    // Glowing radial halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonGreen.copy(alpha = pulseAlpha * 0.45f),
                                NeonCyan.copy(alpha = pulseAlpha * 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.55f * pulseScale
                        )
                    )

                    // Concentric base ellipse
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                NeonGreen.copy(alpha = pulseAlpha * 0.8f),
                                NeonCyan.copy(alpha = pulseAlpha * 0.8f)
                            )
                        ),
                        topLeft = Offset(w * 0.12f, h * 0.76f),
                        size = Size(w * 0.76f, h * 0.16f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 3D Beveled Emerald Shield
                Box(
                    modifier = Modifier
                        .size(118.dp, 134.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = NeonGreen.copy(alpha = 0.5f),
                            spotColor = Color.Black
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val shieldPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            lineTo(w * 0.88f, 0f)
                            lineTo(w, h * 0.20f)
                            cubicTo(w, h * 0.65f, w * 0.76f, h * 0.88f, w * 0.5f, h)
                            cubicTo(w * 0.24f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.20f)
                            lineTo(w * 0.12f, 0f)
                            close()
                        }

                        // Metallic dark cyan/green interior fill
                        drawPath(
                            path = shieldPath,
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF0F322D), Color(0xFF061B18), Color(0xFF030D0C))
                            ),
                            style = Fill
                        )

                        // Ambient central glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(NeonGreen.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.5f),
                                radius = w * 0.45f
                            )
                        )

                        // Thick 3D Beveled Luminous Rim
                        drawPath(
                            path = shieldPath,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF00FFA3),
                                    Color(0xFF00E5FF),
                                    Color(0xFF00FFA3),
                                    Color(0xFF00E676)
                                )
                            ),
                            style = Stroke(width = 3.2.dp.toPx())
                        )
                    }

                    // Thick White Checkmark inside shield
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "All Clean",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Title Row: Circled Checkmark + "All Clean & Clear"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonGreen.copy(alpha = 0.2f))
                        .border(1.5.dp, NeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "All Clean ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 24.sp
                    )
                )
                Text(
                    text = "& Clear",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen,
                        fontSize = 24.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "No explicit visuals or music interruptions detected yet today. Enjoy your safe digital journey!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphic Metric Card: [0 Blurred] [0 Muted] [0 Blocked]
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = NeonGreen.copy(alpha = 0.2f),
                        spotColor = Color.Black
                    ),
                shape = RoundedCornerShape(22.dp),
                borderWidth = 1.dp,
                borderColor = NeonGreen.copy(alpha = 0.35f),
                backgroundColor = Color(0xFF0A151E),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CleanMetricItem(icon = Icons.Default.VisibilityOff, value = "0", label = "Blurred", tint = NeonCyan)
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.15f)))
                    CleanMetricItem(icon = Icons.AutoMirrored.Filled.VolumeOff, value = "0", label = "Muted", tint = NeonCyan)
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.15f)))
                    CleanMetricItem(icon = Icons.Default.BarChart, value = "0", label = "Blocked", tint = NeonGreen)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Frosted Pill: "✔ Stay Focused, Stay Safe 💚"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF09161B))
                    .border(1.2.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Stay Focused, Stay Safe 💚",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CleanMetricItem(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 18.sp
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextMuted,
                fontSize = 11.sp
            )
        )
    }
}

// ===================================================================================
// 2. SHIELD PAUSED / PERMISSION REVOKED STATE (Screen 2 - Center Reference)
// ===================================================================================

@Composable
fun ShieldPausedStateContent(
    onRestorePermissionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WarningPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.40f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Bottom Ambient Amber Cyber Waves
        StateWaveCanvas(
            primaryColor = WarningYellow,
            secondaryColor = Color(0xFFF59E0B),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(180.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Hero: 3D Amber Shield with Warning Triangle & Exclamation Badge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Amber Orbital Glow
                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = size.width
                    val h = size.height

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                WarningYellow.copy(alpha = pulseAlpha * 0.45f),
                                Color(0xFFF59E0B).copy(alpha = pulseAlpha * 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.55f * pulseScale
                        )
                    )

                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = pulseAlpha * 0.8f),
                                WarningYellow.copy(alpha = pulseAlpha * 0.8f)
                            )
                        ),
                        topLeft = Offset(w * 0.12f, h * 0.76f),
                        size = Size(w * 0.76f, h * 0.16f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 3D Beveled Gold/Amber Shield
                Box(
                    modifier = Modifier
                        .size(118.dp, 134.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(32.dp),
                            ambientColor = WarningYellow.copy(alpha = 0.5f),
                            spotColor = Color.Black
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        val shieldPath = Path().apply {
                            moveTo(w * 0.5f, 0f)
                            lineTo(w * 0.88f, 0f)
                            lineTo(w, h * 0.20f)
                            cubicTo(w, h * 0.65f, w * 0.76f, h * 0.88f, w * 0.5f, h)
                            cubicTo(w * 0.24f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.20f)
                            lineTo(w * 0.12f, 0f)
                            close()
                        }

                        // Metallic dark amber interior fill
                        drawPath(
                            path = shieldPath,
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF332009), Color(0xFF1F1203), Color(0xFF0F0800))
                            ),
                            style = Fill
                        )

                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(WarningYellow.copy(alpha = 0.5f), Color.Transparent),
                                center = Offset(w * 0.5f, h * 0.5f),
                                radius = w * 0.45f
                            )
                        )

                        // Thick 3D Beveled Amber Luminous Rim
                        drawPath(
                            path = shieldPath,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFFFFD166),
                                    Color(0xFFF59E0B),
                                    Color(0xFFFBBF24),
                                    Color(0xFFFFD166)
                                )
                            ),
                            style = Stroke(width = 3.2.dp.toPx())
                        )
                    }

                    // Golden Warning Triangle Icon
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Shield Paused",
                        tint = WarningYellow,
                        modifier = Modifier.size(54.dp)
                    )
                }

                // Top-Right Exclamation Badge (!)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 24.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .border(2.dp, Color(0xFFFFD166), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            fontSize = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Title Row: Circled Pause Icon + "Shield Paused: Screen Access Needed"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(WarningYellow.copy(alpha = 0.2f))
                        .border(1.5.dp, WarningYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = WarningYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Shield Paused: ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 21.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Screen Access Needed",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = WarningYellow,
                    fontSize = 21.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Android revoked MediaProjection or Overlay access. SafeScreen cannot protect you until permissions are restored.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Full-Width Luminous Amber Capsule Button: "Restore Permissions"
            Button(
                onClick = onRestorePermissionsClick,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(52.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = Color(0xFFF59E0B).copy(alpha = 0.6f),
                        spotColor = WarningYellow
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF59E0B),
                                Color(0xFFFBBF24),
                                Color(0xFFFFD166)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Restore Permissions",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Helper Info Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(0.90f),
                shape = RoundedCornerShape(16.dp),
                borderWidth = 1.dp,
                borderColor = Color.White.copy(alpha = 0.12f),
                backgroundColor = Color(0xFF111722),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Go to Settings and grant the required permissions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

// ===================================================================================
// 3. AI CORE UNINITIALIZED / MODEL ERROR STATE (Screen 3 - Right Reference)
// ===================================================================================

@Composable
fun AiCoreErrorStateContent(
    onReloadCoreClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ErrorPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Bottom Ambient Purple/Violet Waves
        StateWaveCanvas(
            primaryColor = NeonPurple,
            secondaryColor = Color(0xFF3B82F6),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(180.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Hero: Glowing 3D Neural Brain with Electric Tracks & Crossed Tools
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Electric Blue & Violet Radial Aura
                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = size.width
                    val h = size.height

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF3B82F6).copy(alpha = pulseAlpha * 0.5f),
                                NeonPurple.copy(alpha = pulseAlpha * 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.55f * pulseScale
                        )
                    )
                }

                // Neural Brain Icon Graphic (Canvas Circuit Brain)
                NeuralBrainHeroGraphic(
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    modifier = Modifier.size(130.dp)
                )

                // Lower-Right Crossed Tools Magenta Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 20.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD946EF))
                        .border(2.dp, Color(0xFFF472B6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Fix Core",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Title Row: CPU Microchip + "AI Core Uninitialized"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonCyan.copy(alpha = 0.2f))
                        .border(1.5.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "AI Core ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 23.sp
                    )
                )
                Text(
                    text = "Uninitialized",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        fontSize = 23.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "The local TFLite neural model was not found in local assets. Tap to self-repair or reload model.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Outlined Glowing Cyan Action Button: "Reload Core"
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0F1B2C))
                    .border(
                        width = 1.4.dp,
                        brush = Brush.horizontalGradient(
                            listOf(NeonCyan, Color(0xFF00E5FF), NeonCyan)
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(26.dp),
                        ambientColor = NeonCyan.copy(alpha = 0.45f),
                        spotColor = NeonCyan
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onReloadCoreClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reload Core",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Helper Info Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(0.90f),
                shape = RoundedCornerShape(16.dp),
                borderWidth = 1.dp,
                borderColor = Color.White.copy(alpha = 0.12f),
                backgroundColor = Color(0xFF111722),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "This will reload the AI model from local storage.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * 3D Neural Brain Graphic with electronic circuit nodes.
 */
@Composable
private fun NeuralBrainHeroGraphic(
    pulseScale: Float,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Outer Neural Hemisphere Arc (Left)
        drawArc(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF38BDF8), Color(0xFF3B82F6), Color(0xFF8B5CF6))
            ),
            startAngle = 135f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(w * 0.15f, h * 0.15f),
            size = Size(w * 0.38f, h * 0.65f)
        )

        // Outer Neural Hemisphere Arc (Right)
        drawArc(
            brush = Brush.verticalGradient(
                listOf(Color(0xFF38BDF8), Color(0xFF3B82F6), Color(0xFF8B5CF6))
            ),
            startAngle = 225f,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            topLeft = Offset(w * 0.47f, h * 0.15f),
            size = Size(w * 0.38f, h * 0.65f)
        )

        // Center Stem
        drawLine(
            brush = Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6))),
            start = Offset(w * 0.5f, h * 0.28f),
            end = Offset(w * 0.5f, h * 0.82f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Circuit Branch Left 1
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(w * 0.5f, h * 0.42f),
            end = Offset(w * 0.32f, h * 0.42f),
            strokeWidth = 2.5.dp.toPx()
        )
        drawCircle(color = Color(0xFF38BDF8), radius = 4.dp.toPx(), center = Offset(w * 0.32f, h * 0.42f))

        // Circuit Branch Right 1
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(w * 0.5f, h * 0.55f),
            end = Offset(w * 0.68f, h * 0.55f),
            strokeWidth = 2.5.dp.toPx()
        )
        drawCircle(color = Color(0xFF38BDF8), radius = 4.dp.toPx(), center = Offset(w * 0.68f, h * 0.55f))

        // Circuit Branch Left 2
        drawLine(
            color = Color(0xFF8B5CF6),
            start = Offset(w * 0.5f, h * 0.68f),
            end = Offset(w * 0.34f, h * 0.76f),
            strokeWidth = 2.5.dp.toPx()
        )
        drawCircle(color = Color(0xFF8B5CF6), radius = 4.dp.toPx(), center = Offset(w * 0.34f, h * 0.76f))
    }
}

// ===================================================================================
// 4. DETAILED ACTIVITY LOG EMPTY STATE (Reference: SafeScreen Activity Log_ All Cle.png)
// ===================================================================================

@Composable
fun EmptyActivityLogContent(
    onRefreshClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LogPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Bottom Ambient Waves
        StateWaveCanvas(
            primaryColor = NeonCyan,
            secondaryColor = NeonGreen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(180.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Hero: Glowing 3D Folder with Shield & Checkmark
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Glassmorphic Glowing Dome behind Folder
                Canvas(modifier = Modifier.size(190.dp)) {
                    val w = size.width
                    val h = size.height

                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                NeonGreen.copy(alpha = pulseAlpha * 0.40f),
                                NeonCyan.copy(alpha = pulseAlpha * 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.5f),
                            radius = w * 0.55f * pulseScale
                        )
                    )

                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(NeonGreen.copy(alpha = 0.8f), NeonCyan.copy(alpha = 0.8f))
                        ),
                        topLeft = Offset(w * 0.10f, h * 0.72f),
                        size = Size(w * 0.80f, h * 0.18f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // 3D Folder Graphic
                Box(
                    modifier = Modifier
                        .size(116.dp, 88.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = NeonGreen.copy(alpha = 0.5f),
                            spotColor = Color.Black
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F362F), Color(0xFF071E1A))
                            )
                        )
                        .border(
                            1.5.dp,
                            Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                            RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(44.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Green Circled Checkmark Floating Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 32.dp, end = 24.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Headline: "All Clear! No Threats Found."
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "All Clear! ",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = "No Threats Found.",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen,
                        fontSize = 22.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your digital space is currently pure and focused. We'll log any blocked content here.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Capsule Action Button: "Refresh"
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.70f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF0F1B2C))
                    .border(
                        width = 1.3.dp,
                        brush = Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF00E5FF))),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = NeonCyan.copy(alpha = 0.4f),
                        spotColor = NeonCyan
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRefreshClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Frosted Pill: "✔ Stay Focused, Stay Safe 💚"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF09161B))
                    .border(1.2.dp, NeonGreen.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Stay Focused, Stay Safe 💚",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Reusable Canvas Waves with configurable dual theme colors.
 */
@Composable
private fun StateWaveCanvas(
    primaryColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wave Layer 1
        val wavePath1 = Path().apply {
            moveTo(0f, h * 0.75f)
            cubicTo(w * 0.25f, h * 0.45f, w * 0.55f, h * 0.95f, w, h * 0.55f)
        }
        drawPath(
            path = wavePath1,
            brush = Brush.linearGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.05f),
                    primaryColor.copy(alpha = 0.55f),
                    secondaryColor.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                start = Offset(0f, h * 0.6f),
                end = Offset(w, h * 0.6f)
            ),
            style = Stroke(width = 2.2.dp.toPx())
        )

        // Wave Layer 2
        val wavePath2 = Path().apply {
            moveTo(0f, h * 0.85f)
            cubicTo(w * 0.35f, h * 0.65f, w * 0.70f, h * 0.90f, w, h * 0.68f)
        }
        drawPath(
            path = wavePath2,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    secondaryColor.copy(alpha = 0.45f),
                    primaryColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                start = Offset(w * 0.15f, h * 0.75f),
                end = Offset(w * 0.95f, h * 0.75f)
            ),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AllCleanClearStatePreview() {
    SafeScreenStateView(stateType = SafeScreenStateType.ALL_CLEAN)
}

@Preview(showBackground = true)
@Composable
private fun ShieldPausedStatePreview() {
    SafeScreenStateView(stateType = SafeScreenStateType.SHIELD_PAUSED)
}

@Preview(showBackground = true)
@Composable
private fun AiCoreErrorStatePreview() {
    SafeScreenStateView(stateType = SafeScreenStateType.AI_CORE_ERROR)
}

@Preview(showBackground = true)
@Composable
private fun EmptyActivityLogPreview() {
    SafeScreenStateView(stateType = SafeScreenStateType.EMPTY_LOG)
}
