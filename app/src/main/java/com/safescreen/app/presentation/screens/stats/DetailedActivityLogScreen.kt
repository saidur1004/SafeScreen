package com.safescreen.app.presentation.screens.stats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.safescreen.app.presentation.components.SafeScreenStateType
import com.safescreen.app.presentation.components.SafeScreenStateView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * DetailedActivityLogScreen
 *
 * Implements the chronological Activity Timeline in Jetpack Compose showing
 * every local protection event with category badges, timestamps, confidence scores,
 * collapsible date groups, filter controls, and clear log confirmation modal.
 */
@Composable
fun DetailedActivityLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: DetailedActivityLogViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailedActivityLogContent(
        state = uiState,
        onEvent = { event ->
            if (event is ActivityLogUiEvent.NavigateBack) {
                onNavigateBack()
            } else {
                viewModel.onEvent(event)
            }
        },
        onRefreshLogs = {},
        modifier = modifier
    )
}

/**
 * Pure stateless content composable for DetailedActivityLogScreen (useful for Previews and Testing).
 */
@Composable
fun DetailedActivityLogScreen(
    onNavigateBack: () -> Unit,
    state: ActivityLogUiState,
    onEvent: (ActivityLogUiEvent) -> Unit = {},
    onRefreshLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DetailedActivityLogContent(
        state = state,
        onEvent = { event ->
            if (event is ActivityLogUiEvent.NavigateBack) {
                onNavigateBack()
            } else {
                onEvent(event)
            }
        },
        onRefreshLogs = onRefreshLogs,
        modifier = modifier
    )
}

@Composable
fun DetailedActivityLogContent(
    state: ActivityLogUiState,
    onEvent: (ActivityLogUiEvent) -> Unit,
    onRefreshLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Ambient Cybernetic Glow Waves at the bottom
        AmbientCyberWaves(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            ActivityLogTopBar(
                isFilterActive = state.selectedFilter != LogCategory.ALL,
                isFilterMenuVisible = state.isFilterMenuVisible,
                onBackClicked = { onEvent(ActivityLogUiEvent.NavigateBack) },
                onFilterClicked = { onEvent(ActivityLogUiEvent.ToggleFilterMenu) },
                onClearClicked = { onEvent(ActivityLogUiEvent.RequestClearLogs) }
            )

            // Animated Filter Category Bar
            AnimatedVisibility(
                visible = state.isFilterMenuVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FilterCategorySelectorBar(
                    selectedFilter = state.selectedFilter,
                    onSelectFilter = { onEvent(ActivityLogUiEvent.SelectFilter(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Main Timeline Feed / Empty State
            if (state.isEmpty || state.visibleGroups.isEmpty()) {
                EmptyActivityLogState(
                    onRefreshLogs = onRefreshLogs,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(
                        items = state.visibleGroups,
                        key = { _, group -> group.dateId }
                    ) { index, group ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        DateGroupSection(
                            group = group,
                            onToggleExpansion = {
                                onEvent(ActivityLogUiEvent.ToggleGroupExpansion(group.dateId))
                            },
                            onItemClicked = { item ->
                                onEvent(ActivityLogUiEvent.LogItemClicked(item))
                            }
                        )
                    }
                }
            }
        }

        // Clear Log Confirmation Modal with pure glassmorphic translucency
        // (Positioned right over "Reels Limit" so "Yesterday" header remains visible underneath)
        if (state.isClearConfirmDialogVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.20f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onEvent(ActivityLogUiEvent.DismissClearDialog) }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                ClearLogConfirmationCard(
                    onConfirm = { onEvent(ActivityLogUiEvent.ConfirmClearLogs) },
                    onDismiss = { onEvent(ActivityLogUiEvent.DismissClearDialog) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 120.dp)
                )
            }
        }
    }
}

/**
 * Top Bar:
 * Circular frosted back button, "Activity Timeline", Filter toggle icon, and Red "Clear Log" capsule button.
 */
@Composable
private fun ActivityLogTopBar(
    isFilterActive: Boolean,
    isFilterMenuVisible: Boolean,
    onBackClicked: () -> Unit,
    onFilterClicked: () -> Unit,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button with frosted circular surface
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF131D2D).copy(alpha = 0.70f))
                .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClicked
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title
        Text(
            text = "Activity Timeline",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Filter Icon Button
        IconButton(
            onClick = onFilterClicked,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = "Filter",
                tint = if (isFilterActive || isFilterMenuVisible) NeonCyan else Color(0xFF94A3B8),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Red "Clear Log" Capsule Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(NeonPink.copy(alpha = 0.16f))
                .border(1.2.dp, NeonPink.copy(alpha = 0.95f), RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClearClicked
                )
                .padding(horizontal = 13.dp, vertical = 7.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Log",
                    tint = NeonPink,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Clear Log",
                    color = NeonPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Filter Category Selector Bar:
 * Allows user to filter timeline by [All], [Visual], [Audio], [Reels].
 */
@Composable
private fun FilterCategorySelectorBar(
    selectedFilter: LogCategory,
    onSelectFilter: (LogCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F1726).copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        LogCategory.entries.forEach { category ->
            val isSelected = category == selectedFilter
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) NeonCyan.copy(alpha = 0.22f)
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) NeonCyan else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectFilter(category) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (category) {
                        LogCategory.ALL -> "All"
                        LogCategory.VISUAL -> "Visual"
                        LogCategory.AUDIO -> "Audio"
                        LogCategory.REELS -> "Reels"
                    },
                    color = if (isSelected) NeonCyan else TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Date Group Section:
 * Frosted header card with calendar icon, date string, collapsible chevron,
 * and a list of expandable activity cards.
 */
@Composable
private fun DateGroupSection(
    group: DateGroupedLogs,
    onToggleExpansion: () -> Unit,
    onItemClicked: (ActivityLogItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
    ) {
        // Group Header Pill Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF101927).copy(alpha = 0.90f),
                            Color(0xFF0C1320).copy(alpha = 0.92f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.75f),
                            NeonCyan.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.18f),
                            NeonCyan.copy(alpha = 0.30f)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleExpansion
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calendar Icon Container
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(NeonCyan.copy(alpha = 0.22f))
                        .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Date Label
                Text(
                    text = group.dateLabel,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                // Collapsible Chevron
                Icon(
                    imageVector = if (group.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (group.isExpanded) "Collapse" else "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expanded Activity Items
        if (group.isExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                group.items.forEach { item ->
                    ActivityLogItemCard(
                        item = item,
                        onClick = { onItemClicked(item) }
                    )
                }
            }
        }
    }
}

/**
 * Activity Log Item Card:
 * Cyber-Dark glass card with category-colored left accent aura & border,
 * title, source app + timestamp subtitle, confidence pill badge, and right chevron.
 */
@Composable
private fun ActivityLogItemCard(
    item: ActivityLogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (item.category) {
        LogCategory.VISUAL -> NeonPink
        LogCategory.AUDIO -> NeonCyan
        LogCategory.REELS -> NeonPurple
        LogCategory.ALL -> NeonCyan
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.20f),
                        Color(0xFF0F1726).copy(alpha = 0.90f),
                        Color(0xFF0A0F1B).copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.85f),
                        accentColor.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Free-Floating Radial Glow Aura (No rigid box border)
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vibrant radial aura behind icon
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accentColor.copy(alpha = 0.50f), Color.Transparent)
                        ),
                        radius = size.minDimension / 1.3f
                    )
                }

                when (item.category) {
                    LogCategory.VISUAL -> {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Visual NSFW Blurred",
                            tint = NeonPink,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    LogCategory.AUDIO -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "Audio Guard Muted",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    LogCategory.REELS -> {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Reels Limit Reached",
                            tint = NeonPurple,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    LogCategory.ALL -> {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security Guard",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.8.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = item.sourceApp,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.badgeText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accentColor.copy(alpha = 0.12f))
                            .border(1.dp, accentColor.copy(alpha = 0.70f), RoundedCornerShape(50))
                            .padding(horizontal = 9.dp, vertical = 2.5.dp)
                    ) {
                        Text(
                            text = item.badgeText,
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Inspect Event",
                tint = Color(0xFF94A3B8).copy(alpha = 0.75f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Clear Activity Log Modal Confirmation Card:
 * Outlined glowing amber-yellow warning triangle ⚠️, warning explanation,
 * Solid Cyan "Clear Log" capsule button, and Outlined "Cancel" button.
 */
@Composable
private fun ClearLogConfirmationCard(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1726).copy(alpha = 0.96f),
                        Color(0xFF070B13).copy(alpha = 0.97f)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WarningYellow.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.18f),
                        NeonCyan.copy(alpha = 0.45f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { /* prevent click through */ }
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Outlined Glowing Amber-Yellow Warning Triangle
            GlowingWarningTriangle(
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = "Clear Activity Log?",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "This will permanently remove all\nprotection activity history from this device.\nAre you sure?",
                color = Color(0xFF94A3B8),
                fontSize = 12.8.sp,
                lineHeight = 17.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Button 1: Glowing Solid Cyan "Clear Log" Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(NeonCyan, Color(0xFF00C8FF))
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onConfirm
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = DeepBlack,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Clear Log",
                        color = DeepBlack,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button 2: Outlined "Cancel" Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(50))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Custom Canvas-rendered Outlined Warning Triangle matching reference:
 * Hollow amber triangle with rounded corners, exclamation mark, and glowing radial aura.
 */
@Composable
private fun GlowingWarningTriangle(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Radial ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    WarningYellow.copy(alpha = 0.45f),
                    WarningYellow.copy(alpha = 0.15f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / 1.35f,
            center = Offset(w / 2f, h / 2f)
        )

        // Rounded hollow triangle path
        val trianglePath = Path().apply {
            moveTo(w * 0.50f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.80f)
            lineTo(w * 0.14f, h * 0.80f)
            close()
        }

        // Draw outer glow stroke
        drawPath(
            path = trianglePath,
            color = WarningYellow.copy(alpha = 0.40f),
            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw main crisp stroke
        drawPath(
            path = trianglePath,
            color = WarningYellow,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Exclamation vertical line
        drawLine(
            color = WarningYellow,
            start = Offset(w * 0.50f, h * 0.40f),
            end = Offset(w * 0.50f, h * 0.60f),
            strokeWidth = 3.2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Exclamation dot
        drawCircle(
            color = WarningYellow,
            radius = 2.2.dp.toPx(),
            center = Offset(w * 0.50f, h * 0.70f)
        )
    }
}

/**
 * Screen 16: Empty Activity Log State ("All Clear! No Threats Found.")
 * Uses the official Module 08 SafeScreenStateView with 3D cyber-folder, checkmark,
 * subtitle, and Refresh capsule button.
 */
@Composable
private fun EmptyActivityLogState(
    onRefreshLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    SafeScreenStateView(
        stateType = SafeScreenStateType.EMPTY_LOG,
        onActionClick = onRefreshLogs,
        modifier = modifier
    )
}

/**
 * Ambient Cybernetic Waves at the bottom/top of the screen.
 */
@Composable
private fun AmbientCyberWaves(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Bottom cyber wave 1 (Cyan glow fill)
        val wavePath1 = Path().apply {
            moveTo(0f, height * 0.90f)
            cubicTo(
                width * 0.25f, height * 0.85f,
                width * 0.65f, height * 0.95f,
                width, height * 0.87f
            )
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = wavePath1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.18f),
                    NeonGreen.copy(alpha = 0.08f),
                    Color.Transparent
                )
            )
        )

        // Bottom cyber wave 2 (Luminous Cyan stroke line)
        val wavePath2 = Path().apply {
            moveTo(0f, height * 0.92f)
            cubicTo(
                width * 0.30f, height * 0.96f,
                width * 0.70f, height * 0.89f,
                width, height * 0.94f
            )
        }
        drawPath(
            path = wavePath2,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.60f),
                    NeonGreen.copy(alpha = 0.45f),
                    Color.Transparent
                )
            ),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }
}
