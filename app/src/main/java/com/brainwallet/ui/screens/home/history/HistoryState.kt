package com.brainwallet.ui.screens.home.history

data class HistoryState(
    val biometricEnabled: Boolean = false,
    val iso: String = "USD",
    val formattedCurrency: String = "",
    val formattedVersion: String = ""
)
