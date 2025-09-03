package com.brainwallet.billing.data.repository

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryProductDetailsParams
import com.brainwallet.billing.data.source.remote.BillingException
import com.brainwallet.billing.data.source.remote.BillingRemoteSource
import com.brainwallet.billing.domain.model.ProductQueryResult
import com.brainwallet.billing.domain.repository.BillingConnectionRepository
import com.brainwallet.billing.domain.repository.BillingProductRepository

class BillingProductRepositoryImpl(
    private val billingRemoteSource: BillingRemoteSource,
    private val connectionRepository: BillingConnectionRepository
) : BillingProductRepository {

    override suspend fun queryAvailableProducts(productIds: List<String>, productType: String): ProductQueryResult {
        return try {
            if (!billingRemoteSource.isReady()) {
                val connectionResult = connectionRepository.establishConnection()
                if (!connectionResult.isConnected) {
                    return ProductQueryResult(
                        products = emptyList(),
                        errorMessage = "Failed to establish connection: ${connectionResult.errorMessage}"
                    )
                }
            }

            val queryParams = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    productIds.map { productId ->
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    }
                )
                .build()

            val result = billingRemoteSource.queryProductDetails(queryParams)
            
            if (result.isSuccess) {
                val products = result.getOrNull() ?: emptyList()
                ProductQueryResult(products = products)
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = when (exception) {
                    is BillingException -> handleProductQueryError(exception)
                    else -> "Unknown product query error: ${exception?.message}"
                }
                ProductQueryResult(products = emptyList(), errorMessage = errorMessage)
            }
        } catch (e: Exception) {
            ProductQueryResult(
                products = emptyList(),
                errorMessage = "Unexpected error during product query: ${e.message}"
            )
        }
    }

    private fun handleProductQueryError(exception: BillingException): String {
        return when (exception.responseCode) {
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> 
                "The requested products are not available for purchase. Please check your product configuration."
            
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> 
                "Google Play Billing service is temporarily unavailable. Please try again later."
            
            BillingClient.BillingResponseCode.NETWORK_ERROR -> 
                "Network error occurred while fetching products. Please check your internet connection."
            
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> 
                "Developer error: Please check your product configuration and API usage."
            
            else -> "Failed to query products: ${exception.message}"
        }
    }
}