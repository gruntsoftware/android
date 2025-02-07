package com.brainwallet.ui.screen.inputwords

data class InputWordsState(
    val filterredBip39Words: List<String> = emptyList(),
    val seedWords: Map<Int, String> = (0..11).associateWith { "" } //12 seed words
)