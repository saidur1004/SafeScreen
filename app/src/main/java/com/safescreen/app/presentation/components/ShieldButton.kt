package com.safescreen.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

/**
 * Flagship Hero 3D Shield Button for SafeScreen.
 *
 * Features:
 * - Ambient animated pulse aura when active.
 * - Symmetrical cyberpunk circuit board traces in the background.
 * - Dotted/segmented neon radar track.
 * - True 3D faceted metallic shield (dual-tone Cyan/Green when active, muted slate when disarmed).
 * - Center glowing padlock with keyhole.
 * - Smooth status text and color transitions.
 */
@Composable
fun ShieldButton(
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shieldPulseTransition")

    // Outer breathing pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.15f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldPulseScale"
    )

    // Secondary harmonic aura scale
    val secondaryPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = if (isActive) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldSecondaryPulse"
    )

    // Animated colors based on shield state
    val statusTextColor by animateColorAsState(
        targetValue = if (isActive) NeonGreen else TextMuted,
        animationSpec = tween(300),
        label = "statusTextColor"
    )

    val padlockTint by animateColorAsState(
        targetValue = if (isActive) Color.White else TextMuted,
        animationSpec = tween(300),
        label = "padlockTint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 300.dp, height = 210.dp)
        ) {
            // 1. Background Cyber Circuit Traces Canvas
            Canvas(modifier = Modifier.size(width = 300.dp, height = 210.dp)) {
                val w = size.width
                val h = size.height
                val midY = h * 0.48f

                val traceColor = if (isActive) NeonCyan.copy(alpha = 0.40f) else Color.White.copy(alpha = 0.08f)
                val traceStroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)

                // Left Circuit Traces
                val leftPath1 = Path().apply {
                    moveTo(w * 0.02f, midY - 26.dp.toPx())
                    lineTo(w * 0.16f, midY - 26.dp.toPx())
                    lineTo(w * 0.22f, midY - 8.dp.toPx())
                    lineTo(w * 0.27f, midY - 8.dp.toPx())
                }
                drawPath(leftPath1, color = traceColor, style = traceStroke)
                drawCircle(color = traceColor, radius = 3.dp.toPx(), center = Offset(w * 0.27f, midY - 8.dp.toPx()))

                val leftPath2 = Path().apply {
                    moveTo(w * 0.06f, midY + 22.dp.toPx())
                    lineTo(w * 0.18f, midY + 22.dp.toPx())
                    lineTo(w * 0.24f, midY + 6.dp.toPx())
                    lineTo(w * 0.29f, midY + 6.dp.toPx())
                }
                drawPath(leftPath2, color = traceColor, style = traceStroke)
                drawCircle(color = traceColor, radius = 2.5.dp.toPx(), center = Offset(w * 0.29f, midY + 6.dp.toPx()))

                // Right Circuit Traces (Symmetrical)
                val rightPath1 = Path().apply {
                    moveTo(w * 0.98f, midY - 26.dp.toPx())
                    lineTo(w * 0.84f, midY - 26.dp.toPx())
                    lineTo(w * 0.78f, midY - 8.dp.toPx())
                    lineTo(w * 0.73f, midY - 8.dp.toPx())
                }
                drawPath(rightPath1, color = traceColor, style = traceStroke)
                drawCircle(color = traceColor, radius = 3.dp.toPx(), center = Offset(w * 0.73f, midY - 8.dp.toPx()))

                val rightPath2 = Path().apply {
                    moveTo(w * 0.94f, midY + 22.dp.toPx())
                    lineTo(w * 0.82f, midY + 22.dp.toPx())
                    lineTo(w * 0.76f, midY + 6.dp.toPx())
                    lineTo(w * 0.71f, midY + 6.dp.toPx())
                }
                drawPath(rightPath2, color = traceColor, style = traceStroke)
                drawCircle(color = traceColor, radius = 2.5.dp.toPx(), center = Offset(w * 0.71f, midY + 6.dp.toPx()))
            }

            // 2. Animated Pulsing Aura Ring (Visible when active)
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(secondaryPulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.25f),
                                    NeonGreen.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .size(175.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonCyan.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            // 3. Tech Dotted Radar Outer Track Ring
            Canvas(modifier = Modifier.size(184.dp)) {
                val ringRadius = size.minDimension / 2f - 4.dp.toPx()
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                val ringColor = if (isActive) NeonCyan.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f)
                drawCircle(
                    color = ringColor,
                    radius = ringRadius,
                    center = centerOffset,
                    style = Stroke(
                        width = 1.2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                    )
                )
            }

            // 4. Interactive Shield Core Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
            ) {
                // 3D Beveled Metallic Shield Graphic Canvas
                Canvas(modifier = Modifier.size(width = 112.dp, height = 130.dp)) {
                    val w = size.width
                    val h = size.height

                    // Outer Full Shield Path
                    val shieldPath = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        cubicTo(w * 0.75f, 0f, w * 0.95f, h * 0.08f, w, h * 0.18f)
                        cubicTo(w, h * 0.65f, w * 0.75f, h * 0.88f, w * 0.5f, h)
                        cubicTo(w * 0.25f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.18f)
                        cubicTo(w * 0.05f, h * 0.08f, w * 0.25f, 0f, w * 0.5f, 0f)
                        close()
                    }

                    // Left Facet (Leans Cyan / Luminous Highlight)
                    val leftFacet = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        cubicTo(w * 0.25f, 0f, w * 0.05f, h * 0.08f, 0f, h * 0.18f)
                        cubicTo(0f, h * 0.65f, w * 0.25f, h * 0.88f, w * 0.5f, h)
                        lineTo(w * 0.5f, 0f)
                        close()
                    }

                    // Right Facet (Leans Green / Shadowed Contrast)
                    val rightFacet = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        cubicTo(w * 0.75f, 0f, w * 0.95f, h * 0.08f, w, h * 0.18f)
                        cubicTo(w, h * 0.65f, w * 0.75f, h * 0.88f, w * 0.5f, h)
                        lineTo(w * 0.5f, 0f)
                        close()
                    }

                    if (isActive) {
                        // Active Facet Fills
                        drawPath(
                            path = leftFacet,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E3A5F),
                                    Color(0xFF0C1929)
                                )
                            ),
                            style = Fill
                        )
                        drawPath(
                            path = rightFacet,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF12352B),
                                    Color(0xFF091F18)
                                )
                            ),
                            style = Fill
                        )

                        // Dual-Tone Luminous Beveled Metallic Stroke (Cyan Left -> Bio-Green Right)
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

                        // Specular Bevel Center Crease Line
                        drawLine(
                            color = Color.White.copy(alpha = 0.55f),
                            start = Offset(w * 0.5f, 0f),
                            end = Offset(w * 0.5f, h),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    } else {
                        // Disarmed / Inactive Slate Metallic Look
                        drawPath(
                            path = shieldPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF243042),
                                    Color(0xFF111722)
                                )
                            ),
                            style = Fill
                        )
                        drawPath(
                            path = shieldPath,
                            color = Color(0xFF475569),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f),
                            start = Offset(w * 0.5f, 0f),
                            end = Offset(w * 0.5f, h),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Inner Crest Accent Line
                    val innerCrest = Path().apply {
                        moveTo(w * 0.5f, h * 0.10f)
                        lineTo(w * 0.86f, h * 0.24f)
                        cubicTo(w * 0.86f, h * 0.58f, w * 0.70f, h * 0.78f, w * 0.5f, h * 0.88f)
                        cubicTo(w * 0.30f, h * 0.78f, w * 0.14f, h * 0.58f, w * 0.14f, h * 0.24f)
                        close()
                    }
                    drawPath(
                        path = innerCrest,
                        brush = Brush.verticalGradient(
                            listOf(
                                (if (isActive) NeonCyan else Color.White).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Center Glowing Padlock
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) {
                                Brush.radialGradient(
                                    listOf(
                                        NeonCyan.copy(alpha = 0.40f),
                                        Color.Transparent
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield Lock Core",
                        tint = padlockTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dynamic State Labels
        Text(
            text = if (isActive) "PROTECTION ENGAGED" else "SHIELD DISARMED",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = statusTextColor
            )
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = if (isActive) "Guarding screen & audio in real-time" else "Tap to activate protection",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun ShieldButtonActivePreview() {
    ShieldButton(isActive = true, onToggle = {})
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun ShieldButtonInactivePreview() {
    ShieldButton(isActive = false, onToggle = {})
}
