package com.brainwallet.ui.bentosections.gamehubbento
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.Koin
import org.koin.core.context.GlobalContext.getOrNull
import org.koin.core.parameter.parametersOf

@KoinViewModel
class GameHubBentoViewModel(
    private val settingRepository: SettingRepository,
) : BrainwalletViewModel<GameHubBentoEvent>() {

    private val _state = MutableStateFlow(GameHubBentoState())
    val state: StateFlow<GameHubBentoState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
        }
    }

    fun loadRemoteConfig(): String? {
        val remoteConfigSource: Koin = getOrNull() ?: return null
        return remoteConfigSource.getOrNull { parametersOf(RemoteConfigSource::class) }
        // getOrNull(RemoteConfigSource.KEY_FEATURE_GAMEHUB_CONTENT)
    }
    override fun onEvent(event: GameHubBentoEvent) {
        when (event) {
            is GameHubBentoEvent.OnLoad -> {
                _state.update { currentState ->
                    currentState.copy()
                }
            }
        }
    }
}
