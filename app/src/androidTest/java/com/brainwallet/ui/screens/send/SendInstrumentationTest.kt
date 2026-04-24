package com.brainwallet.ui.screens.send

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.wallet.BRWalletManager
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.runner.RunWith
import kotlin.test.DefaultAsserter.assertTrue

/**
 * Instrumentation tests that exercise [BWSender] against the REAL
 * [com.brainwallet.wallet.BRWalletManager] JNI layer. These are the only way to catch regressions in
 * the native boundary — MockK cannot stub native methods on the JVM.
 *
 * Run on an emulator/device where libcore.so and libbreadwallet.so are loaded.
 *
 * NOTE: These tests assume a wallet has been initialised by the test runner or
 * a prior setup step. They `assumeTrue(isCreated)` so they are safely skipped
 * on fresh installs rather than failing spuriously.
 */
@LargeTest
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SendInstrumentationTest {

    private lateinit var context: Context
    private lateinit var walletManager: BRWalletManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        walletManager = BRWalletManager.getInstance()
    }

    @Before
    fun loadNativeLibrary() {
        System.loadLibrary("core-lib")
    }

    // -------------------------------------------------------------------------
    // Native address validation — the regression that crashed production
    // -------------------------------------------------------------------------

    @Test
    fun validateAddress_acceptsKnownGoodLitecoinAddress() {
        // Litecoin P2PKH sample address (starts with L).
        val addr = "LcWHwzZitnqQsXwi6UScRjtjDFYjR7t24t"
        assertTrue(
            "Native validator must accept canonical L-prefix addresses",
            BRWalletManager.getInstance().validateAddress(addr)
        )
    }

    @Test
    fun validateAddress_rejectsEmptyString() {
        assertFalse(BRWalletManager.getInstance().validateAddress(""))
    }

    @Test
    fun validateAddress_rejectsGarbage() {
        assertFalse(BRWalletManager.getInstance().validateAddress("not-a-real-address"))
    }

    @Test
    fun validateAddress_rejectsBitcoinAddress() {
        // Bitcoin address should be rejected by a Litecoin wallet.
        assertFalse(
            BRWalletManager.getInstance().validateAddress("1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        )
    }

    // -------------------------------------------------------------------------
    // BWSender through the real JNI layer
    // -------------------------------------------------------------------------

    @Test
    fun bwSender_withRealWalletManager_handlesNullTransactionGracefully() = runTest {
        assumeTrue("Skipping — wallet not created on this device", walletManager.isCreated())

        val postAuth = mockk<PostAuth>(relaxed = true)
        val sender = BWSender(
            context = context,
            getWalletManager = { walletManager },
            getPostAuth = { postAuth }
        )

        // Send to an invalid address so the native method returns null rather
        // than throwing — this is the real-world failure mode that previously
        // locked the Send button.
        val tx = TransactionItem(
            "LhK2kQwiaAvhjWY799cZvMyYwnQAcxkarr",
            "LOpsAddrInvalid",
            null,
            /* sendAmount = */
            1L,
            /* opsFee = */
            0L,
            null,
            false,
            ""
        )

        val result = sender.prepareTransaction(tx)

        // Either AmountTooSmall or Unknown is acceptable here — what matters is
        // that isSending is reset and the app is not permanently locked.
        assertNotNull(result)
        assertFalse(
            "isSending must not leak through real-JNI error paths",
            sender.isSending
        )
    }

    @Test
    fun bwSender_reentrancyGuard_holdsAgainstRealWallet() = runTest {
        assumeTrue(walletManager.isCreated())

        val postAuth = mockk<PostAuth>(relaxed = true)
        val sender = BWSender(
            context = context,
            getWalletManager = { walletManager },
            getPostAuth = { postAuth }
        )

        sender.isSending = true
        val result = sender.prepareTransaction(buildDummyTx())

        assert(result is BWSendResult.Error.AlreadySending)
    }

    // -------------------------------------------------------------------------
    // Balance read — driven through getBalance which is used by SendViewModel
    // to decide isAmountBelowBalance. A zero/stale read is the "Send stays
    // disabled" production symptom.
    // -------------------------------------------------------------------------

    @Test
    fun getBalance_returnsNonNegativeValue() {
        val balance = BRWalletManager.getInstance().getBalance(context)
        assertTrue("Balance must never go negative", balance >= 0)
    }

    private fun buildDummyTx() = TransactionItem(
        "LhK2kQwiaAvhjWY799cZvMyYwnQAcxkarr",
        "LOpsAddr",
        null,
        100_000L,
        500L,
        null,
        false,
        ""
    )
}
