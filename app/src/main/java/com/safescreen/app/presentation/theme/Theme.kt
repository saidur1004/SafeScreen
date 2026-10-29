package com.safescreen.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SafeScreenDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepBlack,
    primaryContainer = NeonCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = NeonGreen,
    onSecondary = DeepBlack,
    secondaryContainer = NeonGreenDark,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonPurple,
    onTertiary = TextPrimary,
    error = NeonPink,
    onError = DeepBlack,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceHighlight
)

@Composable
fun SafeScreenTheme(
    darkTheme: Boolean = true, // SafeScreen defaults to a sleek dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = SafeScreenDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

