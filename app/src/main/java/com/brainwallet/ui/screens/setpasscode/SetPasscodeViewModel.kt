package com.brainwallet.ui.screens.setpasscode

import android.util.Log
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet

class SetPasscodeViewModel : BrainwalletViewModel<SetPasscodeEvent>() {

    private val _state = MutableStateFlow(SetPasscodeState())
    val state: StateFlow<SetPasscodeState> = _state.asStateFlow()

    override fun onEvent(event: SetPasscodeEvent) {
        when (event) {
            SetPasscodeEvent.OnDeleteDigit -> _state.update { currentState ->
                val passcodeTemp =
                    if (currentState.isConfirm) currentState.passcodeConfirm else currentState.passcode
                val lastNonMinusOneIndex = passcodeTemp.indexOfLast { it != -1 }
                if (lastNonMinusOneIndex == -1) {
                    currentState
                } else {
                    if (currentState.isConfirm) {
                        currentState.copy(
                            passcodeConfirm = passcodeTemp.toMutableList().apply {
                                set(lastNonMinusOneIndex, -1)
                            }
                        )
                    } else {
                        currentState.copy(
                            passcode = passcodeTemp.toMutableList().apply {
                                set(lastNonMinusOneIndex, -1)
                            }
                        )
                    }
                }
            }

            is SetPasscodeEvent.OnDigitChange -> _state.updateAndGet { currentState ->
                if (event.digit < -1) return@updateAndGet currentState
                val passcodeTemp =
                    if (currentState.isConfirm) currentState.passcodeConfirm else currentState.passcode
                val index = passcodeTemp.indexOfFirst { it == -1 }
                if (index == -1) return@updateAndGet currentState
                if (currentState.isConfirm) {
                    currentState.copy(
                        passcodeConfirm = passcodeTemp.toMutableList().apply {
                            set(index, event.digit)
                        }
                    )
                } else {
                    currentState.copy(
                        passcode = passcodeTemp.toMutableList().apply {
                            set(index, event.digit)
                        }
                    )
                }
            }.also {
                //if passcode all filled, then goto confirm
                if (it.isPasscodeFilled()) {
                    sendUiEffect(
                        UiEffect.Navigate(
                            destinationRoute = Route.SetPasscode(passcode = it.passcode)
                        )
                    )
                }

                //if passcode confirm all filled, then goto your seed words
                if (it.isMatchPasscode()) {
                    //seedwords
                    sendUiEffect(
                        UiEffect.Navigate(
                            destinationRoute = Route.YourSeedWords(
                                seedWords = listOf(
                                    "one",
                                    "two",
                                    "three",
                                    "four",
                                    "five",
                                    "six",
                                    "seventh",
                                    "eight",
                                    "nine",
                                    "ten",
                                    "eleven",
                                    "twelve"
                                )
                            )
                        )
                    )
                }
            }

            is SetPasscodeEvent.OnLoad -> {
                if (event.passcode.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            isConfirm = true,
                            passcode = event.passcode
                        )
                    }
                }
            }
        }
    }

}