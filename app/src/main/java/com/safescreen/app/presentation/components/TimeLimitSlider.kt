package com.safescreen.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.safescreen.app.presentation.theme.NeonCyan

/**
 * Custom tactile time limit slider for social media applications.
 * Stepped in minutes (e.g. 15m, 30m, 45m, 1h 00m, etc.) with a glowing Neon Cyan track.
 */
@Composable
fun TimeLimitSlider(
    valueMinutes: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    minMinutes: Int = 15,
    maxMinutes: Int = 180,
    stepMinutes: Int = 15,
    activeColor: Color = NeonCyan
) {
    val steps = ((maxMinutes - minMinutes) / stepMinutes) - 1

    Slider(
        value = valueMinutes.toFloat(),
        onValueChange = { rawValue ->
            val snapped = ((rawValue / stepMinutes).toInt() * stepMinutes).coerceIn(minMinutes, maxMinutes)
            onValueChange(snapped)
        },
        valueRange = minMinutes.toFloat()..maxMinutes.toFloat(),
        steps = if (steps > 0) steps else 0,
        colors = SliderDefaults.colors(
            thumbColor = Color.White,
            activeTrackColor = activeColor,
            inactiveTrackColor = Color(0xFF1E2838),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Helper utility to format minutes into human readable "1h 30m" format.
 */
fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "Instant Block (0m)"
    val hours = minutes / 60
    val remainingMins = minutes % 60
    return when {
        hours > 0 && remainingMins > 0 -> "${hours}h ${remainingMins}m"
        hours > 0 -> "${hours}h 00m"
        else -> "${remainingMins}m"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun TimeLimitSliderPreview() {
    TimeLimitSlider(
        valueMinutes = 90,
        onValueChange = {}
    )
}
