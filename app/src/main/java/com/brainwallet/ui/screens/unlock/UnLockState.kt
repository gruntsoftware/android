package com.brainwallet.ui.screens.unlock

data class UnLockState(
    val pinDigits: List<Int> = List(6) { -1 },
    val biometricEnabled: Boolean = false,
    val iso: String = "USD",
    val formattedCurrency: String = ""
)
//todo
