package com.brainwallet.ui.screens.buyreceive

import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BuyReceiveViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<BuyReceiveEvent>() {

    private val _state = MutableStateFlow(BuyReceiveState())
    val state: StateFlow<BuyReceiveState> = _state.asStateFlow()
    val appSetting: StateFlow<AppSetting> = settingRepository.currentSettings

    init {
        viewModelScope.launch {
            state.map { it.fiatAmount }
                .debounce(1000)
                .distinctUntilChanged()
                .filter {
                    val (_, min, max) = state.value.moonpayCurrencyLimit.data.baseCurrency
                    it in min..max
                }
                .collect {
                    onEvent(BuyReceiveEvent.OnFiatAmountChange(it))
                }
        }
    }

    override fun onEvent(event: BuyReceiveEvent) {
        when (event) {
            is BuyReceiveEvent.OnLoad -> viewModelScope.launch {
                delay(500)
                _state.update { it.copy(address = BRSharedPrefs.getReceiveAddress(event.context)) }
                try {
                    onLoading(true)

                    _state.getAndUpdate {
                        val limitResult = ltcRepository.fetchLimits(
                            baseCurrencyCode = settingRepository.currentSettings.value.currency.code
                        )

                        it.copy(
                            moonpayCurrencyLimit = limitResult,
                            fiatAmount = limitResult.data.baseCurrency.min,
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }

            is BuyReceiveEvent.OnFiatAmountChange -> viewModelScope.launch {
                // do validation
                val (_, min, max) = state.value.moonpayCurrencyLimit.data.baseCurrency
                val errorStringId = when {
                    event.fiatAmount < min -> R.string.buy_litecoin_fiat_amount_validation_min
                    event.fiatAmount > max -> R.string.buy_litecoin_fiat_amount_validation_max
                    else -> null
                }
                _state.update {
                    it.copy(
                        errorFiatAmountStringId = errorStringId,
                        fiatAmount = event.fiatAmount
                    )
                }

                if (event.needFetch.not()) {
                    return@launch
                }

                try {
                    onLoading(true)

                    _state.update {
                        val result = ltcRepository.fetchBuyQuote(
                            mapOf(
                                "currencyCode" to "ltc",
                                "baseCurrencyCode" to settingRepository.currentSettings.value.currency.code,
                                "baseCurrencyAmount" to event.fiatAmount.toString(),
                            )
                        )

                        it.copy(
                            ltcAmount = result.data.quoteCurrencyAmount,
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }
        }
    }
}
