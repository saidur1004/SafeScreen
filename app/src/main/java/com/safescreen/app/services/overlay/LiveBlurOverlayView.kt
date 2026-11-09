package com.safescreen.app.services.overlay

import android.graphics.RectF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * UI State for the Live Blur System Overlay.
 */
data class LiveBlurUiState(
    val isVisible: Boolean = true,
    val threatConfidence: Float = 0.98f,
    val threatType: String = "Harmful Content Blocked",
    val latencyMs: Int = 38,
    val isTemporarilyUnblurred: Boolean = false,
    val unblurCountdown: Int = 5,
    val normalizedBoxes: List<RectF> = emptyList()
)

/**
 * Live Visual Blur / 100% Solid Blackout Overlay View.
 *
 * Implements strict Islamic Non-Mahram and Anti-Porn gaze protection:
 * - 100% Opaque Solid Black screen cover (Zero light/photo transmission)
 * - Central Trust Badge: 3D Shield with eye-slash icon + "Protected by SafeScreen AI"
 * - Targeted Bounding Box drawing for detected Non-Mahram faces
 * - Safe "Exit to Home" action to instantly divert gaze from offensive media
 */
@Composable
fun LiveBlurOverlayContent(
    state: LiveBlurUiState = LiveBlurUiState(),
    onUnblurTemporarily: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onLeaveApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isUnblurred by remember { mutableStateOf(state.isTemporarilyUnblurred) }
    var countdown by remember { mutableIntStateOf(state.unblurCountdown) }

    // Countdown timer if temporarily peeking
    LaunchedEffect(isUnblurred) {
        if (isUnblurred) {
            countdown = 5
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            isUnblurred = false
        }
    }

    // Continuous radial aura pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "AuraPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isUnblurred) Color.Transparent
                else Color(0xFF000000) // 100% Solid Opaque Pitch Black - Zero transparency
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isUnblurred,
                onClick = { /* Absorb all touches so underlying explicit content cannot be tapped */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnblurred) {
            // Targeted bounding boxes for detected faces if any
            if (state.normalizedBoxes.isNotEmpty()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (box in state.normalizedBoxes) {
                        val left = box.left * size.width
                        val top = box.top * size.height
                        val right = box.right * size.width
                        val bottom = box.bottom * size.height
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(left, top),
                            size = Size(right - left, bottom - top)
                        )
                    }
                }
            }

            // Central Trust Badge with breathing aura
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.88f),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    // 1. Pulsing Ambient Neon Halo behind the card
                    Box(
                        modifier = Modifier
                            .size(280.dp, 280.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NeonCyan.copy(alpha = pulseAlpha),
                                        NeonGreen.copy(alpha = pulseAlpha * 0.4f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // 2. Central Frosted Glassmorphic Trust Card
                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth(0.94f)
                            .shadow(
                                elevation = 32.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = NeonCyan.copy(alpha = 0.5f),
                                spotColor = Color.Black
                            ),
                        shape = RoundedCornerShape(28.dp),
                        borderWidth = 1.4.dp,
                        borderColor = NeonCyan.copy(alpha = 0.6f),
                        backgroundColor = Color(0xFF091220),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Top Row: Dismiss 'X' Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onDismiss
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss Overlay",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // 3D Shield & Eye-Slash Hero Emblem
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .shadow(
                                        elevation = 14.dp,
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = NeonCyan.copy(alpha = 0.5f),
                                        spotColor = NeonCyan
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF132236),
                                                Color(0xFF09121F)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.2.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                NeonCyan,
                                                NeonGreen.copy(alpha = 0.6f),
                                                Color.White.copy(alpha = 0.15f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = "Content Shielded",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Trust Headline
                            Text(
                                text = "Protected by SafeScreen",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    letterSpacing = 0.5.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Subtitle with threat type
                            Text(
                                text = state.threatType,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "100% Solid blackout active. Screen is fully shielded on-device to protect your gaze.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Threat AI Metric Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0B1424))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${(state.threatConfidence * 100).toInt()}% Confidence • ${state.latencyMs}ms Latency",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            // Action Buttons Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Exit to Home (Safe Exit)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onLeaveApp
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Exit to Home",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                // Keep Shielded Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .shadow(
                                            elevation = 6.dp,
                                            shape = RoundedCornerShape(14.dp),
                                            ambientColor = NeonCyan.copy(alpha = 0.35f),
                                            spotColor = NeonCyan
                                        )
                                        .clip(RoundedCornerShape(14.dp))
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
                                            onClick = onDismiss
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Keep Shielded",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Temporary Peek Floating Banner (Shows while 5s countdown runs)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE6101827))
                    .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { isUnblurred = false }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Temporary Peek Active ($countdown s) • Tap to Re-Shield",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LiveBlurOverlayPreview() {
    LiveBlurOverlayContent(
        state = LiveBlurUiState(
            threatConfidence = 0.98f,
            threatType = "Non-Mahram Face Detected"
        )
    )
}

