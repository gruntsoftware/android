package com.brainwallet.billing.data.model

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

data class BrainWalletBillingResult(
    val billingResult: BillingResult,
    val purchases: List<Purchase?>?
)