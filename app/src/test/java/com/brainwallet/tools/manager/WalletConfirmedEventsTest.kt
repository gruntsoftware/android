package com.brainwallet.tools.manager

import android.content.Context
import android.os.Bundle
import com.brainwallet.constants.BWConstants
import com.grunt.brainwallet.af.AFLauncher
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WalletConfirmedEventsTest {

    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(BRSharedPrefs::class)
        mockkStatic(AnalyticsManager::class)
        mockkObject(AFLauncher)
        mockkConstructor(Bundle::class)
        every { BRSharedPrefs.putPhraseWroteDown(any(), any()) } returns Unit
        every { AnalyticsManager.logCustomAdHocEvent(any(), any()) } returns null
        every { AFLauncher.logEvent(any()) } returns true
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `new wallet confirmed for the first time marks the phrase and reports create`() {
        every { BRSharedPrefs.getPhraseWroteDown(context) } returns false

        val reported = WalletConfirmedEvents.onNewWalletConfirmed(context)

        assertTrue(reported)
        verifyOrder {
            BRSharedPrefs.getPhraseWroteDown(context)
            BRSharedPrefs.putPhraseWroteDown(context, true)
        }
        verify(exactly = 1) { AFLauncher.logEvent(BWConstants.AF_EVENT_E20261115A) }
        verify(exactly = 1) {
            anyConstructed<Bundle>().putString(
                WalletConfirmedEvents.PARAM_COMPLETION,
                WalletConfirmedEvents.COMPLETION_CREATE
            )
        }
        verify(exactly = 1) {
            AnalyticsManager.logCustomAdHocEvent(WalletConfirmedEvents.EVENT_USER_CONFIRMED_WALLET, any())
        }
    }

    @Test
    fun `re-confirming an existing wallet from settings is not reported again`() {
        every { BRSharedPrefs.getPhraseWroteDown(context) } returns true

        val reported = WalletConfirmedEvents.onNewWalletConfirmed(context)

        assertFalse(reported)
        // Still marked as written down, just not reported.
        verify(exactly = 1) { BRSharedPrefs.putPhraseWroteDown(context, true) }
        verify(exactly = 0) { AFLauncher.logEvent(any()) }
        verify(exactly = 0) { AnalyticsManager.logCustomAdHocEvent(any(), any()) }
    }

    @Test
    fun `restored wallet reports restore`() {
        WalletConfirmedEvents.onRestoredWalletConfirmed()

        verify(exactly = 1) { AFLauncher.logEvent(BWConstants.AF_EVENT_E20261115A) }
        verify(exactly = 1) {
            anyConstructed<Bundle>().putString(
                WalletConfirmedEvents.PARAM_COMPLETION,
                WalletConfirmedEvents.COMPLETION_RESTORE
            )
        }
        verify(exactly = 1) {
            AnalyticsManager.logCustomAdHocEvent(WalletConfirmedEvents.EVENT_USER_CONFIRMED_WALLET, any())
        }
        // The restore path marks the phrase itself (PostAuth.onRecoverWalletAuth), before reporting.
        verify(exactly = 0) { BRSharedPrefs.putPhraseWroteDown(any(), any()) }
    }

    @Test
    fun `every restore is reported`() {
        repeat(2) { WalletConfirmedEvents.onRestoredWalletConfirmed() }

        verify(exactly = 2) { AFLauncher.logEvent(BWConstants.AF_EVENT_E20261115A) }
        verify(exactly = 2) {
            AnalyticsManager.logCustomAdHocEvent(WalletConfirmedEvents.EVENT_USER_CONFIRMED_WALLET, any())
        }
    }
}
