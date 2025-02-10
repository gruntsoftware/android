package com.brainwallet.ui.screen.yourseedproveit

sealed class YourSeedProveItEvent {
    data class OnLoad(
        val seedWords: List<String>
    ) : YourSeedProveItEvent()

    data class OnDropSeedWordItem(
        val expectedWord: String,
        val actualWord: String
    ) : YourSeedProveItEvent()

    object OnBackClick : YourSeedProveItEvent()

    object OnGameAndSync : YourSeedProveItEvent()

    object OnClear : YourSeedProveItEvent()
}