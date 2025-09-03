package com.brainwallet.billing.data.source.remote

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.brainwallet.billing.data.source.BillingClientProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Single
import kotlin.coroutines.resume

@Single
class BillingRemoteSource(
    private val billingClientProvider: BillingClientProvider
) {

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 3
        const val INITIAL_RETRY_DELAY_MS = 2000L
        const val RETRY_MULTIPLIER = 2
    }

    suspend fun establishConnection(): Result<Unit> {
        return executeWithRetry(
            maxAttempts = MAX_RETRY_ATTEMPTS,
            operation = { attemptConnection() }
        )
    }

    suspend fun queryProductDetails(params: QueryProductDetailsParams): Result<List<ProductDetails>> {
        return executeWithRetry(
            maxAttempts = MAX_RETRY_ATTEMPTS,
            operation = { attemptQueryProductDetails(params) }
        )
    }

    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        val client = billingClientProvider.getClient()
        return client.launchBillingFlow(activity, params)
    }

    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        return executeWithRetry(
            maxAttempts = MAX_RETRY_ATTEMPTS,
            operation = { attemptAcknowledgePurchase(purchaseToken) }
        )
    }

    suspend fun queryPurchases(productType: String): Result<List<Purchase>> {
        return executeWithRetry(
            maxAttempts = MAX_RETRY_ATTEMPTS,
            operation = { attemptQueryPurchases(productType) }
        )
    }

    fun isReady(): Boolean {
        return billingClientProvider.getClient().isReady
    }

    fun isFeatureSupported(feature: String): Boolean {
        return billingClientProvider.getClient()
            .isFeatureSupported(feature).responseCode == BillingClient.BillingResponseCode.OK
    }

    private suspend fun attemptConnection(): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClientProvider.getClient()

            if (client.isReady) {
                continuation.resume(Result.success(Unit))
                return@suspendCancellableCoroutine
            }

            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            continuation.resume(Result.success(Unit))
                        }

                        else -> {
                            val exception = createBillingException(billingResult)
                            continuation.resume(Result.failure(exception))
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    val exception = BillingException(
                        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                        "Billing service disconnected"
                    )
                    continuation.resume(Result.failure(exception))
                }
            })
        }

    private suspend fun attemptQueryProductDetails(params: QueryProductDetailsParams): Result<List<ProductDetails>> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClientProvider.getClient()

            if (!client.isReady) {
                val exception = BillingException(
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    "Billing client not ready"
                )
                continuation.resume(Result.failure(exception))
                return@suspendCancellableCoroutine
            }

            client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        continuation.resume(Result.success(productDetailsList.productDetailsList))
                    }

                    else -> {
                        val exception = createBillingException(billingResult)
                        continuation.resume(Result.failure(exception))
                    }
                }
            }
        }


    private suspend fun attemptAcknowledgePurchase(purchaseToken: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClientProvider.getClient()

            if (!client.isReady) {
                val exception = BillingException(
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    "Billing client not ready"
                )
                continuation.resume(Result.failure(exception))
                return@suspendCancellableCoroutine
            }

            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            client.acknowledgePurchase(acknowledgeParams) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        continuation.resume(Result.success(Unit))
                    }

                    BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                        // Handle stale cache by querying purchases first
                        continuation.resume(
                            Result.failure(
                                BillingException(
                                    billingResult.responseCode,
                                    "Item not owned - possible stale cache"
                                )
                            )
                        )
                    }

                    else -> {
                        val exception = createBillingException(billingResult)
                        continuation.resume(Result.failure(exception))
                    }
                }
            }
        }

    private suspend fun attemptQueryPurchases(productType: String): Result<List<Purchase>> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClientProvider.getClient()

            if (!client.isReady) {
                val exception = BillingException(
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                    "Billing client not ready"
                )
                continuation.resume(Result.failure(exception))
                return@suspendCancellableCoroutine
            }

            val queryParams = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()

            client.queryPurchasesAsync(queryParams) { billingResult, purchasesList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        continuation.resume(Result.success(purchasesList))
                    }

                    else -> {
                        val exception = createBillingException(billingResult)
                        continuation.resume(Result.failure(exception))
                    }
                }
            }
        }

    private suspend fun <T> executeWithRetry(
        maxAttempts: Int,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = INITIAL_RETRY_DELAY_MS

        repeat(maxAttempts) { attempt ->
            val result = operation()

            if (result.isSuccess) {
                return result
            }

            val exception = result.exceptionOrNull()
            if (exception is BillingException && !exception.isRetriable()) {
                return result
            }

            if (attempt < maxAttempts - 1) {
                delay(currentDelay)
                currentDelay *= RETRY_MULTIPLIER
            }
        }

        return operation()
    }

    private fun createBillingException(billingResult: BillingResult): BillingException {
        return BillingException(
            billingResult.responseCode,
            billingResult.debugMessage ?: "Unknown billing error"
        )
    }
}

data class BillingException(
    val responseCode: Int,
    override val message: String
) : Exception(message) {

    fun isRetriable(): Boolean {
        return when (responseCode) {
            BillingClient.BillingResponseCode.NETWORK_ERROR,
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.ERROR -> true

            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> true // Cache issues

            else -> false
        }
    }
}