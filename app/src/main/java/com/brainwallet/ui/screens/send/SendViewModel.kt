package com.brainwallet.ui.screens.send

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.navigation.UiEffect
import com.brainwallet.presenter.entities.ServiceItems
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.util.BRExchange
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.ui.screens.send.BWSendResult.Error
import com.brainwallet.util.EventBus
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal
import java.util.Locale

@KoinViewModel
class SendViewModel(
    private val app: Application,
    private val bwSender: BWSender,
    private val txRepository: TxRepository,
    private val settingRepository: SettingRepository,
) : BrainwalletViewModel<SendEvent>() {
    private val _state =
        MutableStateFlow(SendState())
    val state: StateFlow<SendState> = _state.asStateFlow()
    private val _effect = MutableSharedFlow<SendEffect>(extraBufferCapacity = 1)
    val effect: SharedFlow<SendEffect> = _effect.asSharedFlow()

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
        viewModelScope.launch {
            EventBus.events
                .filterIsInstance<EventBus.Event.QRCodeScanned>()
                .collect { event ->
                    _state.update { it.copy(recipientLTCAddress = event.url ?: "") }
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
            is SendEvent.OnTapPasteLTCAddress -> {
                val litecoinUrl = BRClipboardManager.getClipboard(app)
                val isAddressValid = BRWalletManager.validateAddress(litecoinUrl)
                if (isAddressValid) {
                    _state.update {
                        it.copy(
                            recipientLTCAddress = litecoinUrl,
                            isLTCAddressValid = true,
                            isReadyToSend = isAddressValid && it.isAmountBelowBalance
                        )
                    }
                } else {
                    val error = app.resources.getString(R.string.Alert_error)
                    _state.update {
                        it.copy(
                            recipientLTCAddress = error
                        )
                    }
                }
            }
            is SendEvent.OnTapShowCameraForQRLTCAddress -> {
                sendUiEffect(UiEffect.OpenQRScanner)
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
                        serviceFees = BigDecimal(opsFee),
                        amountInLitoshi = BigDecimal(litoshiAmount)
                    )
                }
            }
            is SendEvent.OnAuthPasscode -> {
                sendUiEffect(UiEffect.OnEnterAuthPasscode)
            }
            is SendEvent.OnSend -> {
                val amountInLitoshi = _state.value.amountInLitoshi.toLong()
                val transactionItem = TransactionItem(
                    _state.value.recipientLTCAddress,
                    Utils.fetchServiceItem(app, ServiceItems.WALLETOPS),
                    null,
                    amountInLitoshi,
                    Utils.tieredOpsFee(app, amountInLitoshi),
                    null,
                    false,
                    _state.value.userMemorandum
                )

                _state.update {
                    it.copy(
                        transactionItem = transactionItem,
                        brainwalletIsPublishing = true
                    )
                }

                viewModelScope.launch {
                    when (val result = bwSender.prepareTransaction(transactionItem)) {
                        BWSendResult.Success -> {
                            _effect.emit(SendEffect.DismissSheet)
                            AnalyticsManager.logCustomEvent(BWConstants._20191105_DSL)
                            BRSharedPrefs.incrementSendTransactionCount(app)
                        }
                        is BWSendResult.Error.InsufficientFunds -> {
                            _state.update {
                                it.copy(
                                    errorResultString = String.format(
                                        Locale.getDefault(),
                                        "%s",
                                        app.getString(R.string.Import_Error_notValid)
                                    )
                                )
                            }
                        }
                        is BWSendResult.Error.AmountTooSmall -> {
                            _state.update {
                                it.copy(
                                    errorResultString = String.format(
                                        Locale.getDefault(),
                                        "%s",
                                        app.getString(R.string.Send_isRescanning)
                                    )
                                )
                            }
                        }
                        BWSendResult.Error.AlreadySending,
                        BWSendResult.Error.TimedOut,
                        is BWSendResult.Error.Unknown -> {
                            Timber.e("Send unknown error: $result")
                            _state.update {
                                it.copy(
                                    errorResultString = String.format(
                                        Locale.getDefault(),
                                        "%s",
                                        app.getString(R.string.Alerts_sendFailure)
                                    )
                                )
                            }
                        }
                    }

                    _state.update { it.copy(isPasscodeAuthenticated = true) }
                    _state.update { it.copy(brainwalletIsPublishing = false) }
                }
            }
            is SendEvent.OnUserMemorandumChanged -> {
                _state.update { it.copy(userMemorandum = event.memo) }
            }
            is SendEvent.OnFieldFocused -> {
                _state.update {
                    it.copy(
                        isReadyToSend = it.isAmountBelowBalance &&
                            BRWalletManager.validateAddress(it.recipientLTCAddress),
                    )
                }
            }
            is SendEvent.OnPasscodeDigitAdded -> {
                if (_state.value.passcode.size < 4) {
                    _state.update { it.copy(passcode = it.passcode + event.digit) }
                }
                if (_state.value.passcode.size == 4) {
                    val pin = BRKeyStore.getPinCode(app)
                    val enteredPin = _state.value.passcode.map { it.toString() }.joinToString("")
                    if (pin == enteredPin) {
                        onEvent(SendEvent.OnSend(_state.value.transactionItem))
                    } else {
                        _state.update { it.copy(isPasscodeAuthenticated = false) }
                    }
                }
            }
            is SendEvent.OnPasscodeDigitDeleted -> {
                _state.update { it.copy(passcode = it.passcode.dropLast(1)) }
            }
        }
    }

    sealed class SendEffect {
        data object DismissSheet : SendEffect()
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
