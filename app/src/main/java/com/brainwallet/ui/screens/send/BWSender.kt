package com.brainwallet.ui.screens.send

import android.content.Context
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlinx.coroutines.withContext

@Single
class BWSender(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile var timedOut = false

    @Volatile var isSending = false

    suspend fun prepareTransaction(
        transactionItem: TransactionItem,
    ): BWSendResult = withContext(Dispatchers.IO) {
        try {
            if (isSending) {
                Timber.e("sendTransaction returned because already sending..")
                return@withContext BWSendResult.Error.AlreadySending
            }
            isSending = true

            if (timedOut) {
                Timber.e("sendTransaction returned because timed out..")
                return@withContext BWSendResult.Error.TimedOut
            }

            verifyAndSetTransaction(transactionItem)
        } catch (e: Exception) {
            Timber.e(e)
            BWSendResult.Error.Unknown(e)
        } finally {
            isSending = false
            timedOut = false
        }
    }
    private suspend fun verifyAndSetTransaction(transactionItem: TransactionItem): BWSendResult {
        if (transactionItem.sendAddress == null) {
            val ex = RuntimeException("transactionItem is malformed: sendAddress is null")
            Timber.e(ex)
            return BWSendResult.Error.Unknown(ex)
        }

        val walletManager = BRWalletManager.getInstance()

        // Transaction creation is CPU/IO-bound — run on IO dispatcher
        val temporaryTransaction = withContext(Dispatchers.IO) {
            walletManager.tryTransactionWithOps(
                transactionItem.sendAddress,
                transactionItem.sendAmount,
                transactionItem.opsAddress,
                transactionItem.opsFee,
            )
        } ?: return BWSendResult.Error.Unknown(RuntimeException("tryTransactionWithOps returned null"))

        // Back on the calling dispatcher — mutate and hand off to PostAuth
        transactionItem.serializedTx = temporaryTransaction
        PostAuth.getInstance().setTransactionItem(transactionItem)
        return tryToPublishToMainnet(transactionItem)
    }

    private suspend fun tryToPublishToMainnet(transactionItem: TransactionItem): BWSendResult {
        val walletManager = BRWalletManager.getInstance()

        val minOutput = if (transactionItem.isAmountRequested) {
            walletManager.getMinOutputAmountRequested()
        } else {
            walletManager.getMinOutputAmount()
        }

        if (transactionItem.sendAmount < minOutput) {
            return BWSendResult.Error.AmountTooSmall(minOutput)
        }

        // onPublishTxAuth is a blocking native call — keep it off the main thread
        withContext(Dispatchers.IO) {
            PostAuth.getInstance().onPublishTxAuth(context, false)
        }

        return BWSendResult.Success
    }
}
