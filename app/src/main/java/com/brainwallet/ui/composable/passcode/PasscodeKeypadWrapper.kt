package com.brainwallet.ui.composable.passcode
import androidx.compose.runtime.Composable

@Composable
fun PasscodeKeypadWrapper(
    onDelete: () -> Unit,
    onDigitPressed: (digit: Int, isValidPin: (String) -> Boolean) -> Unit,
    onValidatePin: (String) -> Boolean,
) {
    PasscodeKeypad { passcodeKeypadEvent ->
        when (passcodeKeypadEvent) {
            PasscodeKeypadEvent.OnDelete -> onDelete()
            is PasscodeKeypadEvent.OnPressed -> onDigitPressed(
                passcodeKeypadEvent.digit,
                onValidatePin,
            )
        }
    }
}
