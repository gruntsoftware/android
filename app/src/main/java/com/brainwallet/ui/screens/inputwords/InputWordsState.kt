package com.brainwallet.ui.screens.inputwords

import com.brainwallet.navigation.Route

data class InputWordsState(
    val source: Route.InputWords.Source? = null,
    val bip39Words: List<String> = emptyList(),
    val suggestionsSeedWords: List<String> = emptyList(),
    val seedWords: SeedWords = (0..11).associateWith { "" } //12 seed words
)

typealias SeedWords = Map<Int, String>

fun SeedWords.asPaperKey(): String {
    return values.joinToString(" ")
}

fun InputWordsState.isFrom(from: Route.InputWords.Source): Boolean {
    return source == from
}