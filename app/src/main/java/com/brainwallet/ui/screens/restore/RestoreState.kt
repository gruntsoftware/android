package com.brainwallet.ui.screens.restore

import com.brainwallet.navigation.Route

data class RestoreState(
    val source: Route.Restore.Source? = null,
    val bip39Words: List<String> = emptyList(),
    val suggestionsSeedWords: List<String> = emptyList(),
    val seedWords: SeedWords = (0..11).associateWith { "" } // 12 seed words
)

typealias SeedWords = Map<Int, String>

fun SeedWords.asPaperKey(): String {
    return values.joinToString(" ")
}

fun RestoreState.isFrom(from: Route.Restore.Source): Boolean {
    return source == from
}

fun RestoreState.isFromWelcome(): Boolean = source == null
