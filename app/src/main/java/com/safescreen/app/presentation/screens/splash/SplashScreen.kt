package com.safescreen.app.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import com.safescreen.app.presentation.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * SplashScreen
 *
 * Implements the hyper-luminous 3D Cyber-Dark splash screen matching the reference
 * mockup in 'Screen Workflow/01_Onboarding/':
 * - Angular heraldic 3D beveled shield with neon cyan/bio-green rims
 * - Glossy 3D padlock with keyhole
 * - Ambient radial bloom and concentric orbital ring
 * - Crisp brand typography: "Safe" (White) + "Screen" (Cyan gradient)
 * - Luminous indeterminate progress capsule bar
 */
@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit = {}
) {
    // Initial entrance scale and alpha
    val entranceScale = remember { Animatable(0.90f) }
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
        initialValue = 0.28f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
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
        // Maintain display for 2000ms
        delay(2000)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Deep Central Volumetric Radial Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.38f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.35f),
                        NeonGreen.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.80f
                )
            )
        }

        // Bottom Decorative Cyber Silk Waves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 190.dp,
            intensity = 1.2f
        )

        // Central Brand Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .scale(entranceScale.value)
        ) {
            Spacer(modifier = Modifier.weight(1.1f))

            // 3D-Stylized Beveled Shield Hero Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp)
            ) {
                // Pulsing outer neon cyan aura
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .scale(auraScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.50f),
                                    NeonGreen.copy(alpha = 0.20f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Concentric glowing circular orbital ring
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.80f),
                                NeonGreen.copy(alpha = 0.45f),
                                NeonCyan.copy(alpha = 0.15f),
                                NeonCyan.copy(alpha = 0.80f)
                            )
                        ),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                }

                // Hyper-Luminous Angular Heraldic 3D Shield & 3D Padlock Canvas
                Canvas(modifier = Modifier.size(width = 168.dp, height = 196.dp)) {
                    val w = size.width
                    val h = size.height

                    // Angular Heraldic 3D Shield Outer Path (sharp shoulders)
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
                            listOf(
                                Color(0xFF13283E),
                                Color(0xFF091422)
                            )
                        ),
                        style = Fill
                    )

                    // 2. Central Ambient Turquoise Glow inside shield
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.55f),
                                Color.Transparent
                            ),
                            center = Offset(w * 0.5f, h * 0.52f),
                            radius = w * 0.46f
                        )
                    )

                    // 3. Thick 3D Beveled Outer Luminous Rim (Cyan left to Bio-Green right)
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
                        style = Stroke(width = 7.dp.toPx())
                    )

                    // 4. Inset Accent Bevel Line
                    val innerCrest = Path().apply {
                        val inset = 8.dp.toPx()
                        moveTo(w * 0.5f, inset)
                        lineTo(w * 0.80f, inset)
                        lineTo(w - inset, h * 0.18f)
                        cubicTo(w - inset, h * 0.63f, w * 0.76f, h * 0.86f, w * 0.5f, h - inset)
                        cubicTo(w * 0.24f, h * 0.86f, inset, h * 0.63f, inset, h * 0.18f)
                        lineTo(w * 0.20f, inset)
                        close()
                    }
                    drawPath(
                        path = innerCrest,
                        brush = Brush.verticalGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.55f),
                                NeonGreen.copy(alpha = 0.20f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 1.6.dp.toPx())
                    )

                    // 5. 3D Glossy Padlock Graphic in Center
                    val lockCenterX = w * 0.5f
                    val lockCenterY = h * 0.53f

                    // Shackle (Rounded Arch)
                    val shackleRadius = 16.dp.toPx()
                    val shackleTop = lockCenterY - 30.dp.toPx()
                    val shacklePath = Path().apply {
                        moveTo(lockCenterX - shackleRadius, lockCenterY - 6.dp.toPx())
                        lineTo(lockCenterX - shackleRadius, shackleTop + shackleRadius)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(
                                left = lockCenterX - shackleRadius,
                                top = shackleTop,
                                right = lockCenterX + shackleRadius,
                                bottom = shackleTop + 2 * shackleRadius
                            ),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 180f,
                            forceMoveTo = false
                        )
                        lineTo(lockCenterX + shackleRadius, lockCenterY - 6.dp.toPx())
                    }
                    drawPath(
                        path = shacklePath,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White,
                                Color(0xFF00E5FF)
                            )
                        ),
                        style = Stroke(width = 5.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Lock Main Body (Glossy Rounded Rect)
                    val bodyWidth = 44.dp.toPx()
                    val bodyHeight = 36.dp.toPx()
                    val bodyLeft = lockCenterX - bodyWidth / 2f
                    val bodyTop = lockCenterY - 6.dp.toPx()

                    // Lock Body Fill
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color(0xFF00F2FE),
                                Color(0xFF00B4D8),
                                Color(0xFF0077B6)
                            ),
                            startY = bodyTop,
                            endY = bodyTop + bodyHeight
                        ),
                        topLeft = Offset(bodyLeft, bodyTop),
                        size = Size(bodyWidth, bodyHeight),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
                    )

                    // Lock Body Gloss Highlight Rim
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.9f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset(bodyLeft, bodyTop),
                        size = Size(bodyWidth, bodyHeight),
                        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx()),
                        style = Stroke(width = 1.4.dp.toPx())
                    )

                    // Dark Keyhole in Lock Center
                    drawCircle(
                        color = Color(0xFF0A192F),
                        radius = 4.dp.toPx(),
                        center = Offset(lockCenterX, bodyTop + 14.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color(0xFF0A192F),
                        topLeft = Offset(lockCenterX - 2.dp.toPx(), bodyTop + 14.dp.toPx()),
                        size = Size(4.dp.toPx(), 9.dp.toPx()),
                        cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Two-Tone Brand Title: "Safe" (White) + "Screen" (Cyan Gradient)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Safe",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "Screen",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
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

            Spacer(modifier = Modifier.weight(1.0f))

            // Hyper-Luminous Capsule Progress Bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(9.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF0E1726))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressSweep)
                        .height(9.dp)
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

            Spacer(modifier = Modifier.weight(0.9f))
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
