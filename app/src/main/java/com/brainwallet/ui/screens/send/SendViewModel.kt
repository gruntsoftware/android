package com.brainwallet.ui.screens.send

import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.CurrencyDataGetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal

@KoinViewModel
class SendViewModel(
    private val ltcRepository: LtcRepository,
    private val currencyDataGetter: CurrencyDataGetter
) : BrainwalletViewModel<SendEvent>() {

    private val _state =
        MutableStateFlow(SendState())
    val state: StateFlow<SendState> = _state.asStateFlow()

    override fun onEvent(event: SendEvent) {
        when (event) {
            is SendEvent.OnLoad -> {
                val iso = currencyDataGetter.getIsoSymbol()

                var formattedCurrency: String? = null
                val currency = currencyDataGetter.getCurrencyByIso(iso)
                if (currency != null) {
                    val roundedPriceAmount: BigDecimal =
                        BigDecimal(currency.rate.toDouble()).multiply(BigDecimal(100))
                            .divide(BigDecimal(100), 2, BRConstants.ROUNDING_MODE)
                    formattedCurrency =
                        currencyDataGetter.getFormattedCurrencyString(
                            iso,
                            roundedPriceAmount
                        )
                } else {
                    Timber.w("The currency related to %s is NULL", iso)
                }

                if (formattedCurrency != null) {
                    _state.update {
                        it.copy(
                            iso = iso,
                            formattedCurrency = formattedCurrency
                        )
                    }
                }
            }
        }
    }
}
