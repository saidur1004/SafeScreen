package com.safescreen.app.presentation.screens.states

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.SafeScreenStateType
import com.safescreen.app.presentation.components.SafeScreenStateView
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import com.safescreen.app.presentation.theme.WarningYellow

/**
 * Screen 16: Interactive Empty, Warning & Error States Screen.
 *
 * Implements Module 08:
 * - Decoupled state viewer with top segment switcher:
 *   [All Clean] | [Shield Paused] | [AI Core Error] | [Empty Log]
 * - Demonstrates all 4 states for live review and automated audits.
 */
@Composable
fun EmptyAndErrorStatesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedState by remember { mutableStateOf(SafeScreenStateType.ALL_CLEAN) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .statusBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "System Status States",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 17.sp
                )
            )

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(40.dp)) // Visual balance
        }

        // Top Segmented State Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1726))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StateTabItem(
                title = "Clean",
                isSelected = selectedState == SafeScreenStateType.ALL_CLEAN,
                activeColor = NeonGreen,
                onClick = { selectedState = SafeScreenStateType.ALL_CLEAN },
                modifier = Modifier.weight(1f)
            )
            StateTabItem(
                title = "Paused",
                isSelected = selectedState == SafeScreenStateType.SHIELD_PAUSED,
                activeColor = WarningYellow,
                onClick = { selectedState = SafeScreenStateType.SHIELD_PAUSED },
                modifier = Modifier.weight(1f)
            )
            StateTabItem(
                title = "AI Error",
                isSelected = selectedState == SafeScreenStateType.AI_CORE_ERROR,
                activeColor = NeonCyan,
                onClick = { selectedState = SafeScreenStateType.AI_CORE_ERROR },
                modifier = Modifier.weight(1f)
            )
            StateTabItem(
                title = "Empty Log",
                isSelected = selectedState == SafeScreenStateType.EMPTY_LOG,
                activeColor = NeonPurple,
                onClick = { selectedState = SafeScreenStateType.EMPTY_LOG },
                modifier = Modifier.weight(1f)
            )
        }

        // Animated State Content
        AnimatedContent(
            targetState = selectedState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "StateTransition",
            modifier = Modifier.weight(1f)
        ) { state ->
            SafeScreenStateView(
                stateType = state,
                onActionClick = {
                    when (state) {
                        SafeScreenStateType.ALL_CLEAN -> {
                            Toast.makeText(context, "System Status: 100% Protected & Clean", Toast.LENGTH_SHORT).show()
                        }
                        SafeScreenStateType.SHIELD_PAUSED -> {
                            Toast.makeText(context, "Navigating to System Permissions...", Toast.LENGTH_SHORT).show()
                        }
                        SafeScreenStateType.AI_CORE_ERROR -> {
                            Toast.makeText(context, "Reloading local TFLite neural weights...", Toast.LENGTH_SHORT).show()
                        }
                        SafeScreenStateType.EMPTY_LOG -> {
                            Toast.makeText(context, "Refreshing Activity Log...", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun StateTabItem(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) activeColor.copy(alpha = 0.22f)
                else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.2.dp else 0.dp,
                color = if (isSelected) activeColor else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else TextMuted,
                fontSize = 12.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyAndErrorStatesScreenPreview() {
    EmptyAndErrorStatesScreen(onNavigateBack = {})
}
