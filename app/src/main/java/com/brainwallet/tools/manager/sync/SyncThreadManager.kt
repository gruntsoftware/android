package com.brainwallet.tools.manager.sync

import com.brainwallet.data.repository.SyncAnalyticsRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.BRSharedPrefs.putEndSyncTimestamp
import com.brainwallet.tools.manager.BRSharedPrefs.putStartSyncTimestamp
import com.brainwallet.tools.manager.BRSharedPrefs.putSyncMetadata
import com.brainwallet.wallet.BRPeerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

@Single
class SyncThreadManager(
    private val syncAnalyticsRepository: SyncAnalyticsRepository,
    private val sharedPrefs: BRSharedPrefs,
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var syncJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    sealed class SyncState {
        data object Idle : SyncState()
        data class Syncing(
            val progress: Float,
            val lastBlockTimestamp: Long,
            val currentBlockHeight: Int,
        ) : SyncState()
        data object Complete : SyncState()
    }

    fun startSyncing(startHeight: Int) {
        if (syncJob?.isActive == true) return

        syncJob = scope.launch(Dispatchers.IO) {
            val runTimestamp = System.currentTimeMillis()
            putStartSyncTimestamp(runTimestamp)
            syncAnalyticsRepository.startSync()

            try {
                while (isActive) {
                    val progress = BRPeerManager.getInstance().syncProgress(startHeight).toFloat()
                    val lastBlockTimestamp = BRPeerManager.getInstance().getLastBlockTimestamp() * 1000L
                    val currentBlockHeight = BRPeerManager.getInstance().getCurrentBlockHeight()

                    if (progress >= 1f) {
                        val endTimestamp = System.currentTimeMillis()
                        putEndSyncTimestamp(endTimestamp)

                        val durationMinutes = (endTimestamp - runTimestamp) / 1_000.0 / 60.0
                        if (durationMinutes > 2.0) {
                            putSyncMetadata(runTimestamp, endTimestamp)
                        }
                        _syncState.value = SyncState.Complete
                        syncAnalyticsRepository.completeSync()
                        break
                    }

                    _syncState.value = SyncState.Syncing(
                        progress = progress,
                        lastBlockTimestamp = lastBlockTimestamp,
                        currentBlockHeight = currentBlockHeight,
                    )

                    delay(100)
                }
            } catch (e: CancellationException) {
                Timber.d("SyncManager: job cancelled")
                throw e
            } finally {
                if (_syncState.value !is SyncState.Complete) {
                    syncAnalyticsRepository.stopSync()
                    _syncState.value = SyncState.Idle
                }
            }
        }
    }

    fun stopSyncing() {
        syncJob?.cancel()
        syncJob = null
    }

    companion object {
        @JvmStatic
        fun getInstance(): SyncThreadManager = KoinJavaComponent
            .get(SyncThreadManager::class.java)
    }
}
