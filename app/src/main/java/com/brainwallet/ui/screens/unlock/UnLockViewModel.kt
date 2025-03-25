package com.brainwallet.ui.screens.unlock

import androidx.lifecycle.viewModelScope
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal


class UnLockViewModel : BrainwalletViewModel<UnLockEvent>() {

    private val _state = MutableStateFlow(UnLockState())
    val state: StateFlow<UnLockState> = _state.asStateFlow()

    override fun onEvent(event: UnLockEvent) {
        when (event) {
            is UnLockEvent.OnPinDigitChange -> _state.updateAndGet {
                val pinDigits = it.passcode.toMutableList()
                if (event.digit < -1) {
                    return
                }

                val index = pinDigits.indexOfFirst { it == -1 }
                if (index == -1) {
                    return
                }
                pinDigits[index] = event.digit
                it.copy(passcode = pinDigits)
            }.also {

                if (it.isPasscodeFilled() && it.isUpdatePin) {
                    //if update pin from drawer
                    if (event.isValidPin.invoke(it.passcode.joinToString(""))) {
                        sendUiEffect(
                            UiEffect.Navigate(
                                destinationRoute = Route.SetPasscode()
                            )
                        )
                    }
                    return
                }

                if (it.isPasscodeFilled()) {
                    viewModelScope.launch {
                        EventBus.emit(EventBus.Event.LegacyUnLock(it.passcode))
                    }
                    return
                }
            }

            UnLockEvent.OnDeletePinDigit -> _state.update {
                val pinDigits = it.passcode.toMutableList()
                val lastNonMinusOneIndex = pinDigits.indexOfLast { digit -> digit != -1 }
                if (lastNonMinusOneIndex != -1) {
                    pinDigits[lastNonMinusOneIndex] = -1
                    it.copy(passcode = pinDigits)
                } else {
                    it
                }
            }

            is UnLockEvent.OnLoad -> {
                val iso = BRSharedPrefs.getIsoSymbol(event.context)

                var formattedCurrency: String? = null
                val currency = CurrencyDataSource.getInstance(event.context).getCurrencyByIso(iso)
                if (currency != null) {
                    val roundedPriceAmount: BigDecimal =
                        BigDecimal(currency.rate.toDouble()).multiply(BigDecimal(100))
                            .divide(BigDecimal(100), 2, BRConstants.ROUNDING_MODE)
                    formattedCurrency =
                        BRCurrency.getFormattedCurrencyString(
                            event.context,
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
                            formattedCurrency = formattedCurrency,
                            isUpdatePin = event.isUpdatePin
                        )

                    }
                }
            }
        }
    }
}