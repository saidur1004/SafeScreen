package com.safescreen.app.presentation.screens.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

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
 * - High-end Glassmorphic Card containing 3 illuminated pillars (Visual Shield, Smart Audio Guard, Doomscroll Breaker)
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
            height = 180.dp,
            intensity = 0.9f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Bar with "Skip >" Pill Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50))
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

            Spacer(modifier = Modifier.height(8.dp))

            // Glassmorphic Hero 3-Pillar Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pillar 1: Visual Shield (Cyan Glow + Scan Eye)
                    VisualShieldPillar()

                    // Vertical Separator
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pillar 2: Smart Audio Guard (Green Glow + Equalizer Waves & Mute Badge)
                    SmartAudioPillar()

                    // Vertical Separator
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.10f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Pillar 3: Doomscroll Breaker (Purple Glow + Hourglass)
                    DoomscrollPillar()
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    )
                    Text(
                        text = pageData.titleHighlight,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Center,
                            color = NeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

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

            // Carousel Page Indicator (Elongated Active Pill)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                repeat(onboardingPages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (isSelected) 22.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) NeonCyan else Color(0xFF233246)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Trust Badge Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF0F1A2A).copy(alpha = 0.85f))
                        .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeonGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Offline AI • Zero Cloud Upload • No Trackers",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            color = TextPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Full-Width Action Button: "Get Started"
            val isLastPage = pagerState.currentPage == onboardingPages.size - 1

            Button(
                onClick = {
                    if (isLastPage) {
                        onContinue()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
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
                    text = if (isLastPage) "Get Started" else "Continue",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Pillar 1: Visual Shield (Cyan Glow Aura with Scan Brackets & Eye)
 */
@Composable
private fun VisualShieldPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial Neon Cyan Glow
            Canvas(modifier = Modifier.size(56.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.45f),
                            NeonCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )

                // Corner Scan Brackets in Cyan
                val w = size.width
                val h = size.height
                val arm = 8.dp.toPx()
                val inset = 8.dp.toPx()

                // Top-left bracket
                drawLine(NeonCyan, Offset(inset, inset), Offset(inset + arm, inset), strokeWidth = 2.dp.toPx())
                drawLine(NeonCyan, Offset(inset, inset), Offset(inset, inset + arm), strokeWidth = 2.dp.toPx())

                // Top-right bracket
                drawLine(NeonCyan, Offset(w - inset, inset), Offset(w - inset - arm, inset), strokeWidth = 2.dp.toPx())
                drawLine(NeonCyan, Offset(w - inset, inset), Offset(w - inset, inset + arm), strokeWidth = 2.dp.toPx())

                // Bottom-left bracket
                drawLine(NeonCyan, Offset(inset, h - inset), Offset(inset + arm, h - inset), strokeWidth = 2.dp.toPx())
                drawLine(NeonCyan, Offset(inset, h - inset), Offset(inset, h - inset - arm), strokeWidth = 2.dp.toPx())

                // Bottom-right bracket
                drawLine(NeonCyan, Offset(w - inset, h - inset), Offset(w - inset - arm, h - inset), strokeWidth = 2.dp.toPx())
                drawLine(NeonCyan, Offset(w - inset, h - inset), Offset(w - inset, h - inset - arm), strokeWidth = 2.dp.toPx())

                // Center Eye Shape
                drawOval(
                    color = NeonCyan,
                    topLeft = Offset(w * 0.28f, h * 0.38f),
                    size = androidx.compose.ui.geometry.Size(w * 0.44f, h * 0.24f),
                    style = Stroke(width = 1.8.dp.toPx())
                )
                drawCircle(
                    color = NeonCyan,
                    radius = 3.dp.toPx(),
                    center = Offset(w * 0.5f, h * 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Blur NSFW\nContent",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

/**
 * Pillar 2: Smart Audio Guard (Green Glow with Audio Waves & Speaker Mute Badge)
 */
@Composable
private fun SmartAudioPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial Neon Green Glow
            Canvas(modifier = Modifier.size(56.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.40f),
                            NeonCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )

                // Vertical Equalizer Bars in Neon Green
                val w = size.width
                val h = size.height
                val barHeights = listOf(0.25f, 0.45f, 0.65f, 0.45f, 0.25f)
                val barWidth = 2.5.dp.toPx()
                val spacing = 5.dp.toPx()
                val totalWidth = (barHeights.size * barWidth) + ((barHeights.size - 1) * spacing)
                val startX = (w - totalWidth) / 2f

                barHeights.forEachIndexed { index, relHeight ->
                    val x = startX + (index * (barWidth + spacing))
                    val barH = h * relHeight
                    val y = (h - barH) / 2f
                    drawRoundRect(
                        color = NeonGreen,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            // Attached Mute Speaker Badge on bottom-right
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF0F1A2A))
                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Auto-Mute\nUnwanted Sound",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

/**
 * Pillar 3: Doomscroll Breaker (Purple Glow with Hourglass Icon)
 */
@Composable
private fun DoomscrollPillar() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            // Radial Neon Purple Glow
            Canvas(modifier = Modifier.size(56.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.45f),
                            NeonPurple.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Stop Infinite\nReels & Shorts",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.Center,
                color = TextSecondary
            )
        )
    }
}

@Preview(name = "Onboarding Screen - Dark", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun OnboardingScreenPreview() {
    SafeScreenTheme {
        OnboardingScreen()
    }
}
