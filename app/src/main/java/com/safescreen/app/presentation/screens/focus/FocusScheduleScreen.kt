package com.safescreen.app.presentation.screens.focus

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.components.NeonSwitch
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.SafeScreenTheme
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary

/**
 * Focus Time Schedules Screen (Module 04 / Screen 08).
 *
 * Implements distraction-free schedule blocks matching the reference mockup
 * in 'Screen Workflow/04_Reels_And_Focus/':
 * - Top back navigation & "Focus Time Schedules" title
 * - Preset cards: "Night Purity & Sleep" & "Deep Work / Study" with 3D canvas art
 * - Day Selector Pills: [M] [T] [W] [T] [F] [S] [S]
 * - Dashed outline card: "+ Add Custom Focus Block" & bottom-right FAB
 * - Inspiration Hero Graphic: 3D Shield with clock face & habit building quote
 * - ModalBottomSheet to configure & save new recurring focus blocks
 */
@Composable
fun FocusScheduleScreen(
    onNavigateBack: () -> Unit = {}
) {
    var state by remember { mutableStateOf(FocusScheduleUiState()) }

    FocusScheduleContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is FocusScheduleUiEvent.ToggleSchedule -> {
                    state = state.copy(
                        schedules = state.schedules.map {
                            if (it.id == event.id) it.copy(isEnabled = !it.isEnabled) else it
                        }
                    )
                }
                is FocusScheduleUiEvent.ToggleDayFilter -> {
                    val updated = state.activeDayFilters.toMutableSet()
                    if (updated.contains(event.dayIndex)) {
                        updated.remove(event.dayIndex)
                    } else {
                        updated.add(event.dayIndex)
                    }
                    state = state.copy(activeDayFilters = updated)
                }
                FocusScheduleUiEvent.OpenAddScheduleSheet -> {
                    state = state.copy(showAddScheduleSheet = true)
                }
                FocusScheduleUiEvent.DismissAddScheduleSheet -> {
                    state = state.copy(showAddScheduleSheet = false)
                }
                is FocusScheduleUiEvent.UpdateNewTitle -> {
                    state = state.copy(newScheduleTitle = event.title)
                }
                is FocusScheduleUiEvent.UpdateNewStartTime -> {
                    state = state.copy(newStartTime = event.time)
                }
                is FocusScheduleUiEvent.UpdateNewEndTime -> {
                    state = state.copy(newEndTime = event.time)
                }
                is FocusScheduleUiEvent.ToggleNewScheduleDay -> {
                    val updated = state.newActiveDays.toMutableSet()
                    if (updated.contains(event.dayIndex)) {
                        updated.remove(event.dayIndex)
                    } else {
                        updated.add(event.dayIndex)
                    }
                    state = state.copy(newActiveDays = updated)
                }
                FocusScheduleUiEvent.SaveNewSchedule -> {
                    val title = state.newScheduleTitle.ifBlank { "Custom Focus Block" }
                    val newBlock = FocusScheduleItem(
                        id = "custom_${System.currentTimeMillis()}",
                        title = title,
                        startTime = state.newStartTime,
                        endTime = state.newEndTime,
                        repeatSummary = if (state.newActiveDays.size == 7) "Daily" else "Custom Days",
                        activeDays = state.newActiveDays,
                        isEnabled = true,
                        accentColor = NeonCyan,
                        badgeLabel = "Active",
                        badgeColor = NeonGreen,
                        iconType = FocusIconType.CUSTOM_CLOCK
                    )
                    state = state.copy(
                        schedules = state.schedules + newBlock,
                        showAddScheduleSheet = false,
                        newScheduleTitle = ""
                    )
                }
                FocusScheduleUiEvent.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    )
}

