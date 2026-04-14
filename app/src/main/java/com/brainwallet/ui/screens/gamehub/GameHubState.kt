package com.brainwallet.ui.screens.gamehub

data class GameHubState(
    val biometricEnabled: Boolean = false,
    val iso: String = "USD",
    val formattedCurrency: String = "",
    val formattedVersion: String = ""
)
