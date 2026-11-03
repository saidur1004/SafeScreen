package com.safescreen.app.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.safescreen.app.presentation.theme.NeonCyan

/**
 * Cyberpunk Neon Switch with smooth thumb sliding, luminous active glow,
 * and a compact frosted capsule track.
 */
@Composable
fun NeonSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = NeonCyan,
    inactiveTrackColor: Color = Color(0xFF1E2838),
    inactiveThumbColor: Color = Color(0xFF64748B)
) {
    val trackWidth = 46.dp
    val trackHeight = 26.dp
    val thumbSize = 20.dp
    val thumbPadding = 3.dp

    // Animated thumb horizontal offset
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - thumbPadding else thumbPadding,
        animationSpec = tween(durationMillis = 220),
        label = "thumbOffset"
    )

    // Animated track background color
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.35f) else inactiveTrackColor,
        animationSpec = tween(durationMillis = 200),
        label = "trackColor"
    )

    // Animated thumb tint
    val thumbColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveThumbColor,
        animationSpec = tween(durationMillis = 200),
        label = "thumbColor"
    )

    // Animated border color
    val borderColor by animateColorAsState(
        targetValue = if (checked) activeColor.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Sliding thumb with optional glow shadow
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = if (checked) 6.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = activeColor,
                    spotColor = activeColor
                )
                .clip(CircleShape)
                .background(if (checked) Color.White else thumbColor)
        )
    }
}

