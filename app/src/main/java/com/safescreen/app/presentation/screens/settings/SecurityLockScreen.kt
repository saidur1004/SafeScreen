package com.safescreen.app.presentation.screens.settings

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.setValue
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safescreen.app.presentation.components.CyberBackgroundWaves
import com.safescreen.app.presentation.theme.DeepBlack
import com.safescreen.app.presentation.theme.NeonCyan
import com.safescreen.app.presentation.theme.NeonGreen
import com.safescreen.app.presentation.theme.TextMuted
import com.safescreen.app.presentation.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * Security Lock Screen with Cyber-Dark PIN Dial Pad and Real BiometricPrompt.
 *
 * Implements Module 06 Security Hardening:
 * - Real OS-level BiometricPrompt (Fingerprint / Face Unlock) via AndroidX Biometric.
 * - Mandatory 2-step PIN Enrollment (ENTER_NEW_PIN -> CONFIRM_PIN) with zero default fallbacks.
 * - Failed-attempt tracking with 30-second lockout penalty after 5 failed attempts.
 * - Testing PIN leaks completely eliminated from UI.
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
    val fragmentActivity = context as? FragmentActivity
    val haptic = LocalHapticFeedback.current

    // Check Biometric hardware support on device
    val biometricManager = remember(context) { BiometricManager.from(context) }
    val isBiometricHardwareAvailable = remember(context) {
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    LaunchedEffect(isBiometricHardwareAvailable) {
        viewModel.setBiometricHardwareAvailable(isBiometricHardwareAvailable)
    }

    // Launch real OS-level BiometricPrompt
    val showBiometricPrompt: () -> Unit = remember(fragmentActivity, state.isBiometricAvailable, state.isLockedOut, mode) {
        {
            if (fragmentActivity != null &&
                state.isBiometricAvailable &&
                !state.isLockedOut &&
                mode != SecurityLockMode.ENROLL_PIN
            ) {
                val executor = ContextCompat.getMainExecutor(fragmentActivity)
                val prompt = BiometricPrompt(
                    fragmentActivity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onEvent(SecurityUiEvent.BiometricSuccess)
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                                errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                                errorCode != BiometricPrompt.ERROR_CANCELED
                            ) {
                                viewModel.onEvent(SecurityUiEvent.BiometricFailed(errString.toString()))
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.onEvent(SecurityUiEvent.BiometricFailed("Biometric credential not recognized"))
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("SafeScreen Security")
                    .setSubtitle(
                        when (mode) {
                            SecurityLockMode.SHIELD_DISARM -> "Verify biometric identity to disarm protection shield"
                            SecurityLockMode.SETTINGS -> "Verify biometric identity to access security settings"
                            else -> "Unlock SafeScreen using Biometrics"
                        }
                    )
                    .setNegativeButtonText("Use PIN")
                    .setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                    .build()

                prompt.authenticate(promptInfo)
            } else if (!state.isBiometricAvailable && mode != SecurityLockMode.ENROLL_PIN) {
                Toast.makeText(context, "Biometric unlock is not configured or unavailable on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-prompt biometrics once on cold open if configured and available
    var autoPromptTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(state.isBiometricAvailable, state.isLockedOut) {
        if (!autoPromptTriggered && state.isBiometricAvailable && !state.isLockedOut && mode != SecurityLockMode.ENROLL_PIN) {
            autoPromptTriggered = true
            delay(250)
            showBiometricPrompt()
        }
    }

    // Prevent security bypass on system back gesture or button
    BackHandler {
        when (mode) {
            SecurityLockMode.APP_UNLOCK -> {
                fragmentActivity?.finish()
            }
            SecurityLockMode.SHIELD_DISARM,
            SecurityLockMode.SETTINGS,
            SecurityLockMode.ENROLL_PIN -> {
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
                    showBiometricPrompt()
                }
                is SecurityUiEvent.BiometricSuccess -> {
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.BiometricFailed -> {
                    viewModel.onEvent(event)
                }
                is SecurityUiEvent.ForgotPin -> {
                    // Removed testing PIN leaks
                }
                is SecurityUiEvent.NavigateBack -> {
                    when (mode) {
                        SecurityLockMode.APP_UNLOCK -> fragmentActivity?.finish()
                        SecurityLockMode.SHIELD_DISARM,
                        SecurityLockMode.SETTINGS,
                        SecurityLockMode.ENROLL_PIN -> onNavigateBack()
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
        SecurityLockMode.ENROLL_PIN -> {
            if (state.enrollmentStage == PinEnrollmentStage.CONFIRM_PIN) {
                Pair(
                    "Confirm your PIN",
                    "Re-enter the 4-digit PIN to confirm and activate protection"
                )
            } else {
                Pair(
                    "Create a 4-Digit PIN",
                    "Enter a secure 4-digit PIN to safeguard SafeScreen"
                )
            }
        }
    }

    val badgeText = when (state.mode) {
        SecurityLockMode.APP_UNLOCK -> "SECURE"
        SecurityLockMode.SHIELD_DISARM -> "ARMED"
        SecurityLockMode.SETTINGS -> "CONFIG"
        SecurityLockMode.ENROLL_PIN -> "SETUP"
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
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        state.isLockedOut -> {
                            Text(
                                text = state.errorMessage ?: "Too many failed attempts. Locked for ${state.lockoutRemainingSeconds}s",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFF4D4D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        state.isError -> {
                            Text(
                                text = state.errorMessage ?: "Incorrect PIN. Please try again.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFFF4D4D),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        state.isSuccess -> {
                            Text(
                                text = if (state.mode == SecurityLockMode.ENROLL_PIN) "PIN Enrolled • Security Activated" else "PIN Verified • Access Granted",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                        state.mode == SecurityLockMode.ENROLL_PIN -> {
                            Text(
                                text = if (state.enrollmentStage == PinEnrollmentStage.ENTER_NEW_PIN) "Step 1 of 2: Set New PIN" else "Step 2 of 2: Confirm PIN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. Custom Glassmorphic Keypad (3x4 Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NumericDialPad(
                    onDigitClick = { digit -> onEvent(SecurityUiEvent.EnterDigit(digit)) },
                    onDeleteClick = { onEvent(SecurityUiEvent.DeleteDigit) },
                    onBiometricClick = { onEvent(SecurityUiEvent.TriggerBiometric) },
                    enabled = !state.isLockedOut && !state.isSuccess,
                    showBiometric = state.mode != SecurityLockMode.ENROLL_PIN
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Security Assurance Footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = NeonCyan.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.mode == SecurityLockMode.ENROLL_PIN) {
                            "SHA-256 On-Device Cryptographic Key"
                        } else {
                            "Zero-Knowledge On-Device Verification"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
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
    enabled: Boolean = true,
    showBiometric: Boolean = true,
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
                            if (showBiometric) {
                                KeypadActionButton(
                                    onClick = onBiometricClick,
                                    enabled = enabled
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = "Biometric Unlock",
                                        tint = if (enabled) NeonCyan else NeonCyan.copy(alpha = 0.3f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(72.dp))
                            }
                        }
                        "del" -> {
                            KeypadActionButton(
                                onClick = onDeleteClick,
                                enabled = enabled
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = if (enabled) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        else -> {
                            KeypadDigitButton(
                                digit = key.digit,
                                letters = key.letters,
                                onClick = { onDigitClick(key.digit) },
                                enabled = enabled
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = CircleShape,
                ambientColor = Color.Black
            )
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF131D2E).copy(alpha = if (enabled) 1f else 0.4f),
                        Color(0xFF0A101C).copy(alpha = if (enabled) 1f else 0.4f)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = if (enabled) 0.12f else 0.04f),
                CircleShape
            )
            .clickable(
                enabled = enabled,
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
                    color = if (enabled) TextPrimary else TextMuted.copy(alpha = 0.4f),
                    fontSize = 24.sp
                )
            )

            if (letters.isNotEmpty()) {
                Text(
                    text = letters,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (enabled) Color(0xFF64748B) else Color(0xFF64748B).copy(alpha = 0.3f),
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
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = if (enabled) 0.04f else 0.01f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.08f else 0.03f), CircleShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
