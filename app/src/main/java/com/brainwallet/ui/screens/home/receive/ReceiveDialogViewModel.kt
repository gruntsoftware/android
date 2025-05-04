package com.brainwallet.ui.screens.home.receive

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
            _state.update { it.copy(selectedFiatCurrency = setting.currency) }

            onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(setting.currency))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onEvent(event: ReceiveDialogEvent) {
        when (event) {
            is ReceiveDialogEvent.OnLoad -> viewModelScope.launch {
                try {
                    onLoading(true)

                    _state.update {
                        val address = BRSharedPrefs.getReceiveAddress(event.context)
                        it.copy(
                            address = address,
                            qrBitmap = QRUtils.generateQR(event.context, "litecoin:${address}"),
                            fiatCurrencies = CurrencyDataSource.getInstance(event.context)
                                .getAllCurrencies(true),
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }

            }

            is ReceiveDialogEvent.OnCopyClick -> BRClipboardManager.putClipboard(
                event.context,
                state.value.address
            )

            is ReceiveDialogEvent.OnFiatAmountChange -> _state.update {
                it.copy(
                    fiatAmount = event.amount,
                    ltcAmount = event.amount / it.selectedFiatCurrency.rate,
                )
            }

            is ReceiveDialogEvent.OnFiatCurrencyChange -> viewModelScope.launch {
                try {
                    onLoading(true)
                    val currencyLimit = ltcRepository.fetchLimits(event.fiatCurrency.code)

                    _state.update {
                        it.copy(
                            selectedFiatCurrency = event.fiatCurrency,
                            moonpayCurrencyLimit = currencyLimit,
                            fiatAmount = currencyLimit.data.baseCurrency.min,
                            ltcAmount = currencyLimit.data.baseCurrency.min / event.fiatCurrency.rate,
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