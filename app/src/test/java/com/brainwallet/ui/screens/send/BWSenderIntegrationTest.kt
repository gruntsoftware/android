package com.brainwallet.ui.screens.send

import android.content.Context
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.util.MainDispatcherRule
import com.brainwallet.wallet.WalletOperations
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for the send-transaction flow.
 *
 * BWSender is the primary orchestrator: it validates guard conditions, builds a
 * serialized transaction via BRWalletManager, and hands it off to PostAuth for
 * broadcast. Tests cover the full happy path and every documented error result.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BWSenderIntegrationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var context: Context
    private lateinit var walletManager: WalletOperations
    private lateinit var postAuth: PostAuth
    private lateinit var sender: BWSender

    private val validTxBytes = byteArrayOf(0x01, 0x02, 0x03)
    private val sendAmount = 100_000L
    private val minOutput = 10_000L

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        walletManager = mockk(relaxed = true)
        postAuth = mockk(relaxed = true)

        every { walletManager.tryTransactionWithOps(any(),
            any(),
            any(),
            any()) } returns validTxBytes
        every { walletManager.getMinOutputAmount() } returns minOutput
        every { walletManager.getMinOutputAmountRequested() } returns minOutput
        every { postAuth.setTransactionItem(any()) } just Runs
        every { postAuth.onPublishTxAuth(any(), any()) } just Runs

        sender = BWSender(
            context = context,
            getWalletManager = { walletManager },
            getPostAuth = { postAuth },
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun makeTransactionItem(
        address: String? = "LSfkWtX2rzXrTeoFmFozj1mcXodTqg4GdH",
        amount: Long = sendAmount,
        isAmountRequested: Boolean = false,
    ) = TransactionItem(address,
        "ops_address",
        null,
        amount,
        100L,
        null, isAmountRequested)

    // ── success path ──────────────────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns Success when all conditions are met`() = runTest {
        val result = sender.prepareTransaction(makeTransactionItem())

        assertEquals(BWSendResult.Success, result)
    }

    @Test
    fun `prepareTransaction invokes onPublishTxAuth exactly once on success`() = runTest {
        sender.prepareTransaction(makeTransactionItem())

        verify(exactly = 1) { postAuth.onPublishTxAuth(context, false) }
    }

    @Test
    fun `prepareTransaction resets isSending to false after success`() = runTest {
        sender.prepareTransaction(makeTransactionItem())

        assertFalse(sender.isSending)
    }

    // ── guard: already sending ────────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns AlreadySending when isSending is true`() = runTest {
        sender.isSending = true

        val result = sender.prepareTransaction(makeTransactionItem())

        assertEquals(BWSendResult.Error.AlreadySending, result)
    }

    @Test
    fun `prepareTransaction resets isSending after AlreadySending guard triggers`() = runTest {
        sender.isSending = true
        sender.prepareTransaction(makeTransactionItem())

        assertFalse(sender.isSending)
    }

    // ── guard: timed out ──────────────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns TimedOut when timedOut is true`() = runTest {
        sender.timedOut = true

        val result = sender.prepareTransaction(makeTransactionItem())

        assertEquals(BWSendResult.Error.TimedOut, result)
    }

    @Test
    fun `prepareTransaction resets timedOut flag after TimedOut result`() = runTest {
        sender.timedOut = true
        sender.prepareTransaction(makeTransactionItem())

        assertFalse(sender.timedOut)
    }

    // ── malformed transaction item ────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns Unknown when sendAddress is null`() = runTest {
        val result = sender.prepareTransaction(makeTransactionItem(address = null))

        assertTrue(result is BWSendResult.Error.Unknown)
    }

    // ── wallet manager failures ───────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns Unknown when tryTransactionWithOps returns null`() = runTest {
        every { walletManager.tryTransactionWithOps(any(),
            any(),
            any(),
            any()) } returns null

        val result = sender.prepareTransaction(makeTransactionItem())

        assertTrue(result is BWSendResult.Error.Unknown)
    }

    @Test
    fun `prepareTransaction returns Unknown when walletManager throws`() = runTest {
        every {
            walletManager.tryTransactionWithOps(any(),
                any(),
                any(),
                any())
        } throws RuntimeException("native crash")

        val result = sender.prepareTransaction(makeTransactionItem())

        assertTrue(result is BWSendResult.Error.Unknown)
    }

    @Test
    fun `prepareTransaction resets isSending after walletManager exception`() = runTest {
        every {
            walletManager.tryTransactionWithOps(any(),
                any(),
                any(),
                any())
        } throws RuntimeException("crash")

        sender.prepareTransaction(makeTransactionItem())

        assertFalse(sender.isSending)
    }

    // ── amount too small ──────────────────────────────────────────────────────

    @Test
    fun `prepareTransaction returns AmountTooSmall when sendAmount is below minOutput`() = runTest {
        every { walletManager.getMinOutputAmount() } returns 500_000L

        val result = sender.prepareTransaction(makeTransactionItem(amount = 1_000L))

        assertTrue(result is BWSendResult.Error.AmountTooSmall)
        assertEquals(500_000L, (result as BWSendResult.Error.AmountTooSmall).minAmount)
    }

    @Test
    fun `prepareTransaction uses getMinOutputAmountRequested when isAmountRequested is true`() = runTest {
        every { walletManager.getMinOutputAmountRequested() } returns 200_000L
        every { walletManager.getMinOutputAmount() } returns 50_000L

        val result = sender.prepareTransaction(
            makeTransactionItem(amount = 100_000L, isAmountRequested = true)
        )

        assertTrue(result is BWSendResult.Error.AmountTooSmall)
        assertEquals(200_000L,
            (result as BWSendResult.Error.AmountTooSmall).minAmount)
    }

    @Test
    fun `prepareTransaction uses getMinOutputAmount when isAmountRequested is false`() = runTest {
        every { walletManager.getMinOutputAmount() } returns 150_000L
        every { walletManager.getMinOutputAmountRequested() } returns 50_000L

        val result = sender.prepareTransaction(
            makeTransactionItem(amount = 100_000L, isAmountRequested = false)
        )

        assertTrue(result is BWSendResult.Error.AmountTooSmall)
        assertEquals(150_000L,
            (result as BWSendResult.Error.AmountTooSmall).minAmount)
    }

    @Test
    fun `prepareTransaction succeeds when sendAmount equals minOutput exactly`() = runTest {
        every { walletManager.getMinOutputAmount() } returns sendAmount

        val result = sender.prepareTransaction(makeTransactionItem(amount = sendAmount))

        assertEquals(BWSendResult.Success, result)
    }
}
