package com.brainwallet.ui.screen.setpasscode

import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SetPasscodeMainViewModel : ViewModel() {

    private val _state = MutableStateFlow(SetPasscodeMainState())
    val state: StateFlow<SetPasscodeMainState> = _state.asStateFlow()


    fun onEvent(event: SetPasscodeMainEvent) {
        when (event) {
            is SetPasscodeMainEvent.OnNavigateStepTo -> _state.update {
                it.copy(setPasscodeScreenStep = event.step)
            }
            else -> Unit
        }
//
//        when (event) {
//            is SetPasscodeMainEvent.OnEnterPasscode -> _state.update {
//
//            }
//        }
    }
}