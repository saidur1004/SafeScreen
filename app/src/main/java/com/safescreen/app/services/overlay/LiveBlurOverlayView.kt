package com.safescreen.app.services.overlay

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
    val latencyMs: Int = 42,
    val isTemporarilyUnblurred: Boolean = false,
    val unblurCountdown: Int = 5
)

/**
 * Live Visual Blur Overlay View.
 *
 * Implements Module 07 Prompt 1:
 * - WindowManager floating overlay over third-party apps
 * - Frosted dark glass veil obscuring explicit screen frames
 * - Central Trust Badge: 3D Shield with eye-slash icon + "Protected by SafeScreen AI"
 * - Pulsing neon cyan/green radial aura breathing animation
 * - Quick "Uncover (5s)" option for false-positive handling and "Dismiss" action
 */
@Composable
fun LiveBlurOverlayContent(
    state: LiveBlurUiState = LiveBlurUiState(),
    onUnblurTemporarily: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isUnblurred by remember { mutableStateOf(state.isTemporarilyUnblurred) }
    var countdown by remember { mutableIntStateOf(state.unblurCountdown) }

    // Countdown timer when unblurred temporarily
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
                else Color(0xDD040812) // Translucent dark frosted scrim
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isUnblurred,
                onClick = { /* Safely absorb background taps so underlying explicit content cannot be clicked */ }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!isUnblurred) {
            // Central Trust Badge with breathing aura
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(initialScale = 0.88f),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
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
                            .fillMaxWidth(0.92f)
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = NeonCyan.copy(alpha = 0.45f),
                                spotColor = Color.Black
                            ),
                        shape = RoundedCornerShape(28.dp),
                        borderWidth = 1.2.dp,
                        borderColor = NeonCyan.copy(alpha = 0.5f),
                        backgroundColor = Color(0xF2091220),
                        contentPadding = PaddingValues(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Top Dismiss 'X' Button
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
                                    contentDescription = "Harmful Content Blocked",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(38.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Trust Headline
                            Text(
                                text = "Protected by SafeScreen AI",
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
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Explicit imagery was actively blocked on-device. Zero data leaves your phone.",
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
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                                        text = "${(state.threatConfidence * 100).toInt()}% NSFW Confidence • ${state.latencyMs}ms Latency",
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
                                // Uncover Temporarily Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.06f))
                                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                isUnblurred = true
                                                onUnblurTemporarily()
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Uncover (5s)",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                color = TextPrimary,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }

                                // Keep Shielded / Dismiss Button
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
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
            threatType = "Harmful Content Blocked"
        )
    )
}
