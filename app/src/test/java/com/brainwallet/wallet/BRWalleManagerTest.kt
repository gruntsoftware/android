package com.brainwallet.wallet

import android.content.Context
import com.brainwallet.presenter.activities.util.ActivityUTILS
import com.brainwallet.tools.manager.BRSharedPrefs
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BRWalletManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)

        mockkStatic(ActivityUTILS::class)

        // ActivityUTILS.isMainThread() must be stubbed here: with
        // unitTests.isReturnDefaultValues = true (app/build.gradle.kts), the real
        // Looper.myLooper()/getMainLooper() stubs both return null in a plain JVM
        // unit test, so `myLooper() == getMainLooper()` is true unconditionally --
        // even inside an explicit background Thread. Without this default,
        // initWallet() throws NetworkOnMainThreadException before reaching any of
        // the logic under test, and since that throw happens on a child Thread the
        // test doesn't fail -- it just silently never exercises the intended code
        // path. See the "on main thread" test below for the one case that wants
        // the opposite behavior.
        every { ActivityUTILS.isMainThread() } returns false
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

    // ── isInitiatingWallet guard, initWallet early exits ────────────────────
    //
    // Both moved to BRWalletManagerInstrumentationTest (androidTest): the guarded
    // code path in initWallet() calls BRWalletManager.isCreated(), a native
    // method with no JVM fallback. In a plain JVM unit test that always throws
    // UnsatisfiedLinkError before reaching either the pubkey-null check or a
    // second sequential call, regardless of what's stubbed here -- so these
    // couldn't actually exercise the behavior they were meant to verify. See
    // BRPeerManagerTest's class doc for the same limitation on the peer-manager
    // side.

    // ── initWallet throws on main thread ───────────────────────────────────

    @Test(expected = android.os.NetworkOnMainThreadException::class)
    fun `initWallet throws NetworkOnMainThreadException when called on main thread`() {
        // Override setUp()'s default to simulate being on the main thread.
        every { ActivityUTILS.isMainThread() } returns true

        BRWalletManager.getInstance().initWallet(context)
    }

    // ── balance listener dispatch ──────────────────────────────────────────

    // Same ConcurrentModificationException class as Crashlytics 726fa3edec67b0732e6f9659f9dd8eff
    // (BRPeerManager): setBalance() iterates the listener list that screens add to / remove from.
    @Test
    fun `setBalance tolerates listeners being added and removed during dispatch`() {
        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.putCachedBalance(any(), any()) } just Runs
        mockkStatic(BRWalletManager::class)
        every { BRWalletManager.refreshAddress(any()) } returns true
        val manager = BRWalletManager.getInstance()
        val late = mockk<BRWalletManager.OnBalanceChanged>(relaxed = true)
        val calls = mutableListOf<String>()
        val first = object : BRWalletManager.OnBalanceChanged {
            override fun onBalanceChanged(balance: Long) {
                calls += "first:$balance"
                manager.removeListener(this)
                manager.addBalanceChangedListener(late)
            }
        }
        val second = object : BRWalletManager.OnBalanceChanged {
            override fun onBalanceChanged(balance: Long) {
                calls += "second:$balance"
            }
        }
        manager.addBalanceChangedListener(first)
        manager.addBalanceChangedListener(second)

        manager.setBalance(context, 1L)

        assertEquals(listOf("first:1", "second:1"), calls)
        verify(exactly = 0) { late.onBalanceChanged(any()) }

        manager.setBalance(context, 2L)

        assertEquals(listOf("first:1", "second:1", "second:2"), calls)
        verify(exactly = 1) { late.onBalanceChanged(2L) }
    }
}
