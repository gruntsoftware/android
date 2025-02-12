package com.brainwallet.ui.screens.yourseedproveit

data class YourSeedProveItState(
    val correctSeedWords: Map<String, String> = mapOf(),
    val shuffledSeedWords: List<String> = emptyList(),
    val orderCorrected: Boolean = false,
)