/**
 * Pure stateless Composable adhering to State Hoisting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScheduleContent(
    uiState: FocusScheduleUiState,
    onEvent: (FocusScheduleUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                .padding(horizontal = 20.dp)
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Frosted Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                        .clickable { onEvent(FocusScheduleUiEvent.NavigateBack) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Title
                Text(
                    text = "Focus Time Schedules",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                )

                // Right Info Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Focus Schedule Info",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Subtitle Notice
            Text(
                text = "Completely block short-video platforms\nduring essential life hours.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 14.dp)
            )

            // Scrollable Schedules List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Preset & Custom Schedule Cards
                items(
                    items = uiState.schedules,
                    key = { it.id }
                ) { schedule ->
                    ScheduleBlockCard(
                        item = schedule,
                        onToggle = { onEvent(FocusScheduleUiEvent.ToggleSchedule(schedule.id)) }
                    )
                }

                // 2. Day Selector Pills
                item {
                    DaySelectorRow(
                        selectedDays = uiState.activeDayFilters,
                        onDayToggled = { onEvent(FocusScheduleUiEvent.ToggleDayFilter(it)) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // 3. "+ Add Custom Focus Block" Dashed Button Card
                item {
                    AddCustomFocusBlockTrigger(
                        onClick = { onEvent(FocusScheduleUiEvent.OpenAddScheduleSheet) }
                    )
                }

                // 4. Inspiration Hero Graphic & Habit Quote
                item {
                    InspirationHeroGraphic(
                        modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
                    )
                }
            }
        }

        // Floating Action Button (FAB) at Bottom-Right
        FloatingActionButton(
            onClick = { onEvent(FocusScheduleUiEvent.OpenAddScheduleSheet) },
            shape = CircleShape,
            containerColor = Color.Transparent,
            elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = NeonPurple, spotColor = NeonPurple)
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFFD946EF),
                            Color(0xFFA855F7),
                            Color(0xFF7C3AED)
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Focus Schedule",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        // ModalBottomSheet to Add Custom Schedule
        if (uiState.showAddScheduleSheet) {
            AddFocusBlockBottomSheet(
                uiState = uiState,
                onDismiss = { onEvent(FocusScheduleUiEvent.DismissAddScheduleSheet) },
                onTitleChange = { onEvent(FocusScheduleUiEvent.UpdateNewTitle(it)) },
                onStartTimeChange = { onEvent(FocusScheduleUiEvent.UpdateNewStartTime(it)) },
                onEndTimeChange = { onEvent(FocusScheduleUiEvent.UpdateNewEndTime(it)) },
                onToggleDay = { onEvent(FocusScheduleUiEvent.ToggleNewScheduleDay(it)) },
                onSave = { onEvent(FocusScheduleUiEvent.SaveNewSchedule) }
            )
        }
    }
}

/**
 * High-End Cyber-Dark Glassmorphic Schedule Card.
 */
@Composable
private fun ScheduleBlockCard(
    item: FocusScheduleItem,
    onToggle: () -> Unit
) {
    val borderColor = if (item.isEnabled) item.accentColor.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF131D2D).copy(alpha = 0.88f),
                        Color(0xFF0C1320).copy(alpha = 0.72f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3D Visual Graphic Emblem
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                when (item.iconType) {
                    FocusIconType.MOON_SLEEP -> MoonGraphic3D()
                    FocusIconType.WORK_STUDY -> StudyGraphic3D()
                    FocusIconType.CUSTOM_CLOCK -> ClockGraphic3D(item.accentColor)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Schedule Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Active Status Badge
                    if (item.isEnabled) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(item.badgeColor.copy(alpha = 0.18f))
                                .border(1.dp, item.badgeColor.copy(alpha = 0.55f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = item.badgeColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = item.badgeColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time Range
                Text(
                    text = "${item.startTime} – ${item.endTime}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.5.sp,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Calendar Repeat Summary
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = item.repeatSummary,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Glowing Neon Switch
            NeonSwitch(
                checked = item.isEnabled,
                onCheckedChange = { onToggle() },
                activeColor = item.accentColor
            )
        }
    }
}

/**
 * 3D Glowing Crescent Moon & Clouds Graphic.
 */
@Composable
private fun MoonGraphic3D() {
    Canvas(modifier = Modifier.size(64.dp)) {
        val w = size.width
        val h = size.height

        // Radial Purple/Cyan Aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPurple.copy(alpha = 0.45f),
                    NeonCyan.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(w * 0.45f, h * 0.45f),
                radius = w * 0.5f
            )
        )

        // Crescent Moon Path
        val moonPath = Path().apply {
            val cx = w * 0.45f
            val cy = h * 0.45f
            val r = 18.dp.toPx()
            moveTo(cx, cy - r)
            cubicTo(cx + r * 1.1f, cy - r * 0.8f, cx + r * 1.1f, cy + r * 0.8f, cx, cy + r)
            cubicTo(cx + r * 0.4f, cy + r * 0.6f, cx + r * 0.4f, cy - r * 0.6f, cx, cy - r)
            close()
        }

        drawPath(
            path = moonPath,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFE9D5FF),
                    Color(0xFFC084FC),
                    Color(0xFF00F2FE)
                )
            ),
            style = Fill
        )

        // Moon Glowing Rim
        drawPath(
            path = moonPath,
            color = Color.White.copy(alpha = 0.7f),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Tiny Stars (✦)
        drawCircle(color = Color.White, radius = 1.8.dp.toPx(), center = Offset(w * 0.72f, h * 0.28f))
        drawCircle(color = Color(0xFFC084FC), radius = 1.4.dp.toPx(), center = Offset(w * 0.82f, h * 0.46f))

        // Soft Cloud Pillows at base
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF1E293B).copy(alpha = 0.85f), Color.Transparent),
                center = Offset(w * 0.35f, h * 0.78f),
                radius = 16.dp.toPx()
            ),
            radius = 14.dp.toPx(),
            center = Offset(w * 0.35f, h * 0.78f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF334155).copy(alpha = 0.75f), Color.Transparent),
                center = Offset(w * 0.60f, h * 0.76f),
                radius = 18.dp.toPx()
            ),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.60f, h * 0.76f)
        )
    }
}

/**
 * 3D Glowing Graduation Cap & Stacked Books Graphic.
 */
@Composable
private fun StudyGraphic3D() {
    Canvas(modifier = Modifier.size(64.dp)) {
        val w = size.width
        val h = size.height

        // Radial Purple Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonPurple.copy(alpha = 0.50f),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.48f),
                radius = w * 0.48f
            )
        )

        // Graduation Cap Diamond Mortarboard
        val capDiamond = Path().apply {
            moveTo(w * 0.5f, h * 0.15f)
            lineTo(w * 0.86f, h * 0.30f)
            lineTo(w * 0.5f, h * 0.45f)
            lineTo(w * 0.14f, h * 0.30f)
            close()
        }

        drawPath(
            path = capDiamond,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFC084FC),
                    Color(0xFF7C3AED)
                )
            ),
            style = Fill
        )
        drawPath(
            path = capDiamond,
            color = Color.White.copy(alpha = 0.8f),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Hanging Tassel
        drawLine(
            color = NeonCyan,
            start = Offset(w * 0.5f, h * 0.30f),
            end = Offset(w * 0.88f, h * 0.42f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Stacked Books at Bottom
        // Book 1 (Top Book)
        val b1 = Path().apply {
            moveTo(w * 0.20f, h * 0.60f)
            lineTo(w * 0.80f, h * 0.60f)
            lineTo(w * 0.76f, h * 0.70f)
            lineTo(w * 0.16f, h * 0.70f)
            close()
        }
        drawPath(path = b1, brush = Brush.horizontalGradient(listOf(Color(0xFF6366F1), Color(0xFFA855F7))), style = Fill)
        drawPath(path = b1, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))

        // Book 2 (Bottom Book)
        val b2 = Path().apply {
            moveTo(w * 0.14f, h * 0.72f)
            lineTo(w * 0.86f, h * 0.72f)
            lineTo(w * 0.82f, h * 0.84f)
            lineTo(w * 0.10f, h * 0.84f)
            close()
        }
        drawPath(path = b2, brush = Brush.horizontalGradient(listOf(Color(0xFF3B82F6), Color(0xFF6366F1))), style = Fill)
        drawPath(path = b2, color = Color.White.copy(alpha = 0.5f), style = Stroke(width = 1.dp.toPx()))
    }
}

/**
 * 3D Clock Graphic for Custom Schedule Blocks.
 */
@Composable
private fun ClockGraphic3D(accentColor: Color) {
    Canvas(modifier = Modifier.size(64.dp)) {
        val w = size.width
        val h = size.height

        drawCircle(
            brush = Brush.radialGradient(
                listOf(accentColor.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.48f
            )
        )

        drawCircle(
            brush = Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))),
            radius = 22.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.5f)
        )

        drawCircle(
            color = accentColor,
            radius = 22.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Clock Hands
        drawLine(
            color = Color.White,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.5f, h * 0.32f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accentColor,
            start = Offset(w * 0.5f, h * 0.5f),
            end = Offset(w * 0.65f, h * 0.5f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Day Selector Row: 7 Circular Pills [M] [T] [W] [T] [F] [S] [S].
 */
@Composable
private fun DaySelectorRow(
    selectedDays: Set<Int>,
    onDayToggled: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dayLabels.forEachIndexed { index, label ->
            val isSelected = selectedDays.contains(index)

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) NeonCyan else Color(0xFF131D2D)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
                    .clickable { onDayToggled(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isSelected) DeepBlack else Color.White
                    )
                )
            }
        }
    }
}

/**
 * "+ Add Custom Focus Block" Dashed Button Card.
 */
@Composable
private fun AddCustomFocusBlockTrigger(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131024).copy(alpha = 0.75f))
            .clickable { onClick() }
    ) {
        // Glowing Dashed Purple Border
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.6.dp.toPx()
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
            val cornerR = 16.dp.toPx()

            drawRoundRect(
                color = Color(0xFFD946EF),
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(cornerR, cornerR),
                style = Stroke(width = strokeWidth, pathEffect = dashEffect)
            )
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color(0xFFD946EF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Custom Focus Block",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    color = Color(0xFFE9D5FF)
                )
            )
        }
    }
}

