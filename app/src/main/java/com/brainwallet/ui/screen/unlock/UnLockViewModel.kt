package com.brainwallet.ui.screen.unlock

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UnLockViewModel : ViewModel() {

    private val _state = MutableStateFlow(UnLockState())
    val state: StateFlow<UnLockState> = _state.asStateFlow()

    fun onEvent(event: UnLockEvent) {
        //todo
    }
}