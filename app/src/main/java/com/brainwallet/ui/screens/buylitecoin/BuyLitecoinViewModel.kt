package com.brainwallet.ui.screens.buylitecoin

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BuyLitecoinViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<BuyLitecoinEvent>() {

    private val _state = MutableStateFlow(BuyLitecoinState())
    val state: StateFlow<BuyLitecoinState> = _state.asStateFlow()

    val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

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
                    onEvent(BuyLitecoinEvent.OnFiatAmountChange(it))
                }
        }

    }

    override fun onEvent(event: BuyLitecoinEvent) {
        when (event) {
            is BuyLitecoinEvent.OnLoad -> viewModelScope.launch {
                delay(500)
                _state.update { it.copy(address = BRSharedPrefs.getReceiveAddress(event.context)) }
                try {
                    onLoading(true)

                    _state.getAndUpdate {
                        val limitResult = ltcRepository.fetchLimits(
                            baseCurrencyCode = appSetting.value.currency.code
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

            is BuyLitecoinEvent.OnFiatAmountChange -> viewModelScope.launch {
                //do validation
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
                                "baseCurrencyCode" to appSetting.value.currency.code,
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