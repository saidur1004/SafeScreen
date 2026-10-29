package com.safescreen.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.safescreen.app.presentation.theme.NeonPurple
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit
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
                .padding(bottom = 72.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to\nSafeScreen",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Pure on-device protection with 100% privacy assurance. Zero data leaves your device.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            FeatureItem(
                icon = Icons.Default.VisibilityOff,
                iconTint = NeonCyan,
                title = "Visual Shield",
                description = "Instantly blurs explicit images and videos in real time using local AI."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureItem(
                icon = Icons.Default.Hearing,
                iconTint = NeonGreen,
                title = "Smart Audio Filter",
                description = "Silences or mutes music in supported apps using on-device ML."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureItem(
                icon = Icons.Default.HourglassTop,
                iconTint = NeonPurple,
                title = "Doomscroll Control",
                description = "Limits excessive Reels, Shorts & TikTok endless scrolling."
            )
        }

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = DeepBlack
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        }
    }
}

