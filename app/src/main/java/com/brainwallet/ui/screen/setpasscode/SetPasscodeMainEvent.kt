package com.brainwallet.ui.screen.setpasscode

sealed class SetPasscodeMainEvent {

    data class OnNavigateStepTo(val step: ScreenStep) : SetPasscodeMainEvent()
    data class OnLoad(
        val digits: List<Int>,
    ) : SetPasscodeMainEvent()

    object OnBackClick : SetPasscodeMainEvent()
    object OnEnterPasscode : SetPasscodeMainEvent()
}