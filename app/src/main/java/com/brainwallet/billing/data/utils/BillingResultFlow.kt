package com.brainwallet.billing.data.utils

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.brainwallet.billing.data.model.BrainWalletBillingResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class BillingResultFlow(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
    private val upstream: MutableSharedFlow<BrainWalletBillingResult> = MutableSharedFlow(),
) : SharedFlow<BrainWalletBillingResult> by upstream, PurchasesUpdatedListener,
    CoroutineScope by CoroutineScope(dispatcher + SupervisorJob()) {

    override fun onPurchasesUpdated(
        p0: BillingResult,
        p1: List<Purchase?>?
    ) {
        val event = BrainWalletBillingResult(p0, p1)
        launch { upstream.emit(event) }
    }
}
