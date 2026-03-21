package com.brainwallet.di

import com.brainwallet.data.repository.MockedTxRepository
import com.brainwallet.data.repository.TxRepository
import org.koin.dsl.module

val debugModule = module {
    single<TxRepository> { MockedTxRepository() }
}