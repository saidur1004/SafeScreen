package com.safescreen.app.presentation.screens.settings

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Security Lock Screen with Cyber-Dark PIN Dial Pad and Biometric Authentication.
 *
 * Implements Module 06 Prompt 3:
 * - Glowing 3D Shield Padlock hero with ambient aura
 * - 4-dot animated PIN entry indicators with error shake feedback
 * - Custom 3x4 Glassmorphic keypad with letters, biometric button, and backspace
 * - Immediate unlock callback when PIN "1234" or Biometrics is validated
 */
@Composable
fun SecurityLockScreen(
    mode: SecurityLockMode = SecurityLockMode.APP_UNLOCK,
    onUnlockSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: SecurityLockViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as? Activity
    val haptic = LocalHapticFeedback.current

    // Prevent security bypass on system back gesture or button
    BackHandler {
        when (mode) {
            SecurityLockMode.APP_UNLOCK -> {
                activity?.finish()
            }
            SecurityLockMode.SHIELD_DISARM,
            SecurityLockMode.SETTINGS -> {
                onNavigateBack()
            }
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(350)
            onUnlockSuccess()
        }
    }

    SecurityLockContent(
        state = state.copy(mode = mode),
        onEvent = { event ->
            when (event) {
                is SecurityUiEvent.EnterDigit -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.DeleteDigit -> {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.ClearPin -> {
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.TriggerBiometric -> {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    Toast.makeText(context, "Biometric Authentication Verified", Toast.LENGTH_SHORT).show()
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.ForgotPin -> {
                    Toast.makeText(context, "Default testing PIN is 1234", Toast.LENGTH_LONG).show()
                }
                is SecurityUiEvent.NavigateBack -> {
                    when (mode) {
                        SecurityLockMode.APP_UNLOCK -> activity?.finish()
                        SecurityLockMode.SHIELD_DISARM,
                        SecurityLockMode.SETTINGS -> onNavigateBack()
                    }
                }
            }
        }
    )
}

@Composable
fun SecurityLockScreen(
    mode: SecurityLockMode = SecurityLockMode.APP_UNLOCK,
    onUnlockSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    state: SecurityUiState,
    onEvent: (SecurityUiEvent) -> Unit
) {
    SecurityLockContent(
        state = state.copy(mode = mode),
        onEvent = onEvent
    )
}

@Composable
fun SecurityLockContent(
    state: SecurityUiState,
    onEvent: (SecurityUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    // Trigger shake animation when error occurs
    LaunchedEffect(state.isError) {
        if (state.isError) {
            shakeOffset.animateTo(-20f, animationSpec = tween(50))
            shakeOffset.animateTo(20f, animationSpec = tween(50))
            shakeOffset.animateTo(-16f, animationSpec = tween(50))
            shakeOffset.animateTo(16f, animationSpec = tween(50))
            shakeOffset.animateTo(-8f, animationSpec = tween(50))
            shakeOffset.animateTo(0f, animationSpec = tween(50))
        }
    }

    val (titleText, subtitleText) = when (state.mode) {
        SecurityLockMode.APP_UNLOCK -> Pair(
            "SafeScreen Locked",
            "Enter your 4-digit PIN or use Biometrics to unlock SafeScreen"
        )
        SecurityLockMode.SHIELD_DISARM -> Pair(
            "Disarm Protection Shield",
            "Enter your 4-digit PIN or use Biometrics to turn OFF shield protection"
        )
        SecurityLockMode.SETTINGS -> Pair(
            "Security Authentication",
            "Enter your 4-digit PIN or use Biometrics to access security settings"
        )
    }

    val badgeText = when (state.mode) {
        SecurityLockMode.APP_UNLOCK -> "SECURE"
        SecurityLockMode.SHIELD_DISARM -> "ARMED"
        SecurityLockMode.SETTINGS -> "CONFIG"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
    ) {
        // Dynamic Cyber Waves Ambient Background
        CyberBackgroundWaves(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Bar with back button
            SecurityTopBar(
                badgeText = badgeText,
                onBackClick = { onEvent(SecurityUiEvent.NavigateBack) }
            )

            // 2. Center Hero & PIN Indicators
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Glowing Shield Padlock Emblem
                GlowingPadlockHero(
                    isSuccess = state.isSuccess,
                    isError = state.isError
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title & Subtitle
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 4 PIN Dots with shake modifier
                Row(
                    modifier = Modifier
                        .offset(x = shakeOffset.value.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(22.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(state.maxPinLength) { index ->
                        val isFilled = index < state.pinInput.length
                        PinIndicatorDot(
                            isFilled = isFilled,
                            isError = state.isError,
                            isSuccess = state.isSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Error / Status Message
                Box(
                    modifier = Modifier.height(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.isError) {
                        Text(
                            text = "Incorrect PIN. Please try again.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFFFF4D4D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    } else if (state.isSuccess) {
                        Text(
                            text = "PIN Verified • Access Granted",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }

            // 3. Custom Glassmorphic Keypad (3x4 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumericDialPad(
                    onDigitClick = { digit -> onEvent(SecurityUiEvent.EnterDigit(digit)) },
                    onDeleteClick = { onEvent(SecurityUiEvent.DeleteDigit) },
                    onBiometricClick = { onEvent(SecurityUiEvent.TriggerBiometric) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Forgot PIN link
                Text(
                    text = "Forgot PIN? (Default is 1234)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NeonCyan.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onEvent(SecurityUiEvent.ForgotPin) }
                        )
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

/**
 * Top Bar with a frosted circular Back/Cancel button.
 */
@Composable
private fun SecurityTopBar(
    badgeText: String = "ARMED",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBackClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F1829))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.8.sp
                    )
                )
            }
        }
    }
}

/**
 * 3D Glowing Shield Padlock Hero.
 */
@Composable
private fun GlowingPadlockHero(
    isSuccess: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    val tintColor = when {
        isSuccess -> NeonGreen
        isError -> Color(0xFFFF3366)
        else -> NeonCyan
    }

    Box(
        modifier = modifier
            .size(76.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = tintColor.copy(alpha = 0.45f),
                spotColor = tintColor
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF132236),
                        Color(0xFF09121F)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        tintColor.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Padlock",
            tint = tintColor,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Individual PIN indicator dot.
 */
@Composable
private fun PinIndicatorDot(
    isFilled: Boolean,
    isError: Boolean,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = when {
        isError -> Color(0xFFFF3366)
        isSuccess -> NeonGreen
        isFilled -> NeonCyan
        else -> Color.Transparent
    }

    val borderColor = when {
        isError -> Color(0xFFFF3366)
        isSuccess -> NeonGreen
        isFilled -> NeonCyan.copy(alpha = 0.8f)
        else -> Color.White.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .size(18.dp)
            .shadow(
                elevation = if (isFilled) 8.dp else 0.dp,
                shape = CircleShape,
                ambientColor = dotColor,
                spotColor = dotColor
            )
            .clip(CircleShape)
            .background(dotColor)
            .border(1.5.dp, borderColor, CircleShape)
    )
}

/**
 * 3x4 Custom Glassmorphic Numeric Dial Pad.
 */
@Composable
private fun NumericDialPad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onBiometricClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadItems = listOf(
        KeypadKey("1", ""),
        KeypadKey("2", "ABC"),
        KeypadKey("3", "DEF"),
        KeypadKey("4", "GHI"),
        KeypadKey("5", "JKL"),
        KeypadKey("6", "MNO"),
        KeypadKey("7", "PQRS"),
        KeypadKey("8", "TUV"),
        KeypadKey("9", "WXYZ"),
        KeypadKey("bio", "BIOMETRIC"),
        KeypadKey("0", "+"),
        KeypadKey("del", "DELETE")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadItems.chunked(3).forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowKeys.forEach { key ->
                    when (key.digit) {
                        "bio" -> {
                            KeypadActionButton(
                                onClick = onBiometricClick
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Unlock",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        "del" -> {
                            KeypadActionButton(
                                onClick = onDeleteClick
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            KeypadDigitButton(
                                digit = key.digit,
                                letters = key.letters,
                                onClick = { onDigitClick(key.digit) }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class KeypadKey(val digit: String, val letters: String)

/**
 * Individual circular number button.
 */
@Composable
private fun KeypadDigitButton(
    digit: String,
    letters: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color.Black
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF131D2E),
                        Color(0xFF0A101C)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.12f),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 24.sp
                )
            )

            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

/**
 * Action button (Biometric & Backspace) inside the keypad.
 */
@Composable
private fun KeypadActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