/**
 * Inspiration Hero Graphic: 3D Shield with clock face inside.
 */
@Composable
private fun InspirationHeroGraphic(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "clockPulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "clockPulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        // 3D Clock Shield Emblem
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .scale(pulseScale)
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val w = size.width
                val h = size.height

                // Radial Blue/Cyan Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(NeonCyan.copy(alpha = 0.45f), NeonPurple.copy(alpha = 0.20f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.5f
                    )
                )

                // Shield Path
                val shieldPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.10f)
                    lineTo(w * 0.82f, h * 0.10f)
                    lineTo(w * 0.92f, h * 0.25f)
                    cubicTo(w * 0.92f, h * 0.65f, w * 0.75f, h * 0.86f, w * 0.5f, h * 0.95f)
                    cubicTo(w * 0.25f, h * 0.86f, w * 0.08f, h * 0.65f, w * 0.08f, h * 0.25f)
                    lineTo(w * 0.18f, h * 0.10f)
                    close()
                }

                // Shield Background Fill
                drawPath(
                    path = shieldPath,
                    brush = Brush.verticalGradient(listOf(Color(0xFF13283E), Color(0xFF0A131F))),
                    style = Fill
                )

                // Shield Glowing Cyan/Green Rim
                drawPath(
                    path = shieldPath,
                    brush = Brush.horizontalGradient(listOf(NeonCyan, Color(0xFF00FFA3))),
                    style = Stroke(width = 3.2.dp.toPx())
                )

                // Clock Face Circle inside Shield
                val clockCenter = Offset(w * 0.5f, h * 0.50f)
                val clockR = 20.dp.toPx()

                drawCircle(
                    brush = Brush.verticalGradient(listOf(Color(0xFF0E1A2C), Color(0xFF060B14))),
                    radius = clockR,
                    center = clockCenter
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.8f),
                    radius = clockR,
                    center = clockCenter,
                    style = Stroke(width = 1.6.dp.toPx())
                )

                // Clock Ticks (12, 3, 6, 9)
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = Offset(clockCenter.x, clockCenter.y - clockR + 3.dp.toPx()))
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = Offset(clockCenter.x + clockR - 3.dp.toPx(), clockCenter.y))
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = Offset(clockCenter.x, clockCenter.y + clockR - 3.dp.toPx()))
                drawCircle(color = Color.White, radius = 1.2.dp.toPx(), center = Offset(clockCenter.x - clockR + 3.dp.toPx(), clockCenter.y))

                // Clock Hands (pointing to 10:10)
                // Hour hand
                drawLine(
                    color = Color.White,
                    start = clockCenter,
                    end = Offset(clockCenter.x - 7.dp.toPx(), clockCenter.y - 7.dp.toPx()),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Minute hand
                drawLine(
                    color = NeonCyan,
                    start = clockCenter,
                    end = Offset(clockCenter.x + 8.dp.toPx(), clockCenter.y - 10.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Stay Focused. Live Healthier.",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Let SafeScreen help you build better habits.",
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                color = TextSecondary
            )
        )
    }
}

/**
 * ModalBottomSheet to create & save a new recurring focus block.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFocusBlockBottomSheet(
    uiState: FocusScheduleUiState,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onToggleDay: (Int) -> Unit,
    onSave: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color(0xFF0F172A),
        dragHandle = {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "New Focus Schedule",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Completely locks out short videos during these recurring hours.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Schedule Name Input
            Text(
                text = "Schedule Name",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (uiState.newScheduleTitle.isEmpty()) {
                    Text(
                        text = "e.g., Reading / Family Time",
                        style = TextStyle(color = TextMuted, fontSize = 14.sp)
                    )
                }
                BasicTextField(
                    value = uiState.newScheduleTitle,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Range Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Time",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.newStartTime,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NeonCyan
                            )
                        )
                    }
                }

                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End Time",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, NeonPurple.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = uiState.newEndTime,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NeonPurple
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat Days
            Text(
                text = "Active Days",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            DaySelectorRow(
                selectedDays = uiState.newActiveDays,
                onDayToggled = onToggleDay
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            Button(
                onClick = onSave,
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
                    text = "Save Focus Block",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeepBlack
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Focus Schedule Screen Preview", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun FocusScheduleScreenPreview() {
    SafeScreenTheme {
        FocusScheduleContent(
            uiState = FocusScheduleUiState(),
            onEvent = {}
        )
    }
}
