package com.brainwallet.ui.screens.setpasscode

data class SetPasscodeState(
    val isConfirm: Boolean = false,
    val passcode: List<Int> = List(4) { -1 }, //create 4 digit
    val passcodeConfirm: List<Int> = List(4) { -1 }, //create 4 digit
    val isUpdatePin: Boolean = false,
)

fun SetPasscodeState.isMatchPasscode(): Boolean =
    passcode.isNotEmpty() && passcodeConfirm.isNotEmpty() && passcode == passcodeConfirm

fun SetPasscodeState.isPasscodeFilled(): Boolean = passcode.all { it > -1 } && isConfirm.not()
