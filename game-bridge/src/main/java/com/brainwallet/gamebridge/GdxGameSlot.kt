package com.brainwallet.gamebridge

import androidx.compose.runtime.Composable
import com.brainwallet.game.contract.GameSlot
import androidx.compose.ui.Modifier
class GdxGameSlot : GameSlot {
    @Composable override fun Render(modifier: Modifier,
                                    visible: Boolean,
                                    onExit: () -> Unit) {
        GdxGameView(modifier, visible, onExit)
    }
}
