package com.safescreen.app.presentation.screens.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.safescreen.app.presentation.theme.DarkSurfaceElevated
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var nsfwFilterEnabled by remember { mutableStateOf(true) }
    var audioMuteEnabled by remember { mutableStateOf(true) }
    var reelsBlockEnabled by remember { mutableStateOf(true) }
    var biometricLockEnabled by remember { mutableStateOf(false) }

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
                    text = "Protection Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    SettingsToggleCard(
                        title = "Visual NSFW Filter",
                        description = "Real-time AI blur on explicit imagery and video content.",
                        checked = nsfwFilterEnabled,
                        onCheckedChange = { nsfwFilterEnabled = it }
                    )
                }
                item {
                    SettingsToggleCard(
                        title = "Audio Auto-Mute",
                        description = "Intercept and mute background music using on-device ML.",
                        checked = audioMuteEnabled,
                        onCheckedChange = { audioMuteEnabled = it }
                    )
                }
                item {
                    SettingsToggleCard(
                        title = "Reels & Shorts Blocker",
                        description = "Enforce daily limits on YouTube Shorts, Instagram Reels & TikTok.",
                        checked = reelsBlockEnabled,
                        onCheckedChange = { reelsBlockEnabled = it }
                    )
                }
                item {
                    SettingsToggleCard(
                        title = "PIN & Biometric Lock",
                        description = "Require authentication to modify settings or disarm shield.",
                        checked = biometricLockEnabled,
                        onCheckedChange = { biometricLockEnabled = it }
                    )
                }
                item {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Text(
                                text = "App Whitelist",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Manage excluded applications exempted from shield restrictions.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DeepBlack,
                    checkedTrackColor = NeonCyan,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = DarkSurfaceElevated
                )
            )
        }
    }
}

