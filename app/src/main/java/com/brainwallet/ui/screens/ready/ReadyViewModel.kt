package com.brainwallet.ui.screens.ready

import com.brainwallet.ui.BrainwalletViewModel

class ReadyViewModel : BrainwalletViewModel<ReadyEvent>() {

    override fun onEvent(event: ReadyEvent) {
        when (event) {
            is ReadyEvent.OnLoad -> Unit
        }
    }
}