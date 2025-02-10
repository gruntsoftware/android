package com.brainwallet.ui.screen.unlock

data class UnLockState(
    val pinDigits: List<Int> = List(6) { -1 },
    val biometricEnabled: Boolean = false,
)
//todo