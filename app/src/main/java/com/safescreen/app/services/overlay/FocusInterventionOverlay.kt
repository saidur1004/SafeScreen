package com.safescreen.app.services.overlay

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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * UI State for the Focus Intervention Fullscreen Overlay (Screen 15).
 */
data class FocusInterventionUiState(
    val isVisible: Boolean = true,
    val appName: String = "Short Videos",
    val limitMinutes: Int = 30,
    val isWalkModeActive: Boolean = false,
    val walkCountdownSeconds: Int = 300, // 5 minutes
    val isEmergencyPassActive: Boolean = false,
    val emergencyCountdownSeconds: Int = 120 // 2 minutes
)

/**
 * Screen 15: Fullscreen Focus Intervention Overlay (Doomscroll Breaker).
 *
 * Implements Module 07 Prompt 2:
 * - WindowManager full-screen lockout scrim intercepting touches to halt doomscrolling.
 * - Glowing 3D Shield enclosing an Hourglass icon with breathing purple/magenta aura rings.
 * - Bold headline: "Focus Time Active" with high-contrast dual-tone typography.
 * - Primary Action: "Take a 5-Min Walk" with interactive 5-minute break countdown.
 * - Secondary Action: "Exit to Home Screen" returning user cleanly to Android Launcher.
 * - Micro-Action: "Emergency 2-min pass (Requires PIN)".
 * - Footer: "SafeScreen • Your Focus, Our Priority".
 */
@Composable
fun FocusInterventionContent(
    state: FocusInterventionUiState = FocusInterventionUiState(),
    onTakeWalkClick: () -> Unit = {},
    onExitToHomeClick: () -> Unit = {},
    onEmergencyPassClick: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isWalkMode by remember { mutableStateOf(state.isWalkModeActive) }
    var walkTimer by remember { mutableIntStateOf(state.walkCountdownSeconds) }

    var isEmergencyPass by remember { mutableStateOf(state.isEmergencyPassActive) }
    var emergencyTimer by remember { mutableIntStateOf(state.emergencyCountdownSeconds) }

    // Walk Countdown Timer
    LaunchedEffect(isWalkMode) {
        if (isWalkMode) {
            walkTimer = 300
            while (walkTimer > 0) {
                delay(1000)
                walkTimer--
            }
            isWalkMode = false
        }
    }

    // Emergency Pass Countdown Timer
    LaunchedEffect(isEmergencyPass) {
        if (isEmergencyPass) {
            emergencyTimer = 120
            while (emergencyTimer > 0) {
                delay(1000)
                emergencyTimer--
            }
            isEmergencyPass = false
        }
    }

    // Continuous breathing aura pulse animation for the central shield
    val infiniteTransition = rememberInfiniteTransition(label = "FocusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.30f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    // Fullscreen Lockout Scrim
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isEmergencyPass) Color.Transparent
                else Color(0xFA050811) // 98% opacity deep cyber-dark backdrop
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isEmergencyPass,
                onClick = { /* Intercept all touches to block underlying reels feed */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isEmergencyPass) {
            if (!isWalkMode) {
                // ==========================================
                // 1. STANDARD FOCUS INTERVENTION MODAL CARD
                // ==========================================
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.90f),
                    exit = fadeOut() + scaleOut()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    ) {
                        // Ambient Radial Neon Aura Halo
                        Box(
                            modifier = Modifier
                                .size(320.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF8B5CF6).copy(alpha = pulseAlpha * 0.7f),
                                            Color(0xFFC026D3).copy(alpha = pulseAlpha * 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Central Frosted Glassmorphic Intervention Card
                        GlassmorphicCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 28.dp,
                                    shape = RoundedCornerShape(32.dp),
                                    ambientColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                                    spotColor = Color.Black
                                ),
                            shape = RoundedCornerShape(32.dp),
                            borderWidth = 1.4.dp,
                            borderColor = Color(0xFFC084FC).copy(alpha = 0.55f),
                            backgroundColor = Color(0xF70A0F1D),
                            contentPadding = PaddingValues(28.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // 3D Shield Hero with Hourglass & Glowing Aura
                                FocusHeroShieldEmblem(
                                    pulseScale = pulseScale,
                                    pulseAlpha = pulseAlpha
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Headline: "Focus Time Active"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Focus Time ",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary,
                                            fontSize = 24.sp,
                                            letterSpacing = 0.3.sp
                                        )
                                    )
                                    Text(
                                        text = "Active",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFD946EF),
                                            fontSize = 24.sp,
                                            letterSpacing = 0.3.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Primary message: limit reached
                                Text(
                                    text = "You've reached your daily ${state.limitMinutes}-minute limit for short videos.",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.5.sp,
                                        lineHeight = 20.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Mindful encouragement message
                                Text(
                                    text = "Your time is valuable — give your mind a restful break.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Button 1: "Take a 5-Min Walk" (Glowing Magenta/Purple Capsule)
                                Button(
                                    onClick = {
                                        isWalkMode = true
                                        onTakeWalkClick()
                                    },
                                    shape = RoundedCornerShape(26.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = RoundedCornerShape(26.dp),
                                            ambientColor = Color(0xFFC026D3).copy(alpha = 0.5f),
                                            spotColor = Color(0xFF8B5CF6)
                                        )
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF8B5CF6),
                                                    Color(0xFFC026D3),
                                                    Color(0xFFE879F9)
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
                                            imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Take a 5-Min Walk",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                letterSpacing = 0.4.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Button 2: "Exit to Home Screen" (Outlined Frosted Capsule)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                        .background(Color(0xFF131A2B))
                                        .border(
                                            width = 1.3.dp,
                                            color = Color.White.copy(alpha = 0.35f),
                                            shape = RoundedCornerShape(26.dp)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onExitToHomeClick
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(19.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Exit to Home Screen",
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                letterSpacing = 0.3.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Micro-Action: "Emergency 2-min pass (Requires PIN)"
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                isEmergencyPass = true
                                                onEmergencyPassClick()
                                            }
                                        )
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFC084FC),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Emergency 2-min pass",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = Color(0xFFC084FC),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                letterSpacing = 0.3.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "(Requires PIN)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Footer: "SafeScreen • Your Focus, Our Priority"
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = NeonCyan.copy(alpha = 0.7f),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "SafeScreen • Your Focus, Our Priority",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.4.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // 2. ACTIVE 5-MIN MINDFUL WALK MODE
                // ==========================================
                MindfulWalkCard(
                    remainingSeconds = walkTimer,
                    pulseScale = pulseScale,
                    pulseAlpha = pulseAlpha,
                    onCancelWalk = { isWalkMode = false },
                    onExitToHome = onExitToHomeClick
                )
            }
        } else {
            // ==========================================
            // 3. EMERGENCY 2-MIN PASS ACTIVE (PEEK BANNER)
            // ==========================================
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 52.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xF0101827))
                    .border(1.2.dp, Color(0xFFC084FC), RoundedCornerShape(22.dp))
                    .clickable { isEmergencyPass = false }
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Emergency Pass Active (${formatSeconds(emergencyTimer)}) • Tap to Lock",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFFE879F9),
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
 * Custom 3D Shield Hero with Hourglass and Concentric Luminous Glow Rings.
 * Matches `focus_overlay_right_ref.jpg` with high fidelity.
 */
@Composable
private fun FocusHeroShieldEmblem(
    pulseScale: Float,
    pulseAlpha: Float,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(130.dp)
    ) {
        // 1. Concentric Elliptical Orbital Aura Rings (Canvas)
        Canvas(modifier = Modifier.size(130.dp)) {
            val w = size.width
            val h = size.height

            // Outer subtle glowing ellipse
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = pulseAlpha * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.65f * pulseScale
                )
            )

            // Inner intense neon purple ring
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = pulseAlpha * 0.9f),
                        Color(0xFFC026D3).copy(alpha = pulseAlpha * 0.9f),
                        Color(0xFFE879F9).copy(alpha = pulseAlpha * 0.9f)
                    )
                ),
                style = Stroke(width = 1.8.dp.toPx())
            )
        }

        // 2. 3D Shield with Deep Metallic Gradient & Beveled Neon Border
        Box(
            modifier = Modifier
                .size(86.dp, 96.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0xFFC026D3).copy(alpha = 0.6f),
                    spotColor = Color(0xFF8B5CF6)
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
                    cubicTo(w, h * 0.66f, w * 0.76f, h * 0.88f, w * 0.5f, h)
                    cubicTo(w * 0.24f, h * 0.88f, 0f, h * 0.66f, 0f, h * 0.20f)
                    lineTo(w * 0.12f, 0f)
                    close()
                }

                // Deep Cosmic Metallic Fill
                drawPath(
                    path = shieldPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1336),
                            Color(0xFF0D0819)
                        )
                    ),
                    style = Fill
                )

                // Central Ambient Glow inside shield
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFC026D3).copy(alpha = 0.55f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.5f
                    )
                )

                // Beveled Neon Border: Electric Purple to Magenta
                drawPath(
                    path = shieldPath,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8B5CF6),
                            Color(0xFFD946EF),
                            Color(0xFFC084FC),
                            Color(0xFF8B5CF6)
                        )
                    ),
                    style = Stroke(width = 2.4.dp.toPx())
                )
            }

            // Luminous Hourglass Hero Icon
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = "Hourglass Focus",
                tint = Color(0xFFE879F9),
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

/**
 * 5-Minute Mindful Walk Screen Mode.
 * Encourages the user to step away from digital screens.
 */
@Composable
private fun MindfulWalkCard(
    remainingSeconds: Int,
    pulseScale: Float,
    pulseAlpha: Float,
    onCancelWalk: () -> Unit,
    onExitToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .shadow(
                elevation = 28.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                spotColor = Color.Black
            ),
        shape = RoundedCornerShape(32.dp),
        borderWidth = 1.4.dp,
        borderColor = Color(0xFF8B5CF6).copy(alpha = 0.6f),
        backgroundColor = Color(0xF7090E1C),
        contentPadding = PaddingValues(28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Breathing Lotus Icon
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF8B5CF6).copy(alpha = pulseAlpha * 0.7f),
                                Color(0xFFC026D3).copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SelfImprovement,
                    contentDescription = "Mindful Break",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Take a Mindful Break",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Step away from your phone, hydrate, and stretch your eyes.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Big Countdown Display
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF13192B))
                    .border(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatSeconds(remainingSeconds),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE879F9),
                        fontSize = 38.sp,
                        letterSpacing = 2.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Exit to Home Screen Button
            Button(
                onClick = onExitToHome,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF8B5CF6), Color(0xFFC026D3))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exit to Home Screen",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cancel Break Button
            Text(
                text = "Cancel Break",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCancelWalk
                    )
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Formats seconds into MM:SS format.
 */
private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", m, s)
}

@Preview(showBackground = true)
@Composable
private fun FocusInterventionPreview() {
    FocusInterventionContent(
        state = FocusInterventionUiState(
            appName = "Instagram Reels",
            limitMinutes = 30
        )
    )
}
