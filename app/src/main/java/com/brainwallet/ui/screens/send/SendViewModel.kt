package com.brainwallet.ui.screens.send

import android.app.Application
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
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
import kotlinx.coroutines.flow.combine
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

        viewModelScope.launch {
            combine(
                snapshotFlow { _state.value.recipientLTCAddress },
                snapshotFlow { _state.value.amountInLTC }
            ) { address, amountInLTC ->
                val isAddressValid = BRWalletManager.validateAddress(address)
                val currentBalance = BRWalletManager.getInstance().getBalance(app)
                val amountInDecimalLTC = BigDecimal(amountInLTC.toString())
                val litoshiAmount = amountInDecimalLTC
                    .divide(BigDecimal(BRExchange.ONE_LITECOIN_OF_LITOSHIS))
                    .toLong()
                val networkFee = FeeManager.getInstance().currentFeeValue
                val opsFee = Utils.tieredOpsFee(app, litoshiAmount)
                val isAmountValid = currentBalance >= (litoshiAmount + opsFee + networkFee)

                isAddressValid && isAmountValid
            }.collect { isReadyToSend ->
                _state.update {
                    it.copy(
                        isReadyToSend = isReadyToSend
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
                Timber.i("SendEvent.OnTapPasteLTCAddress")
            }
            is SendEvent.OnTapShowCameraForQRLTCAddress -> {
                Timber.i("SendEvent.OnTapPasteLTCAddress")
            }
            is SendEvent.OnToggleFiatOrLTC -> {
                _state.update { it.copy(userViewsFiat = !it.userViewsFiat) }
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
