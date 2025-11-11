package com.brainwallet.ltc.domain.flow

import androidx.compose.runtime.Stable
import com.brainwallet.ltc.domain.model.SyncState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Stable
interface SyncStateFlow : StateFlow<SyncState> {
    fun startSync()
    fun stopSync()
}
