package com.brainwallet.ui.screens.reenterpin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.ui.composable.PinDot
import com.brainwallet.ui.composable.PinKeyboard
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun ReEnterPinScreen(
    state: ReEnterPinState,
    onIntent: (ReEnterPinIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeAnimation = remember { Animatable(0f) }

    // Handle validation error animation
    LaunchedEffect(state.showValidationError) {
        if (state.showValidationError) {
            // Shake animation for error
            repeat(3) {
                shakeAnimation.animateTo(
                    targetValue = 10f,
                    animationSpec = tween(50)
                )
                shakeAnimation.animateTo(
                    targetValue = -10f,
                    animationSpec = tween(50)
                )
            }
            shakeAnimation.animateTo(
                targetValue = 0f,
                animationSpec = tween(50)
            )
            onIntent(ReEnterPinIntent.ClearError)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrainwalletTheme.colors.surface)
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = "Re-enter PIN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = BrainwalletTheme.colors.content,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Description
        Text(
            text = "Please re-enter your PIN to confirm",
            fontSize = 16.sp,
            color = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                fontSize = 14.sp,
                color = BrainwalletTheme.colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // PIN input component with shake animation
        ReEnterPinInputComponent(
            currentPinLength = state.currentPin.length,
            onDigitClick = { digit ->
                onIntent(ReEnterPinIntent.DigitPressed(digit))
            },
            onDeleteClick = {
                onIntent(ReEnterPinIntent.DeletePressed)
            },
            modifier = Modifier
                .offset(x = shakeAnimation.value.dp)
                .graphicsLayer {
                    if (state.showValidationError) {
                        alpha = 0.7f
                    }
                }
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ReEnterPinInputComponent(
    currentPinLength: Int,
    onDigitClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 4
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // PIN dots display
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(maxLength) { index ->
                PinDot(
                    isFilled = index < currentPinLength
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN keyboard
        PinKeyboard(
            onDigitClick = onDigitClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@PreviewLightDark
@Composable
fun ReEnterPinScreenEmptyPreview() {
    BrainwalletTheme(darkTheme = isSystemInDarkTheme()) {
        ReEnterPinScreen(
            state = ReEnterPinState(
                originalPin = "1234",
                currentPin = "",
                error = null,
                showValidationError = false
            ),
            onIntent = { }
        )
    }
}

@PreviewLightDark
@Composable
fun ReEnterPinScreenPartialPreview() {
    BrainwalletTheme(darkTheme = isSystemInDarkTheme()) {
        ReEnterPinScreen(
            state = ReEnterPinState(
                originalPin = "1234",
                currentPin = "12",
                error = null,
                showValidationError = false
            ),
            onIntent = { }
        )
    }
}

@PreviewLightDark
@Composable
fun ReEnterPinScreenErrorPreview() {
    BrainwalletTheme(darkTheme = isSystemInDarkTheme()) {
        ReEnterPinScreen(
            state = ReEnterPinState(
                originalPin = "1234",
                currentPin = "",
                error = "PINs do not match. Please try again.",
                showValidationError = true
            ),
            onIntent = { }
        )
    }
}
