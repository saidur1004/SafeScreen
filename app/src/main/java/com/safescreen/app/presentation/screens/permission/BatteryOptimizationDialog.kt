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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.GlassBorder
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * BatteryOptimizationDialog
 *
 * Implements the modal bottom sheet from Module 02 Prompt 2, referencing
 * 'Screen Workflow/02_Permissions/SafeScreen Permission Flow Mocku.png' (Right screen).
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
        containerColor = DarkSurfaceElevated,
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
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
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
    // Ambient breathing pulse for orbital rings
    val infiniteTransition = rememberInfiniteTransition(label = "batteryPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Glowing Hero Battery Icon with Amber Orbital Rings
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(150.dp)
                .scale(pulseScale)
        ) {
            // Ambient Canvas Orbital Elliptical Rings
            Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Background Radial Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WarningYellow.copy(alpha = 0.28f),
                            WarningYellow.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = centerOffset,
                        radius = size.width * 0.45f
                    )
                )

                // Orbital Ring 1 (Tilted Oval)
                drawOval(
                    color = WarningYellow.copy(alpha = 0.55f),
                    topLeft = Offset(size.width * 0.12f, size.height * 0.28f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.44f),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Orbital Ring 2 (Counter-Tilted Oval)
                drawOval(
                    color = WarningYellow.copy(alpha = 0.35f),
                    topLeft = Offset(size.width * 0.18f, size.height * 0.35f),
                    size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.30f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Central Glowing Battery Body
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Battery Terminal Nub (Top)
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(WarningYellow)
                )

                // Battery Main Body (Rounded Rect with Lightning Bolt)
                Box(
                    modifier = Modifier
                        .size(width = 54.dp, height = 76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    WarningYellow.copy(alpha = 0.22f),
                                    WarningYellow.copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    WarningYellow,
                                    WarningYellow.copy(alpha = 0.6f)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Unrestricted Power",
                        tint = WarningYellow,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Headline Title
        Text(
            text = "Unrestricted Battery Access",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Body Description Copy
        Text(
            text = "Android aggressively kills background machine learning tasks. To ensure your Shield never sleeps when explicit content appears, please exclude SafeScreen from battery optimization.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Primary Action: "Allow Unrestricted" (Solid Neon Cyan Pill Button)
        Button(
            onClick = onAllowUnrestricted,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Allow Unrestricted",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Secondary Action: "Later" (TextButton)
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Later",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Preview(name = "Battery Optimization Dialog - Dark", showBackground = true, backgroundColor = 0xFF161D2D)
@Composable
private fun BatteryOptimizationDialogPreview() {
    SafeScreenTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceElevated)
                .padding(16.dp)
        ) {
            BatteryOptimizationContent(
                onDismiss = {},
                onAllowUnrestricted = {}
            )
        }
    }
}

