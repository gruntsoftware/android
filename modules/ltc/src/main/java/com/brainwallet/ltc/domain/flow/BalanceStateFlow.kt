package com.brainwallet.ltc.domain.flow

import androidx.compose.runtime.Stable
import com.brainwallet.ltc.domain.model.BalanceState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Stable
interface BalanceStateFlow : StateFlow<BalanceState> {
    fun refreshBalance()
}
