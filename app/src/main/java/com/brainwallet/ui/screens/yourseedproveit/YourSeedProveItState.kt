package com.brainwallet.ui.screens.yourseedproveit

data class YourSeedProveItState(
    val correctSeedWords: Map<Int, SeedWordItem> = emptyMap(),
    val shuffledSeedWords: List<String> = emptyList(),
    val orderCorrected: Boolean = false,
)

data class SeedWordItem(
    val expected: String,
    val actual: String = ""
)