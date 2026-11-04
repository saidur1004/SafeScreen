package com.safescreen.app.presentation.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary

data class OnboardingPageData(
    val titlePrefix: String,
    val titleHighlight: String,
    val description: String
)

private val onboardingPages = listOf(
    OnboardingPageData(
        titlePrefix = "Reclaim Your\n",
        titleHighlight = "Focus & Purity",
        description = "On-device AI actively shields your screen and audio from unwanted content. Zero data ever leaves your phone."
    ),
    OnboardingPageData(
        titlePrefix = "100% On-Device\n",
        titleHighlight = "Visual Shield & Audio Guard",
        description = "Neural computer vision instantly blurs explicit images and videos in real time, while audio filter mutes unwanted background music."
    ),
    OnboardingPageData(
        titlePrefix = "Break Addiction with\n",
        titleHighlight = "Reels & Shorts Blocker",
        description = "Curbs doomscrolling across Instagram, YouTube, TikTok and Facebook with strict time limits and smart focus interventions."
    )
)

/**
 * OnboardingScreen
 *
 * Polished to strictly match the reference mockup in 'Screen Workflow/01_Onboarding/':
 * - "Skip >" pill button at top right
 * - High-end Glassmorphic Card containing 3 illuminated 3D pillars (Visual Shield, Smart Audio Guard, Doomscroll Breaker)
 * - Two-tone title with radiant cyan-green gradient
 * - Elongated neon pill page indicators
 * - Frosted Trust Badge with green lock
 * - Glowing full-width "Get Started" capsule button
 */
@Composable
fun OnboardingScreen(
    onContinue: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Bottom Decorative Cyber Silk Waves
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 190.dp,
            intensity = 1.0f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Top Bar with "Skip >" Pill Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(50))
                        .clickable { onContinue() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Skip",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Glassmorphic Hero 3-Pillar Card with Top Ambient Cyan Glow
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 14.dp),
                backgroundColor = Color(0xFF111A2B),
                borderColor = NeonCyan.copy(alpha = 0.35f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pillar 1: Visual Shield (Cyan Glow + Scan Eye)
                    VisualShieldPillar()

                    // Vertical Etched Glass Separator
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pillar 2: Smart Audio Guard (Green Glow + Equalizer Waves & Mute Badge)
                    SmartAudioPillar()

                    // Vertical Etched Glass Separator
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(130.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pillar 3: Doomscroll Breaker (Purple Glow + Hourglass)
                    DoomscrollPillar()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Text Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageData = onboardingPages[page]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    // Headline
                    Text(
                        text = pageData.titlePrefix.trimEnd(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    )
                    Text(
                        text = pageData.titleHighlight,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            lineHeight = 38.sp,
                            textAlign = TextAlign.Center,
                            color = NeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle Body
                    Text(
                        text = pageData.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            }

            // Carousel Page Indicator (Elongated Active Pill with Smooth Motion)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val animatedWidth by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                        label = "onboarding_indicator_width"
                    )
                    val animatedColor by animateColorAsState(
                        targetValue = if (isSelected) NeonCyan else Color(0xFF233246),
                        animationSpec = tween(durationMillis = 300),
                        label = "onboarding_indicator_color"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(animatedWidth)
                            .clip(CircleShape)
                            .background(animatedColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Trust Badge Pill
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF0F172A).copy(alpha = 0.75f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Offline Privacy",
                        tint = NeonGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Offline AI • Zero Cloud Upload • No Trackers",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full-Width Action Button: "Get Started"
            Button(
                onClick = { onContinue() },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DeepBlack
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Pillar 1: Visual Shield (Cyan Glow Aura with Scan Brackets & 3D Glowing Eye)
 */
@Composable
private fun VisualShieldPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(104.dp)
    ) {
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val w = size.width
                val h = size.height

                // Radial Neon Cyan Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.65f),
                            NeonCyan.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.50f
                    )
                )

                // Corner Scan Brackets in Cyan
                val arm = 11.dp.toPx()
                val inset = 4.dp.toPx()
                val strokeW = 2.4.dp.toPx()

                // Top-left bracket
                drawLine(NeonCyan, Offset(inset, inset), Offset(inset + arm, inset), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(NeonCyan, Offset(inset, inset), Offset(inset, inset + arm), strokeWidth = strokeW, cap = StrokeCap.Round)

                // Top-right bracket
                drawLine(NeonCyan, Offset(w - inset, inset), Offset(w - inset - arm, inset), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(NeonCyan, Offset(w - inset, inset), Offset(w - inset, inset + arm), strokeWidth = strokeW, cap = StrokeCap.Round)

                // Bottom-left bracket
                drawLine(NeonCyan, Offset(inset, h - inset), Offset(inset + arm, h - inset), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(NeonCyan, Offset(inset, h - inset), Offset(inset, h - inset - arm), strokeWidth = strokeW, cap = StrokeCap.Round)

                // Bottom-right bracket
                drawLine(NeonCyan, Offset(w - inset, h - inset), Offset(w - inset - arm, h - inset), strokeWidth = strokeW, cap = StrokeCap.Round)
                drawLine(NeonCyan, Offset(w - inset, h - inset), Offset(w - inset, h - inset - arm), strokeWidth = strokeW, cap = StrokeCap.Round)

                // Luminous Eye Almond Contour Path
                val eyeWidth = 42.dp.toPx()
                val eyeHeight = 25.dp.toPx()
                val eyeLeft = (w - eyeWidth) / 2f
                val eyeTop = (h - eyeHeight) / 2f

                val eyePath = Path().apply {
                    moveTo(eyeLeft, h * 0.5f)
                    cubicTo(
                        eyeLeft + eyeWidth * 0.3f, eyeTop - 5.dp.toPx(),
                        eyeLeft + eyeWidth * 0.7f, eyeTop - 5.dp.toPx(),
                        eyeLeft + eyeWidth, h * 0.5f
                    )
                    cubicTo(
                        eyeLeft + eyeWidth * 0.7f, eyeTop + eyeHeight + 5.dp.toPx(),
                        eyeLeft + eyeWidth * 0.3f, eyeTop + eyeHeight + 5.dp.toPx(),
                        eyeLeft, h * 0.5f
                    )
                    close()
                }

                drawPath(
                    path = eyePath,
                    brush = Brush.horizontalGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.85f),
                            Color.White,
                            NeonCyan.copy(alpha = 0.85f)
                        )
                    ),
                    style = Stroke(width = 2.8.dp.toPx())
                )

                // Glowing Iris
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFF00F2FE),
                            Color(0xFF0077B6)
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = 10.dp.toPx()
                    ),
                    radius = 9.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.5f)
                )

                // Pupil
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Visual\nShield",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "Blur NSFW\nContent",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

/**
 * Pillar 2: Smart Audio Guard (Cyan/Green Equalizer Waves & Speaker Mute Badge)
 */
