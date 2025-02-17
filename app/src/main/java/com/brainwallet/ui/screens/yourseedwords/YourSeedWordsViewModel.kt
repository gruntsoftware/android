package com.brainwallet.ui.screens.yourseedwords

import com.brainwallet.ui.BrainwalletViewModel

class YourSeedWordsViewModel : BrainwalletViewModel<YourSeedWordsEvent>() {

    override fun onEvent(event: YourSeedWordsEvent) {
        when (event) {
            YourSeedWordsEvent.OnSavedItClick -> {
                //todo
            }
        }
    }
}