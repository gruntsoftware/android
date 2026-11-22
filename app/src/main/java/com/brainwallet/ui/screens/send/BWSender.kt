package com.brainwallet.ui.screens.send

import android.content.Context
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.wallet.TxPublishResults
import com.brainwallet.wallet.WalletOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Single
import timber.log.Timber
import kotlinx.coroutines.withContext

@Single
class BWSender(
    private val context: Context,
    private val getWalletManager: () -> WalletOperations = { BRWalletManager.getInstance() },
    private val getPostAuth: () -> PostAuth = { PostAuth.getInstance() },
    private val awaitPublishResult: suspend (ByteArray) -> Int = { TxPublishResults.await(it) },
    private val confirmationTimeoutMs: Long = CONFIRMATION_TIMEOUT_MS,
) {

    companion object {
        /**
         * Safety net only: the core times out an unrelayed publish on its own (ETIMEDOUT) well
         * before this. If even that never arrives, stop the spinner instead of hanging forever.
         */
        const val CONFIRMATION_TIMEOUT_MS = 90_000L

        // errno values the core reports through the publish callback
        const val ENOTCONN = 107
    }

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

        val walletManager = getWalletManager()

        // Transaction creation is CPU/IO-bound — run on IO dispatcher
        val temporaryTransaction = withContext(Dispatchers.IO) {
            walletManager.tryTransactionWithOps(
                transactionItem.sendAddress,
                transactionItem.sendAmount,
                transactionItem.opsAddress,
                transactionItem.opsFee,
            )
        } ?: return BWSendResult.Error.CreationFailed

        // Back on the calling dispatcher — mutate and hand off to PostAuth
        transactionItem.serializedTx = temporaryTransaction
        getPostAuth().setTransactionItem(transactionItem)
        return tryToPublishToMainnet(transactionItem)
    }

    private suspend fun tryToPublishToMainnet(transactionItem: TransactionItem): BWSendResult {
        val walletManager = getWalletManager()

        val minOutput = if (transactionItem.isAmountRequested) {
            walletManager.getMinOutputAmountRequested()
        } else {
            walletManager.getMinOutputAmount()
        }

        if (transactionItem.sendAmount < minOutput) {
            return BWSendResult.Error.AmountTooSmall(minOutput)
        }

        // onPublishTxAuth is a blocking native call — keep it off the main thread.
        // A returned hash only means the tx was handed to the peer manager, not that it was sent.
        val txHash = withContext(Dispatchers.IO) {
            getPostAuth().onPublishTxAuth(context, false)
        }
        if (txHash == null || txHash.isEmpty()) {
            Timber.e("publishSerializedTransaction returned no hash — nothing was published")
            return BWSendResult.Error.SigningFailed
        }

        // Wait for the core's verdict: success only once a peer relays the tx back to us
        val error = withTimeoutOrNull(confirmationTimeoutMs) { awaitPublishResult(txHash) }
        return when (error) {
            null -> {
                Timber.e("No publish callback within ${confirmationTimeoutMs}ms")
                BWSendResult.Error.ConfirmationTimedOut
            }
            TxPublishResults.SUCCESS -> BWSendResult.Success
            ENOTCONN -> BWSendResult.Error.NotConnected
            else -> {
                Timber.e("Publish rejected by network, errno=$error")
                BWSendResult.Error.PublishRejected(error)
            }
        }
    }
}
