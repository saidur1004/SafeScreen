package com.safescreen.app.presentation.screens.permission

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * BatteryOptimizationDialog
 *
 * Implements the flagship 3D golden-yellow battery modal bottom sheet from Module 02 Prompt 2,
 * matching the reference mockup in 'Screen Workflow/02_Permissions/':
 * - Large 3D Golden-Yellow Curved Battery Hero with intense neon lightning bolt
 * - Tilted concentric 3D elliptical orbital rings and floating golden sparkle stars
 * - High-contrast typography & description
 * - Full-width vibrant Neon Cyan "Allow Unrestricted" capsule button & "Later" button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryOptimizationDialog(
    onDismiss: () -> Unit,
    onAllowUnrestricted: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF0F1728),
        dragHandle = {
            // Sleek frosted glass top drag handle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }
    ) {
        BatteryOptimizationContent(
            onDismiss = onDismiss,
            onAllowUnrestricted = onAllowUnrestricted,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
        )
    }
}

/**
 * Content body for Battery Optimization Dialog.
 */
@Composable
fun BatteryOptimizationContent(
    onDismiss: () -> Unit,
    onAllowUnrestricted: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ambient breathing pulse for the golden orbital rings and battery
    val infiniteTransition = rememberInfiniteTransition(label = "batteryPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Glowing 3D Golden Battery Hero with Tilted Orbital Rings & Sparkles
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .scale(pulseScale)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val w = size.width
                val h = size.height
                val centerOffset = Offset(w / 2f, h / 2f)

                // 1. Deep Radial Amber Bloom
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFB300).copy(alpha = 0.38f),
                            Color(0xFFF59E0B).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = w * 0.40f
                    )
                )

                // 2. Orbital Ring 1: Tilted -16 degrees
                rotate(degrees = -16f, pivot = centerOffset) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.15f),
                                Color(0xFFFFD54F).copy(alpha = 0.85f),
                                Color(0xFFF59E0B).copy(alpha = 0.20f)
                            )
                        ),
                        topLeft = Offset(centerOffset.x - 110.dp.toPx(), centerOffset.y - 36.dp.toPx()),
                        size = Size(220.dp.toPx(), 72.dp.toPx()),
                        style = Stroke(width = 2.2.dp.toPx())
                    )
                }

                // 3. Orbital Ring 2: Tilted +18 degrees
                rotate(degrees = 18f, pivot = centerOffset) {
                    drawOval(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFF59E0B).copy(alpha = 0.20f),
                                Color(0xFFFFE082).copy(alpha = 0.65f),
                                Color(0xFFF59E0B).copy(alpha = 0.15f)
                            )
                        ),
                        topLeft = Offset(centerOffset.x - 95.dp.toPx(), centerOffset.y - 30.dp.toPx()),
                        size = Size(190.dp.toPx(), 60.dp.toPx()),
                        style = Stroke(width = 1.4.dp.toPx())
                    )
                }

                // 4. Floating Golden Sparkle Stars (✦) & Dust Orbits
                val sparkles = listOf(
                    Offset(centerOffset.x - 90.dp.toPx(), centerOffset.y - 25.dp.toPx()),
                    Offset(centerOffset.x + 85.dp.toPx(), centerOffset.y - 30.dp.toPx()),
                    Offset(centerOffset.x - 80.dp.toPx(), centerOffset.y + 35.dp.toPx()),
                    Offset(centerOffset.x + 92.dp.toPx(), centerOffset.y + 28.dp.toPx()),
                    Offset(centerOffset.x + 40.dp.toPx(), centerOffset.y - 50.dp.toPx()),
                    Offset(centerOffset.x - 50.dp.toPx(), centerOffset.y + 48.dp.toPx())
                )

                sparkles.forEachIndexed { i, pos ->
                    val r = if (i % 2 == 0) 3.dp.toPx() else 2.dp.toPx()
                    drawCircle(
                        color = Color(0xFFFFE082),
                        radius = r,
                        center = pos
                    )
                }

                // 5. 3D Battery Body Graphic
                val battW = 68.dp.toPx()
                val battH = 96.dp.toPx()
                val battLeft = centerOffset.x - battW / 2f
                val battTop = centerOffset.y - battH / 2f + 4.dp.toPx()

                // Battery Terminal Nub (Top)
                val nubW = 24.dp.toPx()
                val nubH = 8.dp.toPx()
                val nubLeft = centerOffset.x - nubW / 2f
                val nubTop = battTop - nubH + 1.dp.toPx()

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFFFFD54F), Color(0xFFFFA000))
                    ),
                    topLeft = Offset(nubLeft, nubTop),
                    size = Size(nubW, nubH),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Battery Body Fill
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF2E2008),
                            Color(0xFF1F1404)
                        )
                    ),
                    topLeft = Offset(battLeft, battTop),
                    size = Size(battW, battH),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                )

                // Battery Ambient Core Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFFB300).copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(centerOffset.x, battTop + battH / 2f),
                        radius = battW * 0.48f
                    )
                )

                // Battery Thick 3D Golden Border
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFE082),
                            Color(0xFFFFB300),
                            Color(0xFFFF8F00)
                        )
                    ),
                    topLeft = Offset(battLeft, battTop),
                    size = Size(battW, battH),
                    cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )

                // 6. Intense Neon Yellow Lightning Bolt inside Battery
                val boltPath = Path().apply {
                    val cx = centerOffset.x
                    val cy = battTop + battH / 2f
                    moveTo(cx + 4.dp.toPx(), cy - 26.dp.toPx())
                    lineTo(cx - 14.dp.toPx(), cy + 2.dp.toPx())
                    lineTo(cx - 1.dp.toPx(), cy + 2.dp.toPx())
                    lineTo(cx - 6.dp.toPx(), cy + 26.dp.toPx())
                    lineTo(cx + 14.dp.toPx(), cy - 2.dp.toPx())
                    lineTo(cx + 1.dp.toPx(), cy - 2.dp.toPx())
                    close()
                }

                // Bolt Glow Backing
                drawPath(
                    path = boltPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFFFEB3B),
                            Color(0xFFFFC107)
                        )
                    ),
                    style = Fill
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Headline Title
        Text(
            text = "Unrestricted Battery Access",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Body Description
        Text(
            text = "Android aggressively kills background machine learning tasks. To ensure your Shield never sleeps when explicit content appears, please exclude SafeScreen from battery optimization.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                fontSize = 13.5.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full-Width Cyan Capsule Action Button: "Allow Unrestricted"
        Button(
            onClick = onAllowUnrestricted,
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Allow Unrestricted",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepBlack
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Secondary "Later" Text Button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Later",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Battery Dialog Preview", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun BatteryOptimizationDialogPreview() {
    SafeScreenTheme {
        BatteryOptimizationContent(
            onDismiss = {},
            onAllowUnrestricted = {}
        )
    }
}
