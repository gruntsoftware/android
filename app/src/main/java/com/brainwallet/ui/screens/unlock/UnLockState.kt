package com.brainwallet.ui.screens.unlock

data class UnLockState(
    val passcode: List<Int> = List(4) { -1 }, //4 digit passcode/pin
    val biometricEnabled: Boolean = false,
    val iso: String = "USD",
    val formattedCurrency: String = "",
    val isUpdatePin: Boolean = false,
)

fun UnLockState.isPasscodeFilled(): Boolean = passcode.all { it > -1 }

//todo
