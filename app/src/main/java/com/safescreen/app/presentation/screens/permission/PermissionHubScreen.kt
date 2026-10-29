package com.safescreen.app.presentation.screens.permission

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.safescreen.app.presentation.components.GlassmorphicCard
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

@Composable
fun PermissionHubScreen(
    onAllPermissionsGranted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp)
        ) {
            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SafeScreen needs local access to inspect frames and audio on-device. No data is stored or transmitted.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    PermissionCard(
                        icon = Icons.Default.Layers,
                        title = "Display Over Other Apps",
                        description = "Enables drawing real-time blur shields over explicit content.",
                        isGranted = true
                    )
                }
                item {
                    PermissionCard(
                        icon = Icons.AutoMirrored.Filled.ScreenShare,
                        title = "Screen Capture (MediaProjection)",
                        description = "Allows on-device AI frame inspection for content classification.",
                        isGranted = false
                    )
                }
                item {
                    PermissionCard(
                        icon = Icons.Default.Mic,
                        title = "Audio Playback Capture",
                        description = "Captures internal audio playback to detect and mute music.",
                        isGranted = false
                    )
                }
                item {
                    PermissionCard(
                        icon = Icons.Default.Notifications,
                        title = "Foreground Notifications",
                        description = "Required to keep the background protection shield running stably.",
                        isGranted = true
                    )
                }
            }
        }

        Button(
            onClick = onAllPermissionsGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonGreen,
                contentColor = DeepBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "Proceed to Shield Hub",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isGranted) NeonGreen else NeonCyan,
                modifier = Modifier.size(32.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Granted",
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

