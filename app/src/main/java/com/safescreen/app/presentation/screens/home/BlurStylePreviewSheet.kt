package com.safescreen.app.presentation.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.TextSecondary
import kotlin.math.roundToInt

/**
 * Filter styling options for NSFW screen obfuscation.
 */
enum class BlurStyle(
    val title: String,
    val subtitle: String
) {
    FROSTED_GLASS("Frosted Glass", "Smooth Gaussian\nblur"),
    PIXELATED("Pixelated Censor", "8-bit digital\nblocks"),
    OPAQUE("Opaque Dark Mask", "Total black\nshield")
}

/**
 * BlurStylePreviewSheet (Module 03 / Screen 06)
 *
 * Material 3 ModalBottomSheet that allows users to select their preferred visual
 * obfuscation technique, adjust the filter intensity, and interactively drag a
 * Before / After split comparison slider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlurStylePreviewSheet(
    initialStyle: BlurStyle = BlurStyle.FROSTED_GLASS,
    initialIntensity: Float = 0.85f,
    onDismiss: () -> Unit = {},
    onApply: (BlurStyle, Float) -> Unit = { _, _ -> }
) {
    var selectedStyle by remember { mutableStateOf(initialStyle) }
    var blurIntensity by remember { mutableFloatStateOf(initialIntensity) }
    var splitRatio by remember { mutableFloatStateOf(0.50f) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            // Frosted pill drag handle
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // 1. Header Section: Palette Icon + Title + Subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(
                            width = 1.dp,
                            color = NeonCyan.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(11.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Shield Blur Customization",
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Shield Blur Customization",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose your preferred blur style for explicit content.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Three Selectable Style Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BlurStyleCard(
                    style = BlurStyle.FROSTED_GLASS,
                    isSelected = selectedStyle == BlurStyle.FROSTED_GLASS,
                    onClick = { selectedStyle = BlurStyle.FROSTED_GLASS },
                    modifier = Modifier.weight(1f)
                )

                BlurStyleCard(
                    style = BlurStyle.PIXELATED,
                    isSelected = selectedStyle == BlurStyle.PIXELATED,
                    onClick = { selectedStyle = BlurStyle.PIXELATED },
                    modifier = Modifier.weight(1f)
                )

                BlurStyleCard(
                    style = BlurStyle.OPAQUE,
                    isSelected = selectedStyle == BlurStyle.OPAQUE,
                    onClick = { selectedStyle = BlurStyle.OPAQUE },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Blur Intensity Slider Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Blur Intensity",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "${(blurIntensity * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonCyan,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Slider(
                value = blurIntensity,
                onValueChange = { blurIntensity = it },
                valueRange = 0.15f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = NeonCyan,
                    inactiveTrackColor = Color(0xFF1E2838)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Interactive Split Comparison Preview ("Before / After" Mockup)
            InteractiveSplitComparisonView(
                style = selectedStyle,
                intensity = blurIntensity,
                splitRatio = splitRatio,
                onSplitRatioChange = { splitRatio = it }
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Primary Action Button: "✔ Apply Changes"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(NeonCyan)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onApply(selectedStyle, blurIntensity)
                            onDismiss()
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = DeepBlack,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply Changes",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepBlack,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

/**
 * Individual selectable blur style thumbnail card.
 */
@Composable
private fun BlurStyleCard(
    style: BlurStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) NeonCyan else Color.White.copy(alpha = 0.12f)
    val borderWidth = if (isSelected) 1.6.dp else 1.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF111A29))
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Miniature Style Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0A121F))
            ) {
                StyleThumbnailCanvas(
                    style = style,
                    modifier = Modifier.fillMaxSize()
                )

                // Selected Checkmark Pill Badge at Top-Right
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(NeonCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = DeepBlack,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Style Title
            Text(
                text = style.title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) NeonCyan else TextPrimary,
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Style Subtitle
            Text(
                text = style.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextMuted,
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * Thumbnail canvas displaying miniature graphic previews for Frosted Glass,
 * Pixelated Censor, and Opaque Dark Mask.
 */
@Composable
private fun StyleThumbnailCanvas(
    style: BlurStyle,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (style) {
            BlurStyle.FROSTED_GLASS -> {
                // Miniature sky
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7)),
                        startY = 0f,
                        endY = h * 0.5f
                    )
                )
                // Miniature mountain
                val mountain = Path().apply {
                    moveTo(0f, h)
                    lineTo(w * 0.45f, h * 0.25f)
                    lineTo(w * 0.85f, h * 0.65f)
                    lineTo(w, h * 0.45f)
                    lineTo(w, h)
                    close()
                }
                drawPath(mountain, color = Color(0xFF1E3A5F))

                // Miniature lake
                drawRect(
                    color = Color(0xFF0C4A6E),
                    topLeft = Offset(0f, h * 0.65f),
                    size = Size(w, h * 0.35f)
                )

                // Smooth translucent frosted glass overlay
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.40f),
                            Color(0xFF0284C7).copy(alpha = 0.55f),
                            Color(0xFF0C4A6E).copy(alpha = 0.70f)
                        )
                    )
                )
            }

            BlurStyle.PIXELATED -> {
                // Draw 8-bit digital blocks grid
                val cols = 7
                val rows = 5
                val cellW = w / cols
                val cellH = h / rows

                val palette = listOf(
                    Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1),
                    Color(0xFF1E3A5F), Color(0xFF15803D), Color(0xFF0C4A6E),
                    Color(0xFF0F172A), Color(0xFF475569)
                )

                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val colorIdx = (r * 3 + c * 2) % palette.size
                        drawRect(
                            color = palette[colorIdx],
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH)
                        )
                        // Subtle pixel boundary line
                        drawRect(
                            color = Color.Black.copy(alpha = 0.20f),
                            topLeft = Offset(c * cellW, r * cellH),
                            size = Size(cellW, cellH),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.7f)
                        )
                    }
                }
            }

            BlurStyle.OPAQUE -> {
                // Pitch black privacy mask with subtle centered lock
                drawRect(color = Color(0xFF05080E))
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(w * 0.15f, h * 0.18f),
                    size = Size(w * 0.70f, h * 0.64f),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
    }
}

/**
 * Interactive Before / After Split Comparison View.
 *
 * Left: Original clean scenic artwork.
 * Right: Filtered view (Frosted Glass, Pixelated, or Opaque Mask) with intensity scaling.
 * Center: Touch-draggable vertical divider with "< >" pill handle.
 */
@Composable
private fun InteractiveSplitComparisonView(
    style: BlurStyle,
    intensity: Float,
    splitRatio: Float,
    onSplitRatioChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var containerWidthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(18.dp))
            .onSizeChanged { containerWidthPx = it.width.toFloat() }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(18.dp)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    val totalW = if (containerWidthPx > 0f) containerWidthPx else size.width.toFloat()
                    val newRatio = (splitRatio + (dragAmount / totalW)).coerceIn(0.08f, 0.92f)
                    onSplitRatioChange(newRatio)
                }
            }
    ) {
        // High-Resolution Scenic Artwork Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val splitX = w * splitRatio

            // ==========================================
            // LAYER 1: Full Original Scenic Artwork
            // ==========================================
            fun drawScenicArtwork() {
                // Sky gradient with sun glow
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF38BDF8),
                            Color(0xFF67E8F9),
                            Color(0xFFBAE6FD)
                        ),
                        startY = 0f,
                        endY = h * 0.55f
                    )
                )

                // Sun
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFFFEF08A), Color(0xFFFDE047).copy(alpha = 0.4f), Color.Transparent)
                    ),
                    radius = 24.dp.toPx(),
                    center = Offset(w * 0.20f, h * 0.22f)
                )

                // Fluffy clouds
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(w * 0.52f, h * 0.12f),
                    size = Size(65.dp.toPx(), 18.dp.toPx()),
                    cornerRadius = CornerRadius(9.dp.toPx())
                )

                // Distant Mountain Peaks (High Sierra)
                val peak1 = Path().apply {
                    moveTo(0f, h * 0.70f)
                    lineTo(w * 0.35f, h * 0.18f)
                    lineTo(w * 0.70f, h * 0.70f)
                    close()
                }
                drawPath(peak1, color = Color(0xFF334155))

                // Snow cap on peak 1
                val snowCap1 = Path().apply {
                    moveTo(w * 0.27f, h * 0.30f)
                    lineTo(w * 0.35f, h * 0.18f)
                    lineTo(w * 0.43f, h * 0.30f)
                    lineTo(w * 0.39f, h * 0.27f)
                    lineTo(w * 0.35f, h * 0.32f)
                    lineTo(w * 0.31f, h * 0.27f)
                    close()
                }
                drawPath(snowCap1, color = Color.White)

                // Mountain Peak 2
                val peak2 = Path().apply {
                    moveTo(w * 0.40f, h * 0.70f)
                    lineTo(w * 0.72f, h * 0.24f)
                    lineTo(w, h * 0.70f)
                    close()
                }
                drawPath(peak2, color = Color(0xFF1E293B))

                // Snow cap on peak 2
                val snowCap2 = Path().apply {
                    moveTo(w * 0.65f, h * 0.35f)
                    lineTo(w * 0.72f, h * 0.24f)
                    lineTo(w * 0.79f, h * 0.35f)
                    lineTo(w * 0.75f, h * 0.32f)
                    lineTo(w * 0.72f, h * 0.36f)
                    close()
                }
                drawPath(snowCap2, color = Color.White)

                // Evergreen Pine Trees Belt
                for (i in 0..18) {
                    val treeX = w * (i.toFloat() / 18f)
                    val treeHeight = (18 + (i % 5) * 4).dp.toPx()
                    val treeY = h * 0.65f - treeHeight * 0.75f

                    val treePath = Path().apply {
                        moveTo(treeX, treeY)
                        lineTo(treeX + 8.dp.toPx(), treeY + treeHeight)
                        lineTo(treeX - 8.dp.toPx(), treeY + treeHeight)
                        close()
                    }
                    val treeColor = if (i % 2 == 0) Color(0xFF166534) else Color(0xFF14532D)
                    drawPath(treePath, color = treeColor)
                }

                // Tranquil Lake Water in Foreground
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFF0284C7),
                            Color(0xFF0369A1),
                            Color(0xFF075985)
                        ),
                        startY = h * 0.62f,
                        endY = h
                    ),
                    topLeft = Offset(0f, h * 0.62f),
                    size = Size(w, h * 0.38f)
                )

                // Water reflection ripple lines
                drawLine(
                    color = Color.White.copy(alpha = 0.35f),
                    start = Offset(w * 0.15f, h * 0.75f),
                    end = Offset(w * 0.45f, h * 0.75f),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.25f),
                    start = Offset(w * 0.55f, h * 0.82f),
                    end = Offset(w * 0.85f, h * 0.82f),
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Draw full base scene
            drawScenicArtwork()

            // ==========================================
            // LAYER 2: Obfuscation Filter on the Right Side (splitX -> width)
            // ==========================================
            clipRect(left = splitX, top = 0f, right = w, bottom = h) {
                when (style) {
                    BlurStyle.FROSTED_GLASS -> {
                        // Multi-pass frosted diffused blur simulation with intensity scaling
                        val alpha1 = (0.50f + intensity * 0.45f).coerceIn(0f, 0.98f)
                        val alpha2 = (0.35f + intensity * 0.55f).coerceIn(0f, 0.95f)

                        // 1. Heavy diffused color washes
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0xFF38BDF8).copy(alpha = alpha1),
                                    Color(0xFF0284C7).copy(alpha = alpha2),
                                    Color(0xFF075985).copy(alpha = alpha1)
                                )
                            )
                        )
                        // 2. Frosted glass specular sheen
                        drawRect(
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.35f * intensity),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.18f * intensity)
                                )
                            )
                        )
                    }

                    BlurStyle.PIXELATED -> {
                        // Digital Mosaic blocks scaled by intensity
                        val blockSize = ((24 - (intensity * 10)).coerceAtLeast(10f)).dp.toPx()
                        val cols = ((w - splitX) / blockSize).toInt() + 1
                        val rows = (h / blockSize).toInt() + 1

                        val pixelPalette = listOf(
                            Color(0xFF38BDF8), Color(0xFF67E8F9), Color(0xFF0284C7),
                            Color(0xFF334155), Color(0xFF1E293B), Color(0xFF166534),
                            Color(0xFF0369A1), Color(0xFF075985), Color(0xFFCBD5E1)
                        )

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val cellLeft = splitX + c * blockSize
                                val cellTop = r * blockSize
                                val colorIndex = ((r * 4 + c * 3) + ((1f - intensity) * 10).toInt()) % pixelPalette.size
                                drawRect(
                                    color = pixelPalette[colorIndex.coerceIn(0, pixelPalette.size - 1)],
                                    topLeft = Offset(cellLeft, cellTop),
                                    size = Size(blockSize, blockSize)
                                )
                                // Subtle pixel matrix border
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.22f),
                                    topLeft = Offset(cellLeft, cellTop),
                                    size = Size(blockSize, blockSize),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.7f)
                                )
                            }
                        }
                    }

                    BlurStyle.OPAQUE -> {
                        // Total black censor shield
                        val maskAlpha = (0.75f + intensity * 0.25f).coerceIn(0f, 1.0f)
                        drawRect(
                            color = Color(0xFF04060A).copy(alpha = maskAlpha)
                        )
                    }
                }
            }

            // ==========================================
            // LAYER 3: Center Split Divider Line
            // ==========================================
            drawLine(
                color = Color.White.copy(alpha = 0.90f),
                start = Offset(splitX, 0f),
                end = Offset(splitX, h),
                strokeWidth = 2.dp.toPx()
            )
        }

        // ==========================================
        // LAYER 4: Draggable Center Handle (< > Pill)
        // ==========================================
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset {
                    val handleRadius = 17.dp.toPx()
                    val targetX = (containerWidthPx * splitRatio) - handleRadius
                    IntOffset(x = targetX.roundToInt(), y = 0)
                }
                .size(34.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = NeonCyan,
                    spotColor = Color.Black
                )
                .clip(CircleShape)
                .background(Color(0xEE0B121E))
                .border(width = 1.5.dp, color = NeonCyan, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "< >",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    fontSize = 11.sp,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun BlurStylePreviewSheetPreview() {
    BlurStylePreviewSheet()
}
