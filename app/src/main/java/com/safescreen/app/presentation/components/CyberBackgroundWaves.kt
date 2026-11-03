package com.safescreen.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen

/**
 * CyberBackgroundWaves
 *
 * Renders the signature glowing ambient silk curves and subtle radial auras
 * seen at the bottom of SafeScreen's flagship screens.
 */
@Composable
fun CyberBackgroundWaves(
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    intensity: Float = 1.0f
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val h = size.height

        // Ambient radial light pooling behind waves
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.14f * intensity),
                    NeonGreen.copy(alpha = 0.06f * intensity),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.85f),
                radius = w * 0.65f
            )
        )

        // Wave Layer 1: Cyan Silk Curve
        val wavePath1 = Path().apply {
            moveTo(0f, h * 0.75f)
            cubicTo(
                w * 0.25f, h * 0.45f,
                w * 0.55f, h * 0.95f,
                w, h * 0.55f
            )
        }
        drawPath(
            path = wavePath1,
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.05f * intensity),
                    NeonCyan.copy(alpha = 0.55f * intensity),
                    NeonGreen.copy(alpha = 0.35f * intensity),
                    Color.Transparent
                ),
                start = Offset(0f, h * 0.6f),
                end = Offset(w, h * 0.6f)
            ),
            style = Stroke(width = 2.2.dp.toPx())
        )

        // Wave Layer 2: Bio-Green Accent Curve
        val wavePath2 = Path().apply {
            moveTo(0f, h * 0.85f)
            cubicTo(
                w * 0.35f, h * 0.65f,
                w * 0.70f, h * 0.90f,
                w, h * 0.68f
            )
        }
        drawPath(
            path = wavePath2,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    NeonGreen.copy(alpha = 0.45f * intensity),
                    NeonCyan.copy(alpha = 0.25f * intensity),
                    Color.Transparent
                ),
                start = Offset(w * 0.15f, h * 0.75f),
                end = Offset(w * 0.95f, h * 0.75f)
            ),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Wave Layer 3: Faint tertiary luminous trail
        val wavePath3 = Path().apply {
            moveTo(0f, h * 0.68f)
            cubicTo(
                w * 0.30f, h * 0.88f,
                w * 0.65f, h * 0.50f,
                w, h * 0.80f
            )
        }
        drawPath(
            path = wavePath3,
            brush = Brush.linearGradient(
                colors = listOf(
                    NeonCyan.copy(alpha = 0.25f * intensity),
                    Color.Transparent
                ),
                start = Offset(0f, h * 0.7f),
                end = Offset(w * 0.6f, h * 0.7f)
            ),
            style = Stroke(width = 1.0.dp.toPx())
        )
    }
}