@Composable
private fun SmartAudioPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(104.dp)
    ) {
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val w = size.width
                val h = size.height

                // Radial Emerald/Cyan Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.55f),
                            NeonCyan.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.50f
                    )
                )

                // Vertical Equalizer Bars in Neon Cyan/Green
                val barHeights = listOf(0.25f, 0.42f, 0.65f, 0.88f, 1.0f, 0.88f, 0.65f, 0.42f, 0.25f)
                val barWidth = 3.2.dp.toPx()
                val spacing = 3.8.dp.toPx()
                val totalWidth = (barHeights.size * barWidth) + ((barHeights.size - 1) * spacing)
                val startX = (w - totalWidth) / 2f

                barHeights.forEachIndexed { index, relHeight ->
                    val x = startX + (index * (barWidth + spacing))
                    val barH = h * 0.68f * relHeight
                    val y = (h - barH) / 2f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                NeonCyan,
                                NeonGreen
                            )
                        ),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barH),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            // Lower-Right Mute Badge
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0C1626))
                    .border(1.2.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Auto Mute",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Smart\nAudio Guard",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "Auto-Mute\nUnwanted Sound",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

/**
 * Pillar 3: Doomscroll Breaker (3D Glowing Purple Hourglass with Neon Halo)
 */
@Composable
private fun DoomscrollPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(104.dp)
    ) {
        Box(
            modifier = Modifier.size(76.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(76.dp)) {
                val w = size.width
                val h = size.height

                // Radial Purple Neon Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.65f),
                            Color(0xFF7C3AED).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.50f
                    )
                )

                // Hourglass Glass Contour Path
                val hgWidth = 32.dp.toPx()
                val hgHeight = 44.dp.toPx()
                val left = (w - hgWidth) / 2f
                val top = (h - hgHeight) / 2f
                val right = left + hgWidth
                val bottom = top + hgHeight
                val midY = top + hgHeight / 2f

                // Top Base Rim
                drawLine(
                    color = Color.White,
                    start = Offset(left - 2.dp.toPx(), top),
                    end = Offset(right + 2.dp.toPx(), top),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Bottom Base Rim
                drawLine(
                    color = Color.White,
                    start = Offset(left - 2.dp.toPx(), bottom),
                    end = Offset(right + 2.dp.toPx(), bottom),
                    strokeWidth = 2.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Curved Glass Waist (Top Bulb)
                val glassPath = Path().apply {
                    moveTo(left, top)
                    cubicTo(left, midY - 8.dp.toPx(), w * 0.5f - 3.dp.toPx(), midY - 2.dp.toPx(), w * 0.5f, midY)
                    cubicTo(w * 0.5f + 3.dp.toPx(), midY - 2.dp.toPx(), right, midY - 8.dp.toPx(), right, top)
                    close()
                }
                drawPath(
                    path = glassPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            NeonPurple.copy(alpha = 0.90f),
                            Color(0xFFC084FC)
                        )
                    ),
                    style = Stroke(width = 2.6.dp.toPx())
                )

                // Curved Glass Waist (Bottom Bulb)
                val bottomGlassPath = Path().apply {
                    moveTo(left, bottom)
                    cubicTo(left, midY + 8.dp.toPx(), w * 0.5f - 3.dp.toPx(), midY + 2.dp.toPx(), w * 0.5f, midY)
                    cubicTo(w * 0.5f + 3.dp.toPx(), midY + 2.dp.toPx(), right, midY + 8.dp.toPx(), right, bottom)
                    close()
                }
                drawPath(
                    path = bottomGlassPath,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFC084FC),
                            NeonPurple.copy(alpha = 0.90f)
                        )
                    ),
                    style = Stroke(width = 2.6.dp.toPx())
                )

                // Falling Neon Sand Stream & Droplets
                drawCircle(
                    color = Color.White,
                    radius = 2.4.dp.toPx(),
                    center = Offset(w * 0.5f, midY)
                )
                drawCircle(
                    color = Color(0xFFE9D5FF),
                    radius = 2.0.dp.toPx(),
                    center = Offset(w * 0.5f, midY + 7.dp.toPx())
                )

                // Bottom Sand Pile
                val sandPile = Path().apply {
                    moveTo(left + 5.dp.toPx(), bottom - 2.dp.toPx())
                    lineTo(w * 0.5f, bottom - 12.dp.toPx())
                    lineTo(right - 5.dp.toPx(), bottom - 2.dp.toPx())
                    close()
                }
                drawPath(
                    path = sandPile,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White,
                            NeonPurple
                        )
                    ),
                    style = Fill
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Doomscroll\nBreaker",
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "Stop Infinite\nReels & Shorts",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

@Preview(name = "Onboarding - Dark", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun OnboardingScreenPreview() {
    SafeScreenTheme {
        OnboardingScreen()
    }
}
