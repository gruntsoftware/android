package com.brainwallet.ui.screens.send

import android.content.Context
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.wallet.FakeWalletManager
import com.brainwallet.wallet.WalletOperations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [BWSender]. These avoid JNI entirely by injecting a mock
 * [WalletOperations] and a mock [PostAuth] through the constructor.
 *
 * The critical production failure mode this suite guards against:
 *   - `isSending` being left true after a failure, permanently locking Send
 *   - `timedOut` never being reset on the finally path
 *   - a null return from tryTransactionWithOps crashing the coroutine
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BWSenderTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var context: Context
    private lateinit var walletManager: WalletOperations
    private lateinit var postAuth: PostAuth
    private lateinit var sender: BWSender

    private val validSerializedTx = ByteArray(32) { (it + 1).toByte() }

    private fun buildTx(
        sendAddress: String? = "LValidAddr",
        sendAmount: Long = 100_000L,
        opsAddress: String? = "LOpsAddr",
        opsFee: Long = 500L,
        isAmountRequested: Boolean = false
    ): TransactionItem = TransactionItem(
        sendAddress,
        opsAddress,
        null,
        sendAmount,
        opsFee,
        null,
        isAmountRequested,
        ""
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = mockk(relaxed = true)
        val fakeWallet = FakeWalletManager(created = true)
        walletManager = mockk<WalletOperations>(relaxed = true) {
            every {
                tryTransactionWithOps(any(), any(), any(), any())
            } returns validSerializedTx
            every { getMinOutputAmount() } returns 1_000L
            every { getMinOutputAmountRequested() } returns 500L
        }
        postAuth = mockk<PostAuth>(relaxed = true)

        sender = BWSender(
            context = context,
            getWalletManager = { walletManager },
            getPostAuth = { postAuth }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    fun `prepareTransaction returns Success and calls PostAuth`() = runTest {
        val result = sender.prepareTransaction(buildTx())

        assertEquals(BWSendResult.Success, result)
        verify(exactly = 1) { postAuth.setTransactionItem(any()) }
        verify(exactly = 1) { postAuth.onPublishTxAuth(context, false) }
    }

    @Test
    fun `prepareTransaction mutates serializedTx on the item`() = runTest {
        val tx = buildTx()
        sender.prepareTransaction(tx)
        assertTrue(
            "BWSender must populate serializedTx before calling PostAuth",
            tx.serializedTx?.contentEquals(validSerializedTx) == true
        )
    }

    // -------------------------------------------------------------------------
    // REGRESSION: isSending must always be reset in finally
    // -------------------------------------------------------------------------

    @Test
    fun `isSending is cleared after successful send`() = runTest {
        sender.prepareTransaction(buildTx())
        advanceUntilIdle()
        assertFalse(
            "isSending must be cleared or subsequent sends are permanently blocked",
            sender.isSending
        )
    }

    @Test
    fun `isSending is cleared after null transaction returns Unknown error`() = runTest {
        every {
            walletManager.tryTransactionWithOps(any(), any(), any(), any())
        } returns null

        val result = sender.prepareTransaction(buildTx())

        assertTrue(result is BWSendResult.Error.Unknown)
        assertFalse(sender.isSending)
    }

    @Test
    fun `isSending is cleared after native throws`() = runTest {
        every {
            walletManager.tryTransactionWithOps(any(), any(), any(), any())
        } throws RuntimeException("native boom")

        val result = sender.prepareTransaction(buildTx())

        assertTrue(result is BWSendResult.Error.Unknown)
        assertFalse("finally block must clear isSending on exception", sender.isSending)
    }

    @Test
    fun `timedOut flag is cleared in finally`() = runTest {
        sender.timedOut = true
        // Next attempt should short-circuit to TimedOut but still reset the flag.
        val result = sender.prepareTransaction(buildTx())

        assertEquals(BWSendResult.Error.TimedOut, result)
        assertFalse("timedOut must not leak into subsequent attempts", sender.timedOut)

        // And a follow-up send should now succeed.
        val second = sender.prepareTransaction(buildTx())
        assertEquals(BWSendResult.Success, second)
    }

    // -------------------------------------------------------------------------
    // REGRESSION: AlreadySending reentrancy guard
    // -------------------------------------------------------------------------

    @Test
    fun `second concurrent prepareTransaction returns AlreadySending`() = runTest {
        // Block the first call inside tryTransactionWithOps by making it slow.
        every {
            walletManager.tryTransactionWithOps(
                any(),
                any(),
                any(),
                any()
            )
        } answers {
            Thread.sleep(200)
            validSerializedTx
        }

        val first = async { sender.prepareTransaction(buildTx()) }
        // Give the first call a chance to flip isSending = true.
        advanceUntilIdle()
        val second = sender.prepareTransaction(buildTx())

        // Second call must be rejected without touching the wallet again.
        assertEquals(BWSendResult.Error.AlreadySending, second)
        first.await()
    }

    // -------------------------------------------------------------------------
    // Validation of inputs
    // -------------------------------------------------------------------------

    @Test
    fun `null sendAddress returns Unknown error`() = runTest {
        val result = sender.prepareTransaction(buildTx(sendAddress = null))
        assertTrue(result is BWSendResult.Error.Unknown)
        verify(exactly = 0) { postAuth.setTransactionItem(any()) }
    }

    @Test
    fun `amount below minOutput returns AmountTooSmall`() = runTest {
        every { walletManager.getMinOutputAmount() } returns 10_000L

        val result = sender.prepareTransaction(buildTx(sendAmount = 500L))

        assertTrue(result is BWSendResult.Error.AmountTooSmall)
        assertEquals(10_000L, (result as BWSendResult.Error.AmountTooSmall).minAmount)
        coVerify(exactly = 0) { postAuth.onPublishTxAuth(any(), any()) }
    }

    @Test
    fun `isAmountRequested uses minOutputAmountRequested`() = runTest {
        every { walletManager.getMinOutputAmount() } returns 10_000L
        every { walletManager.getMinOutputAmountRequested() } returns 100L

        // 500 is below the regular minOutput but above the requested one.
        val result = sender.prepareTransaction(
            buildTx(sendAmount = 500L, isAmountRequested = true)
        )

        assertEquals(BWSendResult.Success, result)
        verify(exactly = 1) { walletManager.getMinOutputAmountRequested() }
        verify(exactly = 0) { walletManager.getMinOutputAmount() }
    }
}
