package com.brainwallet.ui.screens.home.history

import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HistoryViewModel(
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<HistoryEvent>() {

    private val _state =
        MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    override fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.OnLoad -> {
            }
        }
    }
}
