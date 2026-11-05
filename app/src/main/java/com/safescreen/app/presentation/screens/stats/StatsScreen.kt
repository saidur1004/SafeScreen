package com.safescreen.app.presentation.screens.stats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * Stateful entry point for Protection Stats Dashboard (Screen 09 / Module 05).
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToDetailedLog: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatsContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                StatsUiEvent.ViewDetailedActivityLog, StatsUiEvent.SeeAllApps -> {
                    onNavigateToDetailedLog()
                }
                StatsUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
                else -> viewModel.onEvent(event)
            }
        }
    )
}

/**
 * Pure stateless Composable for StatsScreen (useful for Previews and Testing).
 */
@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onNavigateBack: () -> Unit = {},
    onNavigateToDetailedLog: () -> Unit = {},
    onEvent: (StatsUiEvent) -> Unit = {}
) {
    StatsContent(
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                StatsUiEvent.ViewDetailedActivityLog, StatsUiEvent.SeeAllApps -> onNavigateToDetailedLog()
                StatsUiEvent.NavigateBack -> onNavigateBack()
                else -> onEvent(event)
            }
        }
    )
}

/**
 * Pure stateless Composable adhering strictly to 'Screen Workflow/05_Stats_And_Analytics/SafeScreen Neon Protection Dashb.png' (Left Screen):
 * - Top Bar with 3D Shield Icon & Capsule Filter Segment [Today] [7 Days (Active)] [30 Days]
 * - Privacy Guarantee Banner ("100% Offline SQLite")
 * - Weekly Activity Overview Card with Dual-Bar Canvas Chart & Active Tooltip
 * - Category Breakdown Card with 3-Color Donut Chart & Percentages
 * - Top Intercepted Apps Card (Chrome, Instagram, Twitter / X) with Proportional Fill Bars
 * - "View Detailed Event Log" Bottom Action Card
 */
@Composable
fun StatsContent(
    uiState: StatsUiState,
    onEvent: (StatsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Ambient Cyber Silk Waves at bottom
        CyberBackgroundWaves(
            modifier = Modifier.align(Alignment.BottomCenter),
            height = 160.dp,
            intensity = 0.8f
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top Bar & Time Range Segmented Pills
            item {
                StatsTopBar(
                    selectedRange = uiState.selectedTimeRange,
                    onRangeSelected = { onEvent(StatsUiEvent.SelectTimeRange(it)) },
                    onNavigateBack = { onEvent(StatsUiEvent.NavigateBack) }
                )
            }

            // 2. Privacy Guarantee Banner
            item {
                PrivacyGuaranteeBanner()
            }

            // 3. Weekly Activity Overview Card (Dual Bar Chart)
            item {
                WeeklyActivityOverviewCard(
                    weeklyActivities = uiState.weeklyActivities,
                    selectedDayIndex = uiState.selectedDayIndex,
                    onDaySelected = { onEvent(StatsUiEvent.SelectDay(it)) }
                )
            }

            // 4. Category Breakdown Card (Donut Chart & Legend)
            item {
                CategoryBreakdownCard(
                    data = uiState.categoryBreakdown
                )
            }

            // 5. Top Intercepted Apps Card
            item {
                TopInterceptedAppsCard(
                    topApps = uiState.topApps,
                    onSeeAllClick = { onEvent(StatsUiEvent.SeeAllApps) }
                )
            }

            // 6. View Detailed Event Log Action Card
            item {
                ViewDetailedEventLogCard(
                    onClick = { onEvent(StatsUiEvent.ViewDetailedActivityLog) }
                )
            }

            // Bottom Spacing for Floating Nav Bar
            item {
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

/**
 * Top bar with 3D Shield icon, Title, and Segmented Time Range Pills.
 */
@Composable
private fun StatsTopBar(
    selectedRange: StatsTimeRange,
    onRangeSelected: (StatsTimeRange) -> Unit,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 3D Shield Emblem & Screen Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onNavigateBack() }
        ) {
            // Small 3D Shield Emblem with Padlock
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.12f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.40f), CircleShape)
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val w = size.width
                    val h = size.height

                    val shieldPath = Path().apply {
                        moveTo(w * 0.5f, 0f)
                        lineTo(w * 0.85f, 0f)
                        lineTo(w, h * 0.20f)
                        cubicTo(w, h * 0.65f, w * 0.78f, h * 0.88f, w * 0.5f, h)
                        cubicTo(w * 0.22f, h * 0.88f, 0f, h * 0.65f, 0f, h * 0.20f)
                        lineTo(w * 0.15f, 0f)
                        close()
                    }

                    drawPath(
                        path = shieldPath,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF00E5FF), Color(0xFF00FFA3))
                        ),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Mini Padlock Body
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(w * 0.35f, h * 0.45f),
                        size = Size(w * 0.30f, h * 0.30f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                    // Shackle
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.37f, h * 0.30f),
                        size = Size(w * 0.26f, h * 0.26f),
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Protection Insights",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White
                )
            )
        }

        // Right: Time Range Segmented Capsule [ Today | 7 Days | 30 Days ]
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatsTimeRange.entries.forEach { range ->
                val isSelected = (range == selectedRange)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) NeonCyan else Color.Transparent
                        )
                        .clickable { onRangeSelected(range) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = range.label,
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            color = if (isSelected) DeepBlack else Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Privacy Guarantee Banner ("100% Offline SQLite. All statistics exist solely on your local device.").
 */
@Composable
private fun PrivacyGuaranteeBanner() {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
        backgroundColor = Color(0xFF071B14).copy(alpha = 0.75f),
        borderColor = NeonGreen.copy(alpha = 0.65f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Glowing Green Padlock in Rounded Frame
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(1.dp, NeonGreen.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy Shield",
                    tint = NeonGreen,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "100% Offline SQLite.",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                )
                Text(
                    text = "All statistics exist solely on your local device.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                )
            }

            // Green Shield Checkmark Badge (Right)
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Verified On-Device",
                    tint = NeonGreen,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

/**
 * Weekly Activity Overview Card with Dual-Bar Canvas Chart & Floating Tooltip.
 */
@Composable
private fun WeeklyActivityOverviewCard(
    weeklyActivities: List<DayActivityItem>,
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        backgroundColor = Color(0xFF0D1625).copy(alpha = 0.85f),
        borderColor = NeonCyan.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Icon + Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Poll,
                    contentDescription = "Bar Chart",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Weekly Activity Overview",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend & Interactive Tooltip Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Blocked Frames (Pink)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Blocked Frames",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Muted Audio (Cyan)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Muted Audio",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                }

                // Tooltip Pill (Active on selected day)
                val selectedItem = weeklyActivities.getOrNull(selectedDayIndex)
                if (selectedItem != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.65f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${selectedItem.day}: ${selectedItem.blockedFrames} Blurred, ${selectedItem.mutedAudio} Muted",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = NeonCyan
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dual-Bar Canvas Chart
            DualBarChartCanvas(
                items = weeklyActivities,
                selectedIndex = selectedDayIndex,
                onDayClick = onDaySelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
        }
    }
}

/**
 * Interactive Dual-Bar Canvas Chart with Y-Axis Grid Lines and Tooltip Pointer.
 */
@Composable
private fun DualBarChartCanvas(
    items: List<DayActivityItem>,
    selectedIndex: Int,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVal = (items.maxOfOrNull { maxOf(it.blockedFrames, it.mutedAudio) } ?: 15).toFloat().coerceAtLeast(15f)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(items) {
                    detectTapGestures { offset ->
                        val chartLeft = 24.dp.toPx()
                        val chartRight = size.width - 6.dp.toPx()
                        val totalWidth = chartRight - chartLeft
                        val slotWidth = totalWidth / items.size

                        val clickedIndex = ((offset.x - chartLeft) / slotWidth).toInt().coerceIn(0, items.size - 1)
                        onDayClick(clickedIndex)
                    }
                }
        ) {
            val w = size.width
            val h = size.height

            val yAxisWidth = 24.dp.toPx()
            val xAxisHeight = 22.dp.toPx()
            val chartTop = 8.dp.toPx()
            val chartBottom = h - xAxisHeight
            val chartHeight = chartBottom - chartTop
            val chartLeft = yAxisWidth
            val chartRight = w - 6.dp.toPx()
            val chartWidth = chartRight - chartLeft

            // 1. Draw Horizontal Grid Lines
            val ySteps = listOf(0.0f, 0.333f, 0.666f, 1.0f)
            ySteps.forEach { ratio ->
                val yPos = chartTop + chartHeight * ratio
                drawLine(
                    color = Color.White.copy(alpha = 0.07f),
                    start = Offset(chartLeft, yPos),
                    end = Offset(chartRight, yPos),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // 2. Draw Dual Bars for each day
            val dayCount = items.size
            val slotWidth = chartWidth / dayCount
            val barWidth = 10.dp.toPx()
            val barGap = 3.dp.toPx()

            items.forEachIndexed { i, item ->
                val slotCenterX = chartLeft + slotWidth * i + slotWidth / 2f
                val isSelected = (i == selectedIndex)

                // Bar 1: Blocked Frames (Pink)
                val pinkRatio = (item.blockedFrames / maxVal).coerceIn(0f, 1f)
                val pinkBarHeight = chartHeight * pinkRatio
                val pinkLeft = slotCenterX - barWidth - barGap / 2f
                val pinkTop = chartBottom - pinkBarHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isSelected) Color(0xFFFF6584) else Color(0xFFFF2A85),
                            Color(0xFFC2185B)
                        )
                    ),
                    topLeft = Offset(pinkLeft, pinkTop),
                    size = Size(barWidth, pinkBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Bar 2: Muted Audio (Cyan)
                val cyanRatio = (item.mutedAudio / maxVal).coerceIn(0f, 1f)
                val cyanBarHeight = chartHeight * cyanRatio
                val cyanLeft = slotCenterX + barGap / 2f
                val cyanTop = chartBottom - cyanBarHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isSelected) Color(0xFF00F0FF) else Color(0xFF00B0FF),
                            Color(0xFF0077B6)
                        )
                    ),
                    topLeft = Offset(cyanLeft, cyanTop),
                    size = Size(barWidth, cyanBarHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Glowing Indicator on Selected Day (Thursday)
                if (isSelected) {
                    val indicatorCenter = Offset(slotCenterX - barWidth / 2f - barGap / 2f, pinkTop)

                    // Angled cyan pointer line connecting to top tooltip
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.85f),
                                NeonCyan.copy(alpha = 0.15f)
                            )
                        ),
                        start = indicatorCenter,
                        end = Offset(indicatorCenter.x + 38.dp.toPx(), 0f),
                        strokeWidth = 1.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = indicatorCenter
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.50f),
                        radius = 8.5.dp.toPx(),
                        center = indicatorCenter
                    )
                }
            }
        }

        // Y-Axis labels (45, 30, 15, 0)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 2.dp, bottom = 22.dp)
                .height(138.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("45", "30", "15", "0").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                )
            }
        }

        // X-Axis Day labels (Mon, Tue, Wed, Thu, Fri, Sun)
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = (index == selectedIndex)
                Text(
                    text = item.day,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.clickable { onDayClick(index) }
                )
            }
        }
    }
}

/**
 * Category Breakdown Card with 3-Segment Donut Chart & Legend.
 */
@Composable
private fun CategoryBreakdownCard(
    data: CategoryBreakdownData
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        backgroundColor = Color(0xFF0D1625).copy(alpha = 0.85f),
        borderColor = NeonCyan.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "Category Breakdown",
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Donut Chart on Left + Legend Table on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: 3-Segment Donut Chart with Center Text
                Box(
                    modifier = Modifier.size(114.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(114.dp)) {
                        val strokeW = 14.dp.toPx()
                        val diameter = size.minDimension - strokeW
                        val arcSize = Size(diameter, diameter)
                        val arcTopLeft = Offset(strokeW / 2f, strokeW / 2f)

                        val vRatio = (data.visualPercent / 100f).coerceIn(0f, 1f)
                        val aRatio = (data.audioPercent / 100f).coerceIn(0f, 1f)
                        val rRatio = (data.reelsPercent / 100f).coerceIn(0f, 1f)
                        val hasData = (data.visualPercent + data.audioPercent + data.reelsPercent) > 0

                        if (!hasData) {
                            // Empty state inactive ring
                            drawArc(
                                color = Color.White.copy(alpha = 0.08f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round)
                            )
                        } else {
                            var currentAngle = -90f

                            // Segment 1: Explicit Visuals Blurred
                            if (vRatio > 0.02f) {
                                val sweep = (360f * vRatio - 4f).coerceAtLeast(6f)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFFFF2A85), Color(0xFFFF6584))
                                    ),
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                )
                                currentAngle += 360f * vRatio
                            }

                            // Segment 2: Background Music Muted
                            if (aRatio > 0.02f) {
                                val sweep = (360f * aRatio - 4f).coerceAtLeast(6f)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFF00E5FF), Color(0xFF00B0FF))
                                    ),
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                )
                                currentAngle += 360f * aRatio
                            }

                            // Segment 3: Reels Sessions Intercepted
                            if (rRatio > 0.02f) {
                                val sweep = (360f * rRatio - 4f).coerceAtLeast(6f)
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        listOf(Color(0xFFA855F7), Color(0xFFD946EF))
                                    ),
                                    startAngle = currentAngle,
                                    sweepAngle = sweep,
                                    useCenter = false,
                                    topLeft = arcTopLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                                )
                            }
                        }
                    }

                    // Center Highlight
                    val hasData = (data.visualPercent + data.audioPercent + data.reelsPercent) > 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (hasData) "${data.visualPercent}%" else "0%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (hasData) "Visual" else "All",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                        Text(
                            text = if (hasData) "Blurred" else "Clear",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right: Legend Items List
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CategoryLegendRow(
                        color = NeonPink,
                        title = data.visualLabel,
                        percent = "${data.visualPercent}%"
                    )
                    CategoryLegendRow(
                        color = NeonCyan,
                        title = data.audioLabel,
                        percent = "${data.audioPercent}%"
                    )
                    CategoryLegendRow(
                        color = NeonPurple,
                        title = data.reelsLabel,
                        percent = "${data.reelsPercent}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendRow(
    color: Color,
    title: String,
    percent: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp,
                    color = Color.White
                )
            )
        }
        Text(
            text = percent,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        )
    }
}

/**
 * Top Intercepted Apps Card (Chrome, Instagram, Twitter / X).
 */
@Composable
private fun TopInterceptedAppsCard(
    topApps: List<InterceptedAppItem>,
    onSeeAllClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        backgroundColor = Color(0xFF0D1625).copy(alpha = 0.85f),
        borderColor = NeonCyan.copy(alpha = 0.25f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Trophy + Title & "See All →"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Top Apps",
                        tint = WarningYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Top Intercepted Apps",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onSeeAllClick() }
                ) {
                    Text(
                        text = "See All",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "See All",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Rows List
            if (topApps.isEmpty()) {
                Text(
                    text = "No intercepted apps yet. Your device is clean & protected!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    topApps.forEach { app ->
                        AppInterceptRow(item = app)
                    }
                }
            }
        }
    }
}

/**
 * Individual row for an intercepted app with custom badge, name, proportional bar, and count.
 */
@Composable
private fun AppInterceptRow(
    item: InterceptedAppItem
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Custom Logo Badge (Canvas)
        AppIconBadge(type = item.iconType)

        Spacer(modifier = Modifier.width(10.dp))

        // App Name (Width-constrained)
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color.White
            ),
            modifier = Modifier.width(76.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Proportional Gradient Capsule Bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1E293B).copy(alpha = 0.60f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.percentage)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                item.barColor.copy(alpha = 0.70f),
                                item.barColor
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Event count label (e.g. "54 events")
        Text(
            text = "${item.eventCount} events",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        )
    }
}

/**
 * High-fidelity Custom App Badges for Chrome, Instagram, and Twitter / X.
 */
@Composable
private fun AppIconBadge(type: AppIconType) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            AppIconType.CHROME -> {
                Canvas(modifier = Modifier.size(30.dp)) {
                    val r = size.minDimension / 2f
                    val c = Offset(size.width / 2f, size.height / 2f)

                    // Chrome Red
                    drawArc(
                        color = Color(0xFFEA4335),
                        startAngle = 180f,
                        sweepAngle = 120f,
                        useCenter = true
                    )
                    // Chrome Green
                    drawArc(
                        color = Color(0xFF34A853),
                        startAngle = 300f,
                        sweepAngle = 120f,
                        useCenter = true
                    )
                    // Chrome Yellow
                    drawArc(
                        color = Color(0xFFFBBC05),
                        startAngle = 60f,
                        sweepAngle = 120f,
                        useCenter = true
                    )
                    // White Ring & Cyan Blue Center
                    drawCircle(color = Color.White, radius = r * 0.45f, center = c)
                    drawCircle(color = Color(0xFF4285F4), radius = r * 0.35f, center = c)
                }
            }
            AppIconType.INSTAGRAM -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF833AB4),
                                    Color(0xFFFD1D1D),
                                    Color(0xFFFCB045)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val w = size.width
                        val h = size.height
                        // Camera rounded rect
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        // Lens
                        drawCircle(
                            color = Color.White,
                            radius = 3.5.dp.toPx(),
                            center = Offset(w / 2f, h / 2f),
                            style = Stroke(width = 1.4.dp.toPx())
                        )
                        // Flash dot
                        drawCircle(
                            color = Color.White,
                            radius = 1.dp.toPx(),
                            center = Offset(w * 0.78f, h * 0.22f)
                        )
                    }
                }
            }
            AppIconType.TWITTER_X -> {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "𝕏",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Bottom action card navigating to Detailed Activity Log Screen (Module 05 Prompt 2).
 */
@Composable
private fun ViewDetailedEventLogCard(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF131D2D).copy(alpha = 0.95f),
                        Color(0xFF0C1320).copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                1.dp,
                NeonCyan.copy(alpha = 0.40f),
                RoundedCornerShape(22.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Glowing Document Log Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = "Event Log",
                        tint = NeonCyan,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "View Detailed Event Log",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Detailed Log",
                tint = NeonCyan,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Preview(name = "Protection Stats Screen", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun StatsScreenPreview() {
    SafeScreenTheme {
        StatsContent(
            uiState = StatsUiState(),
            onEvent = {}
        )
    }
}
