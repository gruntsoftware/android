package com.brainwallet.ui.screens.welcome

data class WelcomeState(
    val bip39Words: List<String> = emptyList(),
    val suggestionsSeedWords: List<String> = emptyList(),
    val seedWords: Map<Int, String> = (0..11).associateWith { "" } //12 seed words
)

//fun WelcomeState.asPaperKey(): String {
//    return seedWords.values.joinToString(" ")
//}