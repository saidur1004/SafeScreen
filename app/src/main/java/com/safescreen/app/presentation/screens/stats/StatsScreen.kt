package com.safescreen.app.presentation.screens.stats

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.NeonPink
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 20.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Protection Insights",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Privacy Guarantee Banner
            GlassmorphicCard(
                borderColor = NeonGreen.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Privacy",
                        tint = NeonGreen
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = "100% On-Device Local Data",
                            style = MaterialTheme.typography.titleMedium.copy(color = NeonGreen)
                        )
                        Text(
                            text = "Stats are stored in Room SQLite on your device only. Never transmitted.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Summary (Past 7 Days)",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    StatRow(
                        category = "Explicit Visuals Filtered",
                        count = "142 instances",
                        color = NeonPink
                    )
                }
                item {
                    StatRow(
                        category = "Background Music Muted",
                        count = "78 tracks",
                        color = NeonCyan
                    )
                }
                item {
                    StatRow(
                        category = "Reels / Shorts Time Saved",
                        count = "4 hrs 20 mins",
                        color = NeonGreen
                    )
                }
                item {
                    StatRow(
                        category = "Doomscroll Interventions",
                        count = "35 sessions",
                        color = NeonPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    category: String,
    count: String,
    color: androidx.compose.ui.graphics.Color
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
            )
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(color = color)
            )
        }
    }
}

