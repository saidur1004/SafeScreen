package com.safescreen.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GlassmorphicCard
 *
 * Implements high-end Cyber-Dark Glassmorphism with a semi-transparent
 * multi-stop dark slate gradient, a luminous top-left 1dp highlight border,
 * and elegant rounded corners.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color = Color.White.copy(alpha = 0.22f),
    backgroundColor: Color = Color(0xFF131D2D),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.82f),
                        backgroundColor.copy(alpha = 0.55f),
                        Color(0xFF090E18).copy(alpha = 0.88f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor.copy(alpha = 0.28f),
                        borderColor.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}
