package com.brainwallet.billing.data.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.brainwallet.billing.data.source.remote.BillingException
import com.brainwallet.billing.data.source.remote.BillingRemoteSource
import com.brainwallet.billing.domain.model.AcknowledgmentResult
import com.brainwallet.billing.domain.repository.BillingConnectionRepository
import com.brainwallet.billing.domain.repository.BillingPurchaseRepository
import kotlinx.coroutines.delay

class BillingPurchaseRepositoryImpl(
    private val billingRemoteSource: BillingRemoteSource,
    private val connectionRepository: BillingConnectionRepository
) : BillingPurchaseRepository {

    override suspend fun acknowledgePurchaseWithRetry(purchaseToken: String): AcknowledgmentResult {
        return try {
            if (!billingRemoteSource.isReady()) {
                val connectionResult = connectionRepository.establishConnection()
                if (!connectionResult.isConnected) {
                    return AcknowledgmentResult(
                        isSuccessful = false,
                        errorMessage = "Failed to establish connection: ${connectionResult.errorMessage}",
                        shouldRetryLater = true
                    )
                }
            }

            val result = billingRemoteSource.acknowledgePurchase(purchaseToken)

            if (result.isSuccess) {
                AcknowledgmentResult(isSuccessful = true)
            } else {
                val exception = result.exceptionOrNull()
                when (exception) {
                    is BillingException -> handleAcknowledgmentError(exception, purchaseToken)
                    else -> AcknowledgmentResult(
                        isSuccessful = false,
                        errorMessage = "Unknown acknowledgment error: ${exception?.message}",
                        shouldRetryLater = true
                    )
                }
            }
        } catch (e: Exception) {
            AcknowledgmentResult(
                isSuccessful = false,
                errorMessage = "Unexpected error during acknowledgment: ${e.message}",
                shouldRetryLater = true
            )
        }
    }

    override suspend fun queryUserPurchases(productType: String): Result<List<Purchase>> {
        return try {
            if (!billingRemoteSource.isReady()) {
                val connectionResult = connectionRepository.establishConnection()
                if (!connectionResult.isConnected) {
                    return Result.failure(Exception("Failed to establish connection: ${connectionResult.errorMessage}"))
                }
            }

            billingRemoteSource.queryPurchases(productType)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to query purchases: ${e.message}"))
        }
    }

    private suspend fun handleAcknowledgmentError(
        exception: BillingException,
        purchaseToken: String
    ): AcknowledgmentResult {
        return when (exception.responseCode) {
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> {
                // Handle stale cache by querying purchases first
                try {
                    val purchasesResult =
                        billingRemoteSource.queryPurchases(BillingClient.ProductType.INAPP)
                    if (purchasesResult.isSuccess) {
                        val purchases = purchasesResult.getOrNull() ?: emptyList()
                        val purchase = purchases.find { it.purchaseToken == purchaseToken }

                        if (purchase != null) {
                            // Purchase exists, retry acknowledgment
                            delay(1000) // Brief delay before retry
                            val retryResult = billingRemoteSource.acknowledgePurchase(purchaseToken)

                            if (retryResult.isSuccess) {
                                AcknowledgmentResult(isSuccessful = true)
                            } else {
                                AcknowledgmentResult(
                                    isSuccessful = false,
                                    errorMessage = "Retry failed after cache refresh",
                                    shouldRetryLater = true
                                )
                            }
                        } else {
                            AcknowledgmentResult(
                                isSuccessful = false,
                                errorMessage = "Purchase not found after cache refresh - may already be acknowledged"
                            )
                        }
                    } else {
                        AcknowledgmentResult(
                            isSuccessful = false,
                            errorMessage = "Failed to refresh purchase cache",
                            shouldRetryLater = true
                        )
                    }
                } catch (e: Exception) {
                    AcknowledgmentResult(
                        isSuccessful = false,
                        errorMessage = "Error handling stale cache: ${e.message}",
                        shouldRetryLater = true
                    )
                }
            }

            BillingClient.BillingResponseCode.NETWORK_ERROR,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.ERROR -> {
                AcknowledgmentResult(
                    isSuccessful = false,
                    errorMessage = "Temporary error during acknowledgment: ${exception.message}",
                    shouldRetryLater = true
                )
            }

            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> {
                AcknowledgmentResult(
                    isSuccessful = false,
                    errorMessage = "Acknowledgment failed permanently: ${exception.message}",
                    shouldRetryLater = false
                )
            }

            else -> {
                AcknowledgmentResult(
                    isSuccessful = false,
                    errorMessage = "Unknown acknowledgment error: ${exception.message}",
                    shouldRetryLater = true
                )
            }
        }
    }
}