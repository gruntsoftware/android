package com.brainwallet.game.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier


interface GameSlot {
    @Composable fun Render(modifier: Modifier,
                           visible: Boolean,
                           launchParams: String,
                           onExit: (String, ByteArray?) -> Unit)
}

val LocalGameSlot = staticCompositionLocalOf<GameSlot?> { null }
