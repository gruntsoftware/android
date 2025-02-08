package com.brainwallet.ui.screen.setpasscode

data class SetPasscodeMainState(
    val didSetPasscode: Boolean = false,
    val didSelectBiometrics: Boolean = false,
    val digits: List<Int> = emptyList(),
    val setPasscodeScreenStep: ScreenStep = ScreenStep.ReadyScreen
)

fun SetPasscodeMainState.isScrenStep(screenStep: ScreenStep): Boolean =
    setPasscodeScreenStep == screenStep


enum class ScreenStep {
    ReadyScreen,
    SetPasscodeScreen,
    ConfirmScreen
}
