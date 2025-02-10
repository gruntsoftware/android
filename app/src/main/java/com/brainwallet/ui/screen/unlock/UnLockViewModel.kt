package com.brainwallet.ui.screen.unlock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UnLockViewModel : ViewModel() {

    private val _state = MutableStateFlow(UnLockState())
    val state: StateFlow<UnLockState> = _state.asStateFlow()

    fun onEvent(event: UnLockEvent) {
        when (event) {
            is UnLockEvent.OnPinDigitChange -> _state.update {
                val pinDigits = it.pinDigits.toMutableList()
                if (event.digit < -1) {
                    return
                }

                val index = pinDigits.indexOfFirst { it == -1 }
                if (index == -1) {
                    return
                }
                pinDigits[index] = event.digit
                it.copy(pinDigits = pinDigits)
            }

            UnLockEvent.OnDeletePinDigit -> _state.update {
                val pinDigits = it.pinDigits.toMutableList()
                val lastNonMinusOneIndex = pinDigits.indexOfLast { digit -> digit != -1 }
                if (lastNonMinusOneIndex != -1) {
                    pinDigits[lastNonMinusOneIndex] = -1
                    it.copy(pinDigits = pinDigits)
                } else {
                    it
                }
            }

            UnLockEvent.OnBackClick -> Unit
        }
    }
}