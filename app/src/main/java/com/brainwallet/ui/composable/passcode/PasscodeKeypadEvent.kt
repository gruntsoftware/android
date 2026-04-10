package com.brainwallet.ui.composable.passcode
sealed class PasscodeKeypadEvent {
    object OnDelete : PasscodeKeypadEvent()
    data class OnPressed(val digit: Int) : PasscodeKeypadEvent()
}
