package com.brainwallet.ui.screen.setpasscode

sealed class SetPasscodeReadyScreenEvent {

    object OnBackClick : SetPasscodeReadyScreenEvent()
    object OnReadyClick : SetPasscodeReadyScreenEvent()

}