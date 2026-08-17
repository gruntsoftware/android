package com.brainwallet.wallet

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.brainwallet.constants.BWConstants
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.BRKeyStore
import io.mockk.Awaits
import io.mockk.every
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented counterpart to two cases moved out of BRWalleManagerTest (JVM
 * unit test): the guarded branch of [BRWalletManager.initWallet] calls
 * [BRWalletManager.isCreated], a native method with no JVM fallback. In a
 * plain JVM unit test that always throws UnsatisfiedLinkError before reaching
 * either the pubkey-null check or a second sequential call, so those tests
 * could never actually exercise the behavior they claimed to verify. Real
 * native library required here, same tier as [com.brainwallet.ui.screens.send.SendInstrumentationTest] --
 * not run in the CI unit-test job.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BRWalletManagerInstrumentationTest {

    private lateinit var context: Context
    private lateinit var manager: BRWalletManager

    @Before
    fun setUp() {
        System.loadLibrary("core-lib")
        context = ApplicationProvider.getApplicationContext()
        manager = BRWalletManager.getInstance()

        mockkStatic(AnalyticsManager::class)
        every { AnalyticsManager.logCustomEvent(any()) } just Awaits
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun initWallet_returns_early_when_pubkey_is_null() {
        // Only reachable when the wallet hasn't been created yet on this device --
        // otherwise initWallet() short-circuits before the pubkey check entirely,
        // same as production.
        assumeTrue("Skipping — wallet already created on this device", !manager.isCreated())

        mockkStatic(BRKeyStore::class)
        every { BRKeyStore.getMasterPublicKey(any()) } returns null

        val firstAddressBefore = BRSharedPrefs.getFirstAddress(context)

        manager.initWallet(context)

        // putFirstAddress is only reached after createWallet succeeds — with a
        // null pubkey it must never run, so the stored value is untouched.
        assertEquals(firstAddressBefore, BRSharedPrefs.getFirstAddress(context))
    }

    @Test
    fun initWallet_sequential_second_call_is_not_blocked_by_guard() {
        // Two sequential (non-overlapping) calls on the same thread -- the
        // isInitiatingWallet guard resets in initWallet()'s finally block after
        // each call, so the second call must not see it still held.
        manager.initWallet(context)
        manager.initWallet(context)

        verify(exactly = 0) { AnalyticsManager.logCustomEvent(BWConstants._20200111_WNI) }
    }
}
