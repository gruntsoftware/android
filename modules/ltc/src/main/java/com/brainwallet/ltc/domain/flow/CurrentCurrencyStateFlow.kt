package com.brainwallet.ltc.domain.flow

import androidx.compose.runtime.Stable
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Stable
interface CurrentCurrencyStateFlow : StateFlow<String>
