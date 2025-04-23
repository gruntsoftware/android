package com.brainwallet.ui.screens.yourseedproveit

sealed class YourSeedProveItEvent {
    data class OnLoad(
        val seedWords: List<String>
    ) : YourSeedProveItEvent()

    data class OnDropSeedWordItem(
        val index: Int,
        val expectedWord: String,
        val actualWord: String
    ) : YourSeedProveItEvent()

    object OnGameAndSync : YourSeedProveItEvent()

    object OnClear : YourSeedProveItEvent()
}