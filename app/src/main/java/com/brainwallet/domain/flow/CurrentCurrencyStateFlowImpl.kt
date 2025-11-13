package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.ltc.domain.flow.CurrentCurrencyStateFlow
import com.brainwallet.tools.manager.BRSharedPrefs
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class CurrentCurrencyStateFlowImpl(
    private val context: Context,
    private val upstream: MutableStateFlow<String> = MutableStateFlow(
        BRSharedPrefs.getIsoSymbol(context)
    )
) : CurrentCurrencyStateFlow, StateFlow<String> by upstream, BRSharedPrefs.OnIsoChangedListener {

    init {
        BRSharedPrefs.addIsoChangedListener(this)
    }

    override fun onIsoChanged(iso: String) {
        upstream.value = iso
    }
}
