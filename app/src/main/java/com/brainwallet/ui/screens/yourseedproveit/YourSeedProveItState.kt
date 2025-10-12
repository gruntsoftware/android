package com.brainwallet.ui.screens.yourseedproveit

data class YourSeedProveItState(
    val correctSeedWords: Map<Int, SeedWordItem> = emptyMap(),
    val shuffledSeedWords: List<Pair<Int, String>> = emptyList(),
    val orderCorrected: Boolean = false,
)

data class SeedWordItem(
    val expected: String,
    val actual: String = ""
)

fun YourSeedProveItState.isWordUsedCorrectly(currentIndex: Int, currentWord: String): Boolean {
    return correctSeedWords[currentIndex]?.let { seedWordItem ->
        seedWordItem.expected == currentWord && seedWordItem.actual == currentWord
    } ?: false
}
