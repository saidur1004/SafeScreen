package com.safescreen.app.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.unit.dp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.components.ShieldButton
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

@Composable
fun HomeScreen(
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var isShieldActive by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SafeScreen",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "On-Device Neural Guardian",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                    )
                }

                Row {
                    IconButton(onClick = onNavigateToStats) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Stats",
                            tint = NeonCyan
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Central Shield Toggle
            ShieldButton(
                isActive = isShieldActive,
                onToggle = { isShieldActive = !isShieldActive }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Live Metrics Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Blocked Today",
                    value = "24",
                    subtitle = "Explicit frames blurred",
                    color = NeonPink,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Music Muted",
                    value = "12",
                    subtitle = "Audio tracks intercepted",
                    color = NeonCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Reels & Shorts Timer",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Daily limit: 15 / 30 mins used",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                    Text(
                        text = "50%",
                        style = MaterialTheme.typography.titleLarge.copy(color = NeonGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(modifier = modifier) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(color = color)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        }
    }
}

