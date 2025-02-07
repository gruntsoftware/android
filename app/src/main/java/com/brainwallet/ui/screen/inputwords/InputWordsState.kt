package com.brainwallet.ui.screen.inputwords

data class InputWordsState(
    val bip39Words: List<String> = emptyList(),
    val suggestionsSeedWords: List<String> = emptyList(),
    val seedWords: Map<Int, String> = (0..11).associateWith { "" } //12 seed words
)

fun InputWordsState.asPaperKey(): String {
    return seedWords.values.joinToString(" ")
}