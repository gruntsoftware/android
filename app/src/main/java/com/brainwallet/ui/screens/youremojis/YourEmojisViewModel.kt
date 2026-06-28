package com.brainwallet.ui.screens.youremojis

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class YourEmojisViewModel(
    private val app: Application,
) : BrainwalletViewModel<YourEmojisEvent>() {
    private val address = BRSharedPrefs.getReceiveAddress(app)
    private val timestamp = java.util.Date().time
    private val emojis = runCatching { BRKeyStore.getEmojis(app, 0) }
        .getOrNull()?.decodeToString() ?: "NO_EMOJIS"
    private val launchParams = JSONObject(
        """{"launchParameters":{"address":"$address", "timestamp":$timestamp,"emojis": "$emojis" }}"""
    )

    override fun onEvent(event: YourEmojisEvent) {
        when (event) {
            is YourEmojisEvent.OnSavedItClick -> viewModelScope.launch {
            }
        }
    }
}
