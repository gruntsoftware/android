package com.brainwallet.wallet

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Covers [BRPeerManager.onIntegrityWarning], the Java-side half of the native integrity-warning
 * path added alongside the NULL-`lastBlock` guards in core's `BRPeerManager.c`
 * (`BRPeerManagerSetIntegrityWarningCallback`). The native guard itself lives in the `core`
 * submodule and isn't covered by this JVM suite.
 */
class BRPeerManagerTest {

    private val mockCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
    }

    @Test
    fun `onIntegrityWarning reports a RuntimeException to Crashlytics`() {
        BRPeerManager.onIntegrityWarning("BRPeerManagerLastBlockHeight: lastBlock is NULL")

        val captured = slot<Throwable>()
        verify(exactly = 1) { mockCrashlytics.recordException(capture(captured)) }
        assertTrue(captured.captured is RuntimeException)
    }

    @Test
    fun `onIntegrityWarning includes the native warning text verbatim in the recorded exception message`() {
        val warning = "BRPeerManagerRescan: checkpoint block missing from block set, lastBlock left unchanged"

        BRPeerManager.onIntegrityWarning(warning)

        val captured = slot<Throwable>()
        verify { mockCrashlytics.recordException(capture(captured)) }
        assertEquals("BRPeerManager native integrity warning: $warning", captured.captured.message)
    }

    @Test
    fun `onIntegrityWarning records once per call for each distinct native warning site`() {
        // Mirrors the four call sites added in core's BRPeerManager.c
        listOf(
            "BRPeerManagerRescan: checkpoint block missing from block set, lastBlock left unchanged",
            "BRPeerManagerEstimatedBlockHeight: lastBlock is NULL",
            "BRPeerManagerLastBlockHeight: lastBlock is NULL",
            "BRPeerManagerLastBlockTimestamp: lastBlock is NULL",
        ).forEach { BRPeerManager.onIntegrityWarning(it) }

        verify(exactly = 4) { mockCrashlytics.recordException(any()) }
    }

    @Test
    fun `onIntegrityWarning does not throw when warning is null`() {
        // Defensive: the native side always passes a short static string, but the JNI bridge
        // (PeerManager.c's integrityWarning()) technically permits a null jstring — this must
        // never crash the calling native thread.
        BRPeerManager.onIntegrityWarning(null)

        verify(exactly = 1) { mockCrashlytics.recordException(any()) }
    }
}
