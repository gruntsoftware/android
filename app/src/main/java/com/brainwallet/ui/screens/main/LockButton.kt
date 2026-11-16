package com.brainwallet.ui.screens.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.R
import com.brainwallet.constants.topNavButtonSize
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.midnight
import kotlinx.coroutines.delay

/** Wait after the tap before the icon starts turning into the lock. */
const val LOCK_BUTTON_DELAY_MS = 400L

/** How long the unlock -> lock crossfade takes. The wallet locks once it finishes. */
const val LOCK_BUTTON_ANIMATION_MS = 300

/**
 * Top nav button that locks the wallet. Shows the open lock until tapped; 400ms after the tap it
 * crossfades to the closed lock, then calls [onLock], which closes the main screen.
 */
@Composable
fun LockButton(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    onLock: () -> Unit
) {
    // Tapped: waiting the 400ms. Locked: showing the closed lock.
    var isTapped by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }

    LaunchedEffect(isTapped) {
        if (!isTapped) return@LaunchedEffect
        delay(LOCK_BUTTON_DELAY_MS)
        isLocked = true
        delay(LOCK_BUTTON_ANIMATION_MS.toLong())
        onLock()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(topNavButtonSize)
            .clip(CircleShape)
            // One tap only: further taps while it animates would lock twice.
            .clickable(enabled = !isTapped) { isTapped = true }
    ) {
        Crossfade(
            targetState = isLocked,
            animationSpec = tween(LOCK_BUTTON_ANIMATION_MS),
            label = "lockIcon"
        ) { locked ->
            Icon(
                imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = stringResource(
                    if (locked) R.string.settings_title_unlock else R.string.settings_title_lock
                ),
                tint = if (isDarkMode) Color.White else midnight
            )
        }
    }
}

@Composable
@PreviewLightDark
fun LockButtonPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        LockButton {}
    }
}
