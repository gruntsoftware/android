package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.ltc.domain.flow.BalanceStateFlow
import com.brainwallet.ltc.domain.flow.PromptStateFlow
import com.brainwallet.ltc.domain.flow.SyncStateFlow
import com.brainwallet.ltc.domain.flow.TransactionFlow
import com.brainwallet.ltc.domain.model.SyncState
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.SyncManager
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.wallet.BRPeerManager
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class SyncStateFlowImpl(
    private val context: Context,
    private val promptStateFlow: PromptStateFlow,
    private val balanceStateFlow: BalanceStateFlow,
    private val transactionFlow: TransactionFlow,
    private val upstream: MutableStateFlow<SyncState> = MutableStateFlow(SyncState.Idle)
) : SyncStateFlow, StateFlow<SyncState> by upstream, SyncManager.Listener {

    init {
        SyncManager.getInstance().addListener(this)
    }

    override fun startSync() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
            val progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(context))
            if (progress > 0 && progress < 1) {
                BRPeerManager.syncStarted()
            }
        }
    }

    override fun stopSync() {
        BRPeerManager.syncFailed()
    }

    override fun onSyncStarted(startTimestamp: Long) {
        val lastBlockHeight = BRSharedPrefs.getLastBlockHeight(context)
        upstream.update { SyncState.Started(startTimestamp, lastBlockHeight) }
        promptStateFlow.onStart()
    }

    override fun onProgressUpdate(
        progress: Double,
        formattedTimestamp: String,
        currentBlockHeight: Int
    ) {
        upstream.update {
            SyncState.Syncing(
                progress = progress,
                timeStamp = formattedTimestamp,
                currentBlockHeight = currentBlockHeight
            )
        }
        BRPeerManager.txStatusUpdate()
        promptStateFlow.onSyncProgress(progress)
    }

    override fun onSyncCompleted(
        startTimestamp: Long,
        endTimestamp: Long,
        durationMinutes: Double
    ) {
        upstream.update { SyncState.Synced }
        transactionFlow.refresh()
        balanceStateFlow.refreshBalance()
        promptStateFlow.onSynced()
    }
}
