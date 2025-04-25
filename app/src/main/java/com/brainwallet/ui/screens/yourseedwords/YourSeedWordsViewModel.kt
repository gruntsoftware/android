package com.brainwallet.ui.screens.yourseedwords

import androidx.lifecycle.viewModelScope
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.launch

class YourSeedWordsViewModel : BrainwalletViewModel<YourSeedWordsEvent>() {

    override fun onEvent(event: YourSeedWordsEvent) {
        when (event) {
            is YourSeedWordsEvent.OnSavedItClick -> viewModelScope.launch {
                sendUiEffect(UiEffect.Navigate(destinationRoute = Route.YourSeedProveIt(event.seedWords)))
            }
        }
    }
}