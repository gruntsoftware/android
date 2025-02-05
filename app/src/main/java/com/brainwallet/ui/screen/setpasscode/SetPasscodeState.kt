package com.brainwallet.ui.screen.setpasscode

data class SetPasscodeState(
    val didSetPasscode: Boolean = false,
    val didSelectBiometrics: Boolean = false,
    val passcode: List<Int> = emptyList(),
)
