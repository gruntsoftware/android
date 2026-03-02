package com.brainwallet.ui.screens.send

data class SendState(
    val biometricEnabled: Boolean = false,
    val iso: String = "USD",
    val formattedCurrency: String = "",
    val formattedVersion: String = ""
)
