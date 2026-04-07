package com.brainwallet.ui.screens.send

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.repository.LtcRepository
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
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<SendEvent>() {
    private val _state =
        MutableStateFlow(SendState())
    val state: StateFlow<SendState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingRepository.currentSettings.collect { currentSettings ->
                _state.update {
                    it.copy(
                        darkMode = currentSettings.isDarkMode
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
            is SendEvent.OnCheckIfSendIsReady -> {
                Timber.i("SendEvent.OnCheckIfSendIsReady")
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
                Timber.i("SendEvent.OnTapPasteLTCAddress")
            }
            is SendEvent.OnToggleFiatOrLTC -> {
                _state.update { it.copy(userViewsFiat = !it.userViewsFiat) }
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
                val amountInDecimalLTC = event.amountInLTCString.toBigDecimalOrNull() ?: BigDecimal.ZERO
                val litoshiAmount = amountInDecimalLTC
                    .multiply(BigDecimal(BRExchange.ONE_LITECOIN_OF_LITOSHIS))
                    .toLong()
                val currentBalance = BRWalletManager.getInstance().getBalance(app)
                val networkFee = FeeManager.getInstance().currentFeeValue
                val opsFee = Utils.tieredOpsFee(app, litoshiAmount)
                val isAmountValid = currentBalance >= (litoshiAmount + opsFee + networkFee)

                _state.update {
                    it.copy(
                        amountInLTCString = event.amountInLTCString,
                        isAmountBelowBalance = isAmountValid,
                        isReadyToSend = BRWalletManager.validateAddress(it.recipientLTCAddress) && isAmountValid
                    )
                }
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
