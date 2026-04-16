package com.brainwallet.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class PinInputState(private val maxLength: Int = 4) {
    var pin by mutableStateOf("")
        private set

    val pinLength: Int get() = pin.length
    val isComplete: Boolean get() = pin.length == maxLength
    val isEmpty: Boolean get() = pin.isEmpty()

    fun addDigit(digit: Int) {
        if (pin.length < maxLength && digit in 0..9) {
            pin += digit.toString()
        }
    }

    fun deleteDigit() {
        if (pin.isNotEmpty()) {
            pin = pin.dropLast(1)
        }
    }

    fun clear() {
        pin = ""
    }

    fun getPinAsString(): String = pin
}

@Composable
fun rememberPinInputState(maxLength: Int = 4): PinInputState {
    return remember { PinInputState(maxLength) }
}
