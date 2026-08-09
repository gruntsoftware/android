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
import java.lang.reflect.Modifier
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Covers [BRPeerManager.onIntegrityWarning], the Java-side half of the native integrity-warning
 * path added alongside the NULL-`lastBlock` guards in core's `BRPeerManager.c`
 * (`BRPeerManagerSetIntegrityWarningCallback`). The native guard itself lives in the `core`
 * submodule and isn't covered by this JVM suite.
 *
 * Also covers the singleton/synchronization shape of [BRPeerManager] itself, added as a
 * regression guard for Crashlytics issue `85c581edcdb39b941df627e7b1324a71`: a FORTIFY
 * `pthread_mutex_lock called on a destroyed mutex` SIGABRT caused by `connect()` and
 * `peerManagerFreeEverything()` racing each other on the shared native peer manager with no
 * synchronization. The native methods themselves can't be invoked from a plain JVM unit test
 * (no native library is loaded here - calling one throws `UnsatisfiedLinkError`), so these
 * checks assert the structural properties the fix depends on instead: exactly one singleton
 * instance, and every native method that touches the shared native pointer declared as a
 * `synchronized` instance method so the JVM serializes access to it.
 */
class BRPeerManagerTest {

    // Native methods that read/write the shared native BRPeerManager* and must stay
    // non-static + synchronized so getInstance()'s monitor serializes every caller.
    private val nativePeerManagerMethods = listOf(
        "getCurrentPeerName", "create", "connect", "putPeer", "createPeerArrayWithCount",
        "putBlock", "createBlockArrayWithCount", "syncProgress", "getCurrentBlockHeight",
        "getRelayCount", "setFixedPeer", "getEstimatedBlockHeight", "isCreated",
        "isConnected", "peerManagerFreeEverything", "getLastBlockTimestamp", "rescan"
    )

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

    @Test
    fun `getInstance returns the same instance on repeated calls`() {
        assertSame(BRPeerManager.getInstance(), BRPeerManager.getInstance())
    }

    @Test
    fun `getInstance returns the same instance when first called concurrently from many threads`() {
        val threadCount = 32
        val readyLatch = CountDownLatch(threadCount)
        val startLatch = CountDownLatch(1)
        val doneLatch = CountDownLatch(threadCount)
        val instances = CopyOnWriteArrayList<BRPeerManager>()
        val executor = Executors.newFixedThreadPool(threadCount)

        try {
            repeat(threadCount) {
                executor.execute {
                    readyLatch.countDown()
                    startLatch.await()
                    instances.add(BRPeerManager.getInstance())
                    doneLatch.countDown()
                }
            }

            assertTrue(readyLatch.await(5, TimeUnit.SECONDS), "threads failed to start in time")
            startLatch.countDown() // release every thread to call getInstance() at once
            assertTrue(doneLatch.await(5, TimeUnit.SECONDS), "threads failed to finish in time")
        } finally {
            executor.shutdown()
        }

        assertEquals(threadCount, instances.size)
        val first = instances.first()
        assertTrue(instances.all { it === first }, "expected every thread to observe the same singleton instance")
    }

    @Test
    fun `BRPeerManager cannot be subclassed`() {
        assertTrue(
            Modifier.isFinal(BRPeerManager::class.java.modifiers),
            "BRPeerManager should be final so it can't bypass the singleton via subclassing"
        )
    }

    @Test
    fun `BRPeerManager constructor is private`() {
        val constructor = BRPeerManager::class.java.getDeclaredConstructor()
        assertTrue(
            Modifier.isPrivate(constructor.modifiers),
            "BRPeerManager's constructor should be private so callers can only reach it via getInstance()"
        )
    }

    @Test
    fun `native methods touching the shared peer manager are instance methods, not static`() {
        nativePeerManagerMethods.forEach { name ->
            val method = BRPeerManager::class.java.declaredMethods.first { it.name == name }
            assertFalse(
                Modifier.isStatic(method.modifiers),
                "$name should be an instance method so it locks on the shared singleton's monitor"
            )
        }
    }

    @Test
    fun `native methods touching the shared peer manager are synchronized`() {
        nativePeerManagerMethods.forEach { name ->
            val method = BRPeerManager::class.java.declaredMethods.first { it.name == name }
            assertTrue(
                Modifier.isSynchronized(method.modifiers),
                "$name should be synchronized to prevent it racing peerManagerFreeEverything()"
            )
        }
    }
}
