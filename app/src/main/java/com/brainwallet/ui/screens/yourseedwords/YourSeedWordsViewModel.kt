package com.brainwallet.ui.screens.yourseedwords

import androidx.lifecycle.viewModelScope
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.EventBus
import kotlinx.coroutines.launch

class YourSeedWordsViewModel : BrainwalletViewModel<YourSeedWordsEvent>() {

    override fun onEvent(event: YourSeedWordsEvent) {
        when (event) {
            YourSeedWordsEvent.OnSavedItClick -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SAVED_PAPERKEY))
            }
        }
    }

    companion object {
        const val LEGACY_EFFECT_ON_SAVED_PAPERKEY = "onSavedPaperKey"
    }
}