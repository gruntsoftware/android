package com.brainwallet.billing.data.repository

import com.brainwallet.billing.data.source.local.dao.PurchaseTransactionDao
import com.brainwallet.billing.data.source.remote.BillingRemoteSource
import com.brainwallet.billing.domain.repository.BillingConnectionRepository
import com.brainwallet.billing.domain.repository.BillingLocalRepository
import com.brainwallet.billing.domain.repository.BillingProductRepository
import com.brainwallet.billing.domain.repository.BillingPurchaseRepository
import org.koin.core.annotation.Single

@Single
class BillingRepository(
    private val remoteSource: BillingRemoteSource,
    private val localSource: PurchaseTransactionDao,
    private val connectionRepository: BillingConnectionRepository = BillingConnectionRepositoryImpl(
        remoteSource
    ),
    private val productRepository: BillingProductRepository = BillingProductRepositoryImpl(
        remoteSource,
        connectionRepository
    ),
    private val purchaseRepository: BillingPurchaseRepository = BillingPurchaseRepositoryImpl(
        remoteSource,
        connectionRepository
    ),
    private val localRepository: BillingLocalRepository = BillingLocalRepositoryImpl(
        localSource
    )
) : BillingConnectionRepository by connectionRepository,
    BillingProductRepository by productRepository,
    BillingPurchaseRepository by purchaseRepository,
    BillingLocalRepository by localRepository
