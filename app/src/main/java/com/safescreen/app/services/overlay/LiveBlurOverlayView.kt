package com.safescreen.app.services.overlay

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data model for a single targeted shield blackout box.
 */
data class ShieldBox(
    val id: Int,
    var rect: RectF, // Normalized 0..1 coordinates
    var label: String = "Non-Mahram Guarded",
    var lastSeenMs: Long = 0L
)

/**
 * Targeted Bounding Box Blackout & Full-Page Domain Blocking Overlay View.
 *
 * Implements strict KafGuard-style targeted non-mahram protection:
 * - 100% Transparent root canvas (Does not obscure underlying app/text)
 * - Exact targeted Solid Black (#000000) boxes over detected female faces/figures
 * - Touch pass-through enabled via FLAG_NOT_TOUCHABLE in WindowManager
 * - Sleek cyber border and single-line "Non-Mahram Guarded" badge
 * - Full-page solid blackout when a blacklisted adult domain is detected
 */
@Composable
fun LiveBlurOverlayContent(
    boxes: List<ShieldBox> = emptyList(),
    blockedDomain: String? = null,
    onDismissBlock: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. Extreme Adult Domain Block: 100% Solid Full-Screen Blackout
    if (blockedDomain != null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black), // 100% Solid Pitch Black
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.2f))
                        .border(2.dp, Color(0xFFEF4444), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "WEBSITE ACCESS BLOCKED",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Harmful adult content prohibited by SafeScreen AI\n(Strict Islamic Protection Active)",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = blockedDomain,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(28.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEF4444))
                        .clickable(onClick = onDismissBlock)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Leave Website",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
        return
    }

    // 2. Targeted Bounding Box Blackout Mode
    if (boxes.isEmpty()) return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent) // 100% Transparent, whole screen remains visible
    ) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current

        val cyberBorder = Brush.linearGradient(
            listOf(
                Color(0xFF00E5FF).copy(alpha = 0.9f),
                Color(0xFF10B981).copy(alpha = 0.7f)
            )
        )

        // Draw 100% Solid Pitch Black (#000000) Opaque Boxes strictly over target coordinates
        for (box in boxes) {
            val left = (box.rect.left * screenWidthPx).coerceAtLeast(0f)
            val top = (box.rect.top * screenHeightPx).coerceAtLeast(0f)
            val right = (box.rect.right * screenWidthPx).coerceAtMost(screenWidthPx)
            val bottom = (box.rect.bottom * screenHeightPx).coerceAtMost(screenHeightPx)

            val boxW = (right - left).coerceAtLeast(16f)
            val boxH = (bottom - top).coerceAtLeast(16f)

            val widthDp = with(density) { boxW.toDp() }
            val heightDp = with(density) { boxH.toDp() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(left.toInt(), top.toInt()) }
                    .size(widthDp, heightDp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black) // 100% Solid Opaque Pitch Black Box
                    .border(2.dp, cyberBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Render sleek cyber badge when space permits; otherwise sleek eye-off icon
                if (widthDp >= 115.dp && heightDp >= 42.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Non-Mahram Guarded",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                } else if (widthDp >= 32.dp && heightDp >= 32.dp) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
private fun TargetedBlurOverlayPreview() {
    LiveBlurOverlayContent(
        boxes = listOf(
            ShieldBox(1, RectF(0.1f, 0.2f, 0.9f, 0.5f), "Non-Mahram Guarded")
        )
    )
}
