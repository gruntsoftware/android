package com.brainwallet.ui.screens.setpasscode

sealed class SetPasscodeEvent {
    data class OnLoad(val passcode: List<Int> = emptyList()) : SetPasscodeEvent()
    data class OnDigitChange(val digit: Int) : SetPasscodeEvent()
    object OnDeleteDigit : SetPasscodeEvent()
}