package com.brainwallet.data.repository

import android.app.Application
import app.cash.turbine.test
import com.brainwallet.data.source.TransactionSource
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for the receive-transaction flow.
 *
 * TxRepositoryImpl is the single source of truth for transaction state. It pulls
 * raw TxItems from BRWalletManager, exposes them as an immutable StateFlow, and
 * provides filtered views for sent and received transactions. Tests cover the full
 * refresh lifecycle, filtering correctness, and StateFlow emission behaviour.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TxRepositoryImplIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: Application
    private lateinit var transactionSource: TransactionSource
    private lateinit var repository: TxRepositoryImpl

    @Before
    fun setUp() {
        app = mockk(relaxed = true)
        transactionSource = mockk(relaxed = true)
        repository = TxRepositoryImpl(app, transactionSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun makeTxItem(
        sent: Long = 0L,
        received: Long = 0L,
        timeStamp: Long = 1_000_000L,
        txReversed: String = "abc123",
    ) = TxItem(
        timeStamp,
        800_000,
        byteArrayOf(0x01),
        txReversed,
        sent,
        received,
        1_000L,
        arrayOf("LReceiver"),
        arrayOf("LSender"),
        0L,
        250,
        longArrayOf(sent + received),
        true,
    )

    // ── initial state ─────────────────────────────────────────────────────────

    @Test
    fun `transactionItems StateFlow starts empty before any refresh`() {
        assertTrue(repository.transactionItems.value.isEmpty())
    }

    // ── refresh: happy path ───────────────────────────────────────────────────

    @Test
    fun `refresh populates transactionItems with all returned transactions`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 50_000L),
            makeTxItem(sent = 30_000L),
        )

        repository.refresh()

        assertEquals(2, repository.transactionItems.value.size)
    }

    @Test
    fun `refresh with a single received transaction stores it correctly`() = runTest {
        val expectedReceived = 75_000L
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = expectedReceived)
        )

        repository.refresh()

        assertEquals(1, repository.transactionItems.value.size)
        assertEquals(expectedReceived, repository.transactionItems.value[0].received)
    }

    @Test
    fun `refresh with a single sent transaction stores it correctly`() = runTest {
        val expectedSent = 40_000L
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(sent = expectedSent)
        )

        repository.refresh()

        assertEquals(1, repository.transactionItems.value.size)
        assertEquals(expectedSent, repository.transactionItems.value[0].sent)
    }

    // ── refresh: edge cases ───────────────────────────────────────────────────

    @Test
    fun `refresh with null from wallet produces empty list`() = runTest {
        every { transactionSource.getTransactions() } returns null

        repository.refresh()

        assertTrue(repository.transactionItems.value.isEmpty())
    }

    @Test
    fun `refresh with empty array produces empty list`() = runTest {
        every { transactionSource.getTransactions() } returns emptyArray()

        repository.refresh()

        assertTrue(repository.transactionItems.value.isEmpty())
    }

    @Test
    fun `subsequent refresh replaces previous transaction list`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 10_000L)
        )
        repository.refresh()
        assertEquals(1, repository.transactionItems.value.size)

        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 20_000L),
            makeTxItem(received = 30_000L),
        )
        repository.refresh()

        assertEquals(2, repository.transactionItems.value.size)
    }

    @Test
    fun `refresh after populated state with null collapses list to empty`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(makeTxItem(received = 10_000L))
        repository.refresh()

        every { transactionSource.getTransactions() } returns null
        repository.refresh()

        assertTrue(repository.transactionItems.value.isEmpty())
    }

    // ── receive filter ────────────────────────────────────────────────────────

    @Test
    fun `allReceiveTransactions returns only items with received greater than zero`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 50_000L),
            makeTxItem(sent = 30_000L),
        )
        repository.refresh()

        val result = repository.allReceiveTransactions()

        assertEquals(1, result.size)
        assertEquals(50_000L, result[0].received)
    }

    @Test
    fun `allReceiveTransactions returns all items when all have received amounts`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 10_000L),
            makeTxItem(received = 20_000L),
            makeTxItem(received = 30_000L),
        )
        repository.refresh()

        val result = repository.allReceiveTransactions()

        assertEquals(3, result.size)
    }

    @Test
    fun `allReceiveTransactions returns empty when no incoming transactions exist`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(sent = 20_000L),
            makeTxItem(sent = 10_000L),
        )
        repository.refresh()

        assertTrue(repository.allReceiveTransactions().isEmpty())
    }

    // ── sent filter ───────────────────────────────────────────────────────────

    @Test
    fun `allSentTransactions returns only items with sent greater than zero`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 50_000L),
            makeTxItem(sent = 30_000L),
        )
        repository.refresh()

        val result = repository.allSentTransactions()

        assertEquals(1, result.size)
        assertEquals(30_000L, result[0].sent)
    }

    @Test
    fun `allSentTransactions returns all items when all have sent amounts`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(sent = 10_000L),
            makeTxItem(sent = 20_000L),
        )
        repository.refresh()

        val result = repository.allSentTransactions()

        assertEquals(2, result.size)
    }

    @Test
    fun `allSentTransactions returns empty when no outgoing transactions exist`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(
            makeTxItem(received = 20_000L)
        )
        repository.refresh()

        assertTrue(repository.allSentTransactions().isEmpty())
    }

    // ── StateFlow emissions ───────────────────────────────────────────────────

    @Test
    fun `transactionItems flow emits empty then updated list after refresh`() = runTest {
        every { transactionSource.getTransactions() } returns arrayOf(makeTxItem(received = 10_000L))

        repository.transactionItems.test {
            assertEquals(0, awaitItem().size) // initial empty emission
            repository.refresh()
            assertEquals(1, awaitItem().size) // post-refresh emission
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transactionItems flow emits twice on two refreshes with different data`() = runTest {
        repository.transactionItems.test {
            awaitItem() // initial empty

            every { transactionSource.getTransactions() } returns arrayOf(makeTxItem(received = 5_000L))
            repository.refresh()
            assertEquals(1, awaitItem().size)

            every { transactionSource.getTransactions() } returns arrayOf(
                makeTxItem(received = 5_000L),
                makeTxItem(sent = 3_000L),
            )
            repository.refresh()
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
