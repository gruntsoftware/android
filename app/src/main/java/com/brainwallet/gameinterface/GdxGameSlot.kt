package com.brainwallet.gameinterface

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
class GdxGameSlot : GameSlot {
    @Composable override fun Render(
        modifier: Modifier,
        visible: Boolean,
        launchParams: String,
        onExit: (String, ByteArray?) -> Unit
    ) {
        GdxGameView(visible, launchParams, onExit, modifier)
    }
}
