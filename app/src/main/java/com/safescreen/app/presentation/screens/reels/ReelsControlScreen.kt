package com.safescreen.app.presentation.screens.reels

import androidx.compose.runtime.Composable
import com.safescreen.app.presentation.screens.focus.ReelsControlScreen as FocusReelsControlScreen

/**
 * Re-exporting ReelsControlScreen under the reels package for backward and alternate import compatibility.
 */
@Composable
fun ReelsControlScreen(
    onNavigateBack: () -> Unit = {}
) {
    FocusReelsControlScreen(onNavigateBack = onNavigateBack)
}
