package com.brainwallet.ui.screen.setpasscode

enum class ScreenStep {
    ReadyScreen,
    SetPasscodeScreen,
    ConfirmScreen
}

data class SetPasscodeMainState(
    val didSetPasscode: Boolean = false,
    val didSelectBiometrics: Boolean = false,
    val digits: List<Int> = emptyList(),
    val setPasscodeScreenStep: ScreenStep = ScreenStep.ReadyScreen
)

fun SetPasscodeMainState.isScreenStep(screenStep: ScreenStep): Boolean =
    setPasscodeScreenStep == screenStep

