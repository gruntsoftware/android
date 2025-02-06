package com.brainwallet.ui.screen.setpasscode

sealed class SetPasscodeConfirmScreenEvent {

    data class OnLoad(
        val digits: List<Int>,
    ) : SetPasscodeConfirmScreenEvent()


    object OnBackClick : SetPasscodeConfirmScreenEvent()
    object OnConfirmClick : SetPasscodeConfirmScreenEvent()

}