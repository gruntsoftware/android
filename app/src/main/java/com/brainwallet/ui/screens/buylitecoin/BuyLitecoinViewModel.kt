package com.brainwallet.ui.screens.buylitecoin

import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        state.debounce(700)
            .distinctUntilChanged()
            .filter { it.isValid() }
            .onEach {
                try {
                    onLoading(true)

                    val buyQuoteResult = ltcRepository.fetchBuyQuote(
                        params = mapOf(
                            "currencyCode" to "ltc",
                            "baseCurrencyCode" to appSetting.value.currency.code,
                            "baseCurrencyAmount" to it.fiatAmount.toString(),
                        )
                    )

                    _state.update {
                        it.copy(
                            fiatAmount = buyQuoteResult.data.totalAmount,
                            ltcAmount = buyQuoteResult.data.quoteCurrencyAmount
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: BuyLitecoinEvent) {
        when (event) {
            is BuyLitecoinEvent.OnLoad -> viewModelScope.launch {
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

            is BuyLitecoinEvent.OnFiatAmountChange -> _state.getAndUpdate {
                val (_, min, max) = it.moonpayCurrencyLimit.data.baseCurrency
                val errorStringId = when {
                    event.fiatAmount < min -> R.string.buy_litecoin_fiat_amount_validation_min
                    event.fiatAmount > max -> R.string.buy_litecoin_fiat_amount_validation_max
                    else -> null
                }
                it.copy(
                    errorStringId = errorStringId,
                    fiatAmount = event.fiatAmount
                )
            }
        }
    }

}