package com.brainwallet.ui.screens.send

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.util.BRExchange
import com.brainwallet.tools.util.Utils
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal

@KoinViewModel
class SendViewModel(
    private val app: Application,
    private val txRepository: TxRepository,
    private val settingRepository: SettingRepository,
) : BrainwalletViewModel<SendEvent>() {
    private val _state =
        MutableStateFlow(SendState())
    val state: StateFlow<SendState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingRepository.currentSettings.collect { currentSettings ->
                _state.update {
                    it.copy(
                        darkMode = currentSettings.isDarkMode,
                        selectedCurrency = currentSettings.currency
                    )
                }
            }
        }
    }

    fun onResume(
        isWalletCreated: () -> Boolean = { BRWalletManager.getInstance().isCreated() },
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        viewModelScope.launch(ioDispatcher) {
            var attempts = 0

            while (!isWalletCreated() && attempts < 20) {
                delay(250)
                attempts++
            }
            if (isWalletCreated()) {
                txRepository.refresh()
                val address = _state.value.recipientLTCAddress
                if (address.isNotBlank()) {
                    val isValid = BRWalletManager.validateAddress(address)
                    _state.update {
                        it.copy(
                            isLTCAddressValid = isValid,
                            isReadyToSend = isValid && it.isAmountBelowBalance
                        )
                    }
                }
            } else {
                Timber.d("BalanceBentoViewModel: wallet not ready after waiting")
            }
        }
    }

    override fun onEvent(event: SendEvent) {
        when (event) {
            is SendEvent.OnLoad -> {
                Timber.i("SendEvent.OnLoad")
            }
            is SendEvent.OnConfirmSend -> {
                Timber.i("SendEvent.OnConfirmSend")
            }
            is SendEvent.OnTapPasteLTCAddress -> {
                val litecoinUrl = BRClipboardManager.getClipboard(app)
                if (BRWalletManager.validateAddress(litecoinUrl)) {
                    _state.update { it.copy(recipientLTCAddress = litecoinUrl) }
                } else {
                    val error = app.resources.getString(R.string.Alert_error)
                    _state.update { it.copy(recipientLTCAddress = error) }
                }
            }
            is SendEvent.OnTapShowCameraForQRLTCAddress -> {
                Timber.i("SendEvent.OnTapShowCameraForQRLTCAddress")
            }
            is SendEvent.OnToggleFiatOrLTC -> {
                val currentlyViewingFiat = _state.value.userViewsFiat
                val currentAmountString = _state.value.amountString
                val rate = _state.value.selectedCurrency.rate
                val symbol = _state.value.selectedCurrency.symbol

                val convertedAmount = if (currentAmountString.isNotBlank()) {
                    val currentAmount = currentAmountString.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val rateBD = BigDecimal(rate.toString())

                    if (currentlyViewingFiat) {
                        // switching back to LTC — divide fiat by rate
                        currentAmount
                            .divide(
                                rateBD,
                                8,
                                BWConstants.ROUNDING_MODE
                            )
                            .stripTrailingZeros()
                            .toPlainString()
                    } else {
                        // switching to fiat — multiply LTC by rate
                        String.format(
                            "%s %s",
                            symbol,
                            currentAmount
                                .multiply(rateBD)
                                .setScale(
                                    2,
                                    BWConstants.ROUNDING_MODE
                                )
                                .toPlainString()
                        )
                    }
                } else {
                    currentAmountString // no rate available, leave as-is
                }

                _state.update {
                    it.copy(
                        userViewsFiat = !it.userViewsFiat,
                        amountString = convertedAmount
                    )
                }
            }
            is SendEvent.OnRecipientAddressChanged -> {
                val isAddressValid = if (BRWalletManager.getInstance().isCreated()) {
                    BRWalletManager.validateAddress(event.address)
                } else {
                    null
                }
                _state.update {
                    it.copy(
                        recipientLTCAddress = event.address,
                        isLTCAddressValid = isAddressValid ?: true,
                        isReadyToSend = (isAddressValid == true) && it.isAmountBelowBalance
                    )
                }
            }
            is SendEvent.OnAmountChanged -> {
                val currentState = _state.value
                val enteredAmount = event
                    .amountInLTCString.toBigDecimalOrNull() ?: BigDecimal.ZERO

                // Resolve rate safely
                val rate = _state.value.selectedCurrency.rate

                // Always convert to LTC first
                val amountInLTC = when {
                    currentState.userViewsFiat && rate != null -> {
                        enteredAmount.divide(
                            BigDecimal(rate.toString()),
                            8,
                            BWConstants.ROUNDING_MODE
                        )
                    }
                    currentState.userViewsFiat && rate == null -> {
                        // rate not available yet — block send
                        null
                    }
                    else -> enteredAmount // already in LTC
                }

                // Convert LTC → litoshis safely
                val litoshiAmount = amountInLTC
                    ?.multiply(
                        BigDecimal(
                            BRExchange
                                .ONE_LITECOIN_OF_LITOSHIS
                        )
                    )
                    ?.toLong()
                    ?: 0L

                val currentBalance = BRWalletManager.getInstance().getBalance(app)
                val networkFee = FeeManager.getInstance().currentFeeValue
                val opsFee = Utils.tieredOpsFee(app, litoshiAmount)

                // isAmountValid is false if rate was unavailable in fiat mode
                val isAmountValid = amountInLTC != null &&
                    enteredAmount > BigDecimal.ZERO &&
                    currentBalance >= (litoshiAmount + opsFee + networkFee)

                _state.update {
                    it.copy(
                        amountString = event.amountInLTCString,
                        isAmountBelowBalance = isAmountValid,
                        isReadyToSend = isAmountValid &&
                            BRWalletManager.validateAddress(it.recipientLTCAddress),
                        networkFees = BigDecimal(networkFee.toDouble()),
                        serviceFees = BigDecimal(opsFee)
                    )
                }
            }
            is SendEvent.OnSend -> {
                Timber.i("SendEvent.OnSend")
            }
            is SendEvent.OnUserMemorandumChanged -> {
                _state.update { it.copy(userMemorandum = event.memo) }
            }
        }
    }
}

// if (allFilled) {
//    BRSender.getInstance().sendTransaction(
//        context,
//        TransactionItem(
//            sendAddress,
//            Utils.fetchServiceItem(context, ServiceItems.WALLETOPS),
//            null,
//            litoshiAmount.toLong(),
//            Utils.tieredOpsFee(context, litoshiAmount.toLong()),
//            null,
//            false,
//            comment
//        ),
//    )
//    AnalyticsManager.logCustomEvent(BWConstants._20191105_DSL)
//    BRSharedPrefs.incrementSendTransactionCount(context)
// }
