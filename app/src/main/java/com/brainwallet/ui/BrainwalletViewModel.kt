package com.brainwallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainwallet.di.json
import com.brainwallet.navigation.UiEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

/**
 * describe [BrainwalletViewModel] here
 */
abstract class BrainwalletViewModel<Event> : ViewModel() {

    private val _uiEffect = Channel<UiEffect>()
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    fun sendUiEffect(effect: UiEffect) {
        viewModelScope.launch {
            _uiEffect.send(effect)
        }
    }

    abstract fun onEvent(event: Event)

    protected fun handleError(t: Throwable) {
        val errorMessage = t.message ?: "Oops, something went wrong"
        //todo more error handler

        if (t is retrofit2.HttpException) {
            val message = t.response()?.errorBody()?.string()?.let {
                runCatching {
                    json.decodeFromString<JsonObject>(it)["message"]?.toString()
                }.getOrNull()
            } ?: "Oops, something went wrong"

            sendUiEffect(
                UiEffect.ShowMessage(
                    type = UiEffect.ShowMessage.Type.Error,
                    message = message
                )
            )
            return
        }

        sendUiEffect(
            UiEffect.ShowMessage(
                type = UiEffect.ShowMessage.Type.Error,
                message = errorMessage
            )
        )
    }

    protected fun onLoading(
        visible: Boolean,
        message: String = "msg_please_wait"
    ) = _loadingState.getAndUpdate { it.copy(visible = visible, message = message) }
}

data class LoadingState(
    val visible: Boolean = false,
    val message: String = "msg_please_wait"
)