package com.brainwallet.billing.domain.repository

import com.brainwallet.billing.domain.model.ConnectionResult

interface BillingConnectionRepository {
    suspend fun establishConnection(): ConnectionResult
    fun isReady(): Boolean
    fun isFeatureSupported(feature: String): Boolean
}