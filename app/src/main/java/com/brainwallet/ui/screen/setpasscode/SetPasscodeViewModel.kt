package com.brainwallet.ui.screen.setpasscode

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SetPasscodeViewModel : ViewModel() {

    private val _state = MutableStateFlow(SetPasscodeState())
    val state: StateFlow<SetPasscodeState> = _state.asStateFlow()

    fun onEvent(event: SetPasscodeScreenEvent) {
        when (event) {

            is SetPasscodeScreenEvent.OnLoad -> _state.update {
                it.copy(
                    digits = event.digits
                )
            }

            is SetPasscodeScreenEvent.OnSetPasscode -> _state.update {
               val preferredPasscode = it.digits.apply {
                    this[event] = event
                }
                        it.copy( digits = preferredPasscode)
            }
            is SetPasscodeScreenEvent.OnClear -> _state.update {
                it.copy(
                    digits = emptyList()
                )
            }

            else -> Unit
        }
    }

    fun onEvent(event: SetPasscodeConfirmScreenEvent) {
        when (event) {

            is SetPasscodeConfirmScreenEvent.OnLoad -> _state.update {
                it.copy(
                    digits = event.digits
                )
            }

//            is SetPasscodeConfirmScreenEvent.OnConfirmClick -> _state.update {
//                val preferredPasscode = it.digits.apply {
//                    this[event] = event.
//                }
//                it.copy( digits = preferredPasscode)
//            }

            else -> Unit
        }
    }


}