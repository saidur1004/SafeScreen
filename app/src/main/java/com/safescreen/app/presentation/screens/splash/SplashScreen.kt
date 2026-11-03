package com.safescreen.app.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * SplashScreen
 *
 * Implements the flagship cyber-dark splash screen from Module 01, perfectly
 * matching the visual design in 'Screen Workflow/01_Onboarding/'.
 */
@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit = {}
) {
    // Initial entrance scale animation
    val entranceScale = remember { Animatable(0.85f) }
    val entranceAlpha = remember { Animatable(0.0f) }

    // Continuous ambient breathing pulse for the shield aura
    val infiniteTransition = rememberInfiniteTransition(label = "shieldAuraTransition")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )

    // Indeterminate progress bar sweep animation
    val progressSweep by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressSweep"
    )

    LaunchedEffect(Unit) {
        entranceScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        entranceAlpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 400)
        )
        // Maintain display for 1500ms for smooth initial impression
        delay(1500)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Ambient Central Radial Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.40f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.20f),
                        NeonGreen.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.65f
                )
            )
        }

        // Bottom Decorative Cyber Silk Curves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 180.dp,
            intensity = 1.1f
        )

        // Central Brand Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .scale(entranceScale.value)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // 3D-Stylized Shield Hero with Concentric Glowing Aura Rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Outer pulsing neon aura
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(auraScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.40f),
                                    NeonGreen.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle concentric thin circular ring
                Box(
                    modifier = Modifier
                        .size(174.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.65f),
                                    NeonGreen.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // High-End Custom Beveled 3D Shield Canvas
                Canvas(modifier = Modifier.size(width = 120.dp, height = 138.dp)) {
                    val w = size.width
                    val h = size.height

                    val shieldPath = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        // Top edge to right shoulder
                        cubicTo(w * 0.75f, 0f, w * 0.95f, h * 0.08f, w, h * 0.18f)
                        // Right edge down to bottom vertex
                        cubicTo(w, h * 0.65f, w * 0.75f, h * 0.88f, w * 0.5f, h)
                        // Bottom vertex back up to left shoulder
                        cubicTo(w * 0.25f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.18f)
                        // Left shoulder to top center
                        cubicTo(w * 0.05f, h * 0.08f, w * 0.25f, 0f, w * 0.5f, 0f)
                        close()
                    }

                    // Shield Deep Glass Background
                    drawPath(
                        path = shieldPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF16253B),
                                Color(0xFF0C1420)
                            )
                        ),
                        style = Fill
                    )

                    // Dual-Tone Luminous Beveled Stroke (Cyan on left, Bio-Green on right)
                    drawPath(
                        path = shieldPath,
                        brush = Brush.horizontalGradient(
                            listOf(
                                NeonCyan,
                                NeonCyan,
                                NeonGreen
                            )
                        ),
                        style = Stroke(width = 3.5.dp.toPx())
                    )

                    // Inner Crest Accent
                    val innerCrest = Path().apply {
                        moveTo(w * 0.5f, h * 0.08f)
                        lineTo(w * 0.88f, h * 0.22f)
                        cubicTo(w * 0.88f, h * 0.60f, w * 0.70f, h * 0.80f, w * 0.5f, h * 0.90f)
                        cubicTo(w * 0.30f, h * 0.80f, w * 0.12f, h * 0.60f, w * 0.12f, h * 0.22f)
                        close()
                    }
                    drawPath(
                        path = innerCrest,
                        brush = Brush.verticalGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Glowing Padlock in Shield Center
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "SafeScreen Shield Core",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Two-Tone Brand Title: "Safe" (White) + "Screen" (Neon Cyan Gradient)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Safe",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "Screen",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp,
                        letterSpacing = (-0.5).sp,
                        color = NeonCyan
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Tagline
            Text(
                text = "Your Digital Purity & Focus Guard",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    letterSpacing = 0.2.sp
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Glowing Indeterminate Capsule Progress Bar
            Box(
                modifier = Modifier
                    .width(230.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF141C2B))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressSweep)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.6f),
                                    NeonCyan,
                                    NeonGreen
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Microcopy Label
            Text(
                text = "Initializing Neural Core (100% On-Device)...",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.85f),
                    letterSpacing = 0.3.sp
                )
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Preview(name = "Splash Screen - Dark", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun SplashScreenPreview() {
    SafeScreenTheme {
        SplashScreen()
    }
}
