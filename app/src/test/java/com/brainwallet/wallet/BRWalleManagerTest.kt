package com.brainwallet.wallet

import android.content.Context
import com.brainwallet.constants.BWConstants
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.sqlite.TransactionDataSource
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.Runs
import io.mockk.Awaits
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class BRWalletManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)

        mockkStatic(BRSharedPrefs::class)
        mockkStatic(BRKeyStore::class)
        mockkStatic(AnalyticsManager::class)
        mockkStatic(TransactionDataSource::class)

        // Safe defaults so every test starts with a consistent baseline
        every { AnalyticsManager.logCustomEvent(any()) } just Awaits
        every { BRSharedPrefs.getFalsePositivesRate(any()) } returns 0.0f
        every { BRSharedPrefs.getStartHeight(any()) } returns 0
        every { BRSharedPrefs.putStartHeight(any(), any()) } just Runs
        every { BRSharedPrefs.putFirstAddress(any(), any()) } just Runs
        every { BRSharedPrefs.putCachedBalance(any(), any()) } just Runs
        every { BRSharedPrefs.putReceiveAddress(any(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
        // Reset the Java singleton between tests
        val field = BRWalletManager::class.java.getDeclaredField("instance")
        field.isAccessible = true
        field.set(null, null)
    }

    // ── getInstance ────────────────────────────────────────────────────────

    @Test
    fun `getInstance returns same instance on repeated calls`() {
        val first = BRWalletManager.getInstance()
        val second = BRWalletManager.getInstance()
        assertTrue(first === second)
    }

    // ── isInitiatingWallet guard ───────────────────────────────────────────

    @Test
    fun `initWallet sequential second call is not blocked by guard`() {
        // Arrange — pubkey null causes early return without doing any native work
        val mockDs = mockk<TransactionDataSource>(relaxed = true)
        every { TransactionDataSource.getInstance(any()) } returns mockDs
        every { mockDs.getAllTransactions() } returns emptyList()
        every { BRKeyStore.getMasterPublicKey(any()) } returns null

        val manager = BRWalletManager.getInstance()

        // Act — two sequential calls on a background-like thread (not main)
        val thread = Thread {
            manager.initWallet(context)
            manager.initWallet(context)
        }
        thread.start()
        thread.join(3_000)

        // Assert — guard WNI event never fired because calls were sequential
        verify(exactly = 0) { AnalyticsManager.logCustomEvent(BWConstants._20200111_WNI) }
    }

    // ── initWallet throws on main thread ───────────────────────────────────

    @Test(expected = android.os.NetworkOnMainThreadException::class)
    fun `initWallet throws NetworkOnMainThreadException when called on main thread`() {
        // ActivityUTILS.isMainThread() — mock it to simulate main thread
        mockkStatic(com.brainwallet.presenter.activities.util.ActivityUTILS::class)
        every { com.brainwallet.presenter.activities.util.ActivityUTILS.isMainThread() } returns true

        BRWalletManager.getInstance().initWallet(context)
    }

    // ── initWallet early exits ─────────────────────────────────────────────

    @Test
    fun `initWallet returns early when pubkey is null`() {
        val mockDs = mockk<TransactionDataSource>(relaxed = true)
        every { TransactionDataSource.getInstance(any()) } returns mockDs
        every { mockDs.getAllTransactions() } returns emptyList()
        every { BRKeyStore.getMasterPublicKey(any()) } returns null

        val thread = Thread { BRWalletManager.getInstance().initWallet(context) }
        thread.start()
        thread.join(3_000)

        // putFirstAddress is only reached after createWallet succeeds — it must not be called
        verify(exactly = 0) { BRSharedPrefs.putFirstAddress(any(), any()) }
    }
}
