package com.brainwallet.ltc.presentation.component.balance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.design.presentation.state.rememberIsConnectedState
import com.brainwallet.ltc.domain.flow.BalanceStateFlow
import com.brainwallet.ltc.domain.flow.SyncStateFlow
import com.brainwallet.ltc.domain.model.BalanceState
import com.brainwallet.ltc.domain.model.SyncState
import org.koin.compose.koinInject

@Composable
fun rememberBalanceBentoGridState(
    syncStateFlow: SyncStateFlow = koinInject(),
    balanceStateFlow: BalanceStateFlow = koinInject()
): BalanceBentoGridUiState {
    val syncState by syncStateFlow.collectAsStateWithLifecycle()
    val balanceState by balanceStateFlow.collectAsStateWithLifecycle()
    val isConnected = rememberIsConnectedState().isConnected
    val state = remember {
        BalanceBentoGridUiState(
            initialSyncState = syncState,
            initialBalance = balanceState,
            initialIsShown = false
        )
    }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            syncStateFlow.startSync()
        } else {
            syncStateFlow.stopSync()
        }
    }
    LaunchedEffect(syncState) {
        state.updateSyncState(syncState)
    }
    LaunchedEffect(balanceState) {
        state.updateBalanceState(balanceState)
    }
    return state
}

@Stable
class BalanceBentoGridUiState(
    initialSyncState: SyncState = SyncState.Idle,
    initialBalance: BalanceState = BalanceState(),
    initialLastBlock: Int = 0,
    initialIsShown: Boolean = false
) {
    var syncState by mutableStateOf(initialSyncState)
        private set

    var balanceState by mutableStateOf(initialBalance)
        private set

    var lastBlock by mutableStateOf(initialLastBlock)
        private set

    var isShown by mutableStateOf(initialIsShown)
        private set

    fun updateSyncState(syncState: SyncState) {
        if (syncState is SyncState.Started) {
            lastBlock = syncState.lastBlockHeight
        }
        this.syncState = syncState
    }

    fun updateBalanceState(balanceState: BalanceState) {
        this.balanceState = balanceState
    }

    fun toggleShown() {
        isShown = !isShown
    }
}
