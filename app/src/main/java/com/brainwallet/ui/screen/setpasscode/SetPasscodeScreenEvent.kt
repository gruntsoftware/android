package com.brainwallet.ui.screen.setpasscode

sealed class SetPasscodeScreenEvent {

    data class OnSetDigit(
        val digit1: Int,
        val digit2: Int,
        val digit3: Int,
        val digit4: Int,
        val digit5: Int,
        val digit6: Int,
    ) : SetPasscodeScreenEvent()

    object OnSetBiometrics : SetPasscodeScreenEvent()

    object OnBackClick : SetPasscodeScreenEvent()

    object OnClear : SetPasscodeScreenEvent()

    object OnSetPasscode : SetPasscodeScreenEvent()

}