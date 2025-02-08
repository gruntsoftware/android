package com.brainwallet.ui.screen.setpasscode

import com.brainwallet.ui.screen.yourseedproveit.YourSeedProveItEvent

sealed class SetPasscodeScreenEvent {

    data class OnLoad(
        val digits: List<Int>,
    ) : SetPasscodeScreenEvent()

    data class OnSetPasscode (
        val selectedDigits: List<Int>,
        val actualDigits: List<Int>,
    ) : SetPasscodeScreenEvent()

    object OnBackClick : SetPasscodeScreenEvent()
    object OnClear : SetPasscodeScreenEvent()
    object OnEnterPasscode : SetPasscodeScreenEvent()
}