package com.brainwallet.data.repository

import com.brainwallet.presenter.entities.TxItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MockedTxRepository : TxRepository {

    private val _transactionItems = MutableStateFlow<List<TxItem>>(emptyList())
    override val transactionItems: StateFlow<List<TxItem>> = _transactionItems.asStateFlow()

    override suspend fun refresh() {
        // Simulate network delay
        delay(5000)
        _transactionItems.update { FAKE_TRANSACTIONS }
    }

    companion object {
        val FAKE_TRANSACTIONS = listOf(
            TxItem(
                System.currentTimeMillis() / 1000L,
                1_857_462,
                ByteArray(32) { it.toByte() },
                "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
                1_000_000L,
                0L,
                1_000L,
                arrayOf("mocked_address_out"),
                arrayOf("mocked_address_in"),
                1_000_000L,
                100,
                longArrayOf(1_000_000L),
                true
            ),
            TxItem(
                System.currentTimeMillis() / 1000L,
                1_857_500,
                ByteArray(32) { it.toByte() },
                "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
                0L,
                1_000_000L,
                1_000L,
                arrayOf("mocked_address_out"),
                arrayOf("mocked_address_in"),
                1_000_000L,
                100,
                longArrayOf(1_000_000L),
                true
            )
        )
    }
}