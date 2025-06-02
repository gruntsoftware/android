package com.brainwallet.ui.screens.home.receive

import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.isCustom
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

//todo: wip
class ReceiveDialogViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
) : BrainwalletViewModel<ReceiveDialogEvent>() {

    private val _state = MutableStateFlow(ReceiveDialogState())
    val state: StateFlow<ReceiveDialogState> = _state.asStateFlow()

    val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(setting.currency))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onEvent(event: ReceiveDialogEvent) {
        when (event) {
            is ReceiveDialogEvent.OnLoad -> _state.update {
                val address = BRSharedPrefs.getReceiveAddress(event.context)
                it.copy(
                    address = address,
                    qrBitmap = QRUtils.generateQR(event.context, "litecoin:${address}"),
                    fiatCurrencies = CurrencyDataSource.getInstance(event.context).getCurrenciesForBuy(),
                )
            }

            is ReceiveDialogEvent.OnCopyClick -> BRClipboardManager.putClipboard(
                event.context,
                state.value.address
            )

            is ReceiveDialogEvent.OnFiatAmountChange -> viewModelScope.launch {
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
                                "baseCurrencyCode" to it.selectedFiatCurrency.code,
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

            is ReceiveDialogEvent.OnFiatCurrencyChange -> viewModelScope.launch {
                try {
                    onLoading(true)
                    val currencyLimit = ltcRepository.fetchLimits(event.fiatCurrency.code)

                    _state.updateAndGet {
                        it.copy(
                            selectedFiatCurrency = event.fiatCurrency,
                            moonpayCurrencyLimit = currencyLimit,
                            selectedQuickFiatAmountOptionIndex = 1, //default to 10X
                            fiatAmount = it.getDefaultFiatAmount(),
                        )
                    }.let {
                        onEvent(
                            ReceiveDialogEvent.OnFiatAmountOptionIndexChange(
                                index = it.selectedQuickFiatAmountOptionIndex,
                                quickFiatAmountOption = it.getQuickFiatAmountOptions()[it.selectedQuickFiatAmountOptionIndex]
                            )
                        )
                    }

                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }

            is ReceiveDialogEvent.OnFiatAmountOptionIndexChange -> _state.updateAndGet {
                it.copy(
                    selectedQuickFiatAmountOptionIndex = event.index,
                    fiatAmount = if (event.quickFiatAmountOption.isCustom()) it.fiatAmount
                    else event.quickFiatAmountOption.value
                )
            }.let {
                if (event.quickFiatAmountOption.isCustom().not()) {
                    onEvent(ReceiveDialogEvent.OnFiatAmountChange(it.fiatAmount))
                }
            }

            ReceiveDialogEvent.OnMoonpayButtonClick -> viewModelScope.launch {
                try {
                    onLoading(true)

                    val currentState = state.value
                    val agentString = Utils.getAgentString(event.context, "android/HttpURLConnection")
                    val signedUrl = ltcRepository.fetchMoonpaySignedUrl(
                        mapOf(
                            "baseCurrencyCode" to currentState.selectedFiatCurrency.code,
                            "baseCurrencyAmount" to currentState.fiatAmount.toString(),
                            "language" to appSetting.value.languageCode,
                            "walletAddress" to currentState.address,
                            "defaultCurrencyCode" to "ltc",
                            "externalTransactionId" to currentState.externalTransactionId,
                            "currencyCode" to "ltc",
                            "themeId" to "main-v1.0.0",
                            "theme" to if (appSetting.value.isDarkMode) "dark" else "light"
                        )
                    )

                    _state.update { it.copy(moonpayBuySignedUrl = signedUrl) }

                } catch (e: Exception) {
                    handleError(e)
                } finally {

                    onLoading(false)

                }

            }

            ReceiveDialogEvent.OnSignedUrlClear -> _state.update { it.copy(moonpayBuySignedUrl = null) }
        }
    }
}