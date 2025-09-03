package com.brainwallet.billing.data.source

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.brainwallet.billing.data.utils.BillingResultFlow
import org.koin.core.annotation.Single

@Single
class BillingClientProvider(
    private val context: Context,
    private val resultFlow: BillingResultFlow,
    private val builder: BillingClient.Builder = BillingClient.newBuilder(context),
) {
    @Volatile
    private var client: BillingClient? = null

    fun getClient(): BillingClient {
        return client ?: synchronized(this) {
            client ?: builder
                .setListener(resultFlow)
                .enableAutoServiceReconnection()
                .build()
                .also { client = it }
        }
    }
}
