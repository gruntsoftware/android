package com.brainwallet.wallet

import android.content.Context
import android.security.keystore.UserNotAuthenticatedException
import com.brainwallet.tools.manager.sync.SyncThreadManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.util.TrustedNode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
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
        mockkStatic(BRKeyStore::class)
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
        unmockkStatic(BRKeyStore::class)
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
        // Kept modest (not e.g. 32) deliberately: this spins up real OS threads via a live
        // ExecutorService, and the CI unit-test job already runs under a tight memory ceiling
        // (see .circleci/config.yml's GRADLE_OPTS comment) - enough threads to actually
        // exercise the race without adding meaningful per-run thread/stack overhead.
        val threadCount = 8
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

    // ── resolveTrustedFixedPeer: at launch the peer manager is (re)initialized to sync via
    //    BRWalletManager.initWallet() -> pm.create(...) -> pm.updateFixedPeer(ctx), and
    //    updateFixedPeer() resolves the peer to pin the SPV sync to from the user's
    //    peer-sync-mode preference + trusted node in the Keystore. These cover that
    //    resolution directly; the setFixedPeer()/connect() calls it feeds are native and
    //    can't run in a plain JVM unit test (UnsatisfiedLinkError). ─────────────────────

    /** Stubs the three Keystore reads [BRPeerManager.resolveTrustedFixedPeer] makes. */
    private fun stubTrustedNodeKeystore(
        ctx: Context,
        prefersTrustedNode: Boolean,
        host: String?,
        port: Int,
    ) {
        every { BRKeyStore.getTrustedNodeSyncPreference(ctx, 0) } returns prefersTrustedNode
        every { BRKeyStore.getTrustedNodeIPAddress(ctx, 0) } returns host
        every { BRKeyStore.getTrustedNodePort(ctx, 0) } returns port
    }

    @Test
    fun `resolveTrustedFixedPeer uses the stored trusted node host and port when trusted-node mode is on`() {
        val ctx = mockk<Context>(relaxed = true)
        stubTrustedNodeKeystore(ctx, prefersTrustedNode = true, host = "192.168.1.50", port = 9335)

        val peer = BRPeerManager.resolveTrustedFixedPeer(ctx)

        assertTrue(peer.isSet, "a stored trusted node in trusted-node mode should be a set fixed peer")
        assertEquals("192.168.1.50", peer.host)
        assertEquals(9335, peer.port)
    }

    @Test
    fun `resolveTrustedFixedPeer falls back to the standard LTC port when only a trusted host is stored`() {
        val ctx = mockk<Context>(relaxed = true)
        // 0 == "unset" (e.g. a host saved before the port had its own Keystore field)
        stubTrustedNodeKeystore(ctx, prefersTrustedNode = true, host = "10.0.0.9", port = 0)

        val peer = BRPeerManager.resolveTrustedFixedPeer(ctx)

        assertTrue(peer.isSet)
        assertEquals("10.0.0.9", peer.host)
        assertEquals(TrustedNode.STANDARD_PORT, peer.port)
    }

    @Test
    fun `resolveTrustedFixedPeer reports no fixed peer when Litecoin mainnet mode is selected even if a trusted node is still stored`() {
        val ctx = mockk<Context>(relaxed = true)
        // User toggled the peer sync mode to "Litecoin mainnet": the preference is false,
        // but a trusted node address/port from before is still in the Keystore.
        stubTrustedNodeKeystore(ctx, prefersTrustedNode = false, host = "192.168.1.50", port = 9335)

        val peer = BRPeerManager.resolveTrustedFixedPeer(ctx)

        // Empty host -> updateFixedPeer() clears the pinned peer so the sync stops and
        // restarts against the random mainnet peer array.
        assertFalse(peer.isSet, "Litecoin mainnet mode must not pin the stored trusted node")
        assertEquals("", peer.host)
        assertEquals(0, peer.port)
        // The stored address must not even be read when the user isn't on trusted-node mode.
        verify(exactly = 0) { BRKeyStore.getTrustedNodeIPAddress(ctx, 0) }
    }

    @Test
    fun `resolveTrustedFixedPeer reports no fixed peer when trusted-node mode is on but no address is stored yet`() {
        val ctx = mockk<Context>(relaxed = true)
        stubTrustedNodeKeystore(ctx, prefersTrustedNode = true, host = null, port = 0)

        val peer = BRPeerManager.resolveTrustedFixedPeer(ctx)

        assertFalse(peer.isSet, "no stored address should mean no fixed peer")
        assertEquals("", peer.host)
        assertEquals(0, peer.port)
    }

    @Test
    fun `resolveTrustedFixedPeer reports no fixed peer when the keystore read is not authenticated`() {
        val ctx = mockk<Context>(relaxed = true)
        every { BRKeyStore.getTrustedNodeSyncPreference(ctx, 0) } throws UserNotAuthenticatedException()

        val peer = BRPeerManager.resolveTrustedFixedPeer(ctx)

        assertFalse(peer.isSet)
        assertEquals("", peer.host)
        assertEquals(0, peer.port)
    }

    @Test
    fun `updateFixedPeer stops the current sync and re-resolves the fixed peer before restarting`() {
        // Covers the peer-sync-mode toggle in both directions: switching to the trusted peer
        // (or to Litecoin mainnet) must stop the running sync, re-fetch the fixed peer from
        // the Keystore, then restart. stopSyncing() is the explicit "stop"; the restart is
        // setFixedPeer()/connect() (native - they throw UnsatisfiedLinkError here, so the
        // call is wrapped and we assert the stop + re-resolve that precede them).
        val ctx = mockk<Context>(relaxed = true)
        val syncThreadManager = mockk<SyncThreadManager>(relaxed = true)
        mockkStatic(BRPeerManager::class)
        // getInstance() is @JvmStatic on the companion - mockkObject(Companion) covers both
        // the Java static forwarder BRPeerManager.java calls and the Kotlin companion method.
        mockkObject(SyncThreadManager.Companion)
        try {
            // mockkStatic stubs every static on the class - keep the singleton accessor real.
            every { BRPeerManager.getInstance() } answers { callOriginal() }
            every { BRPeerManager.resolveTrustedFixedPeer(ctx) } returns
                BRPeerManager.TrustedFixedPeer("192.168.1.50", 9335)
            every { SyncThreadManager.getInstance() } returns syncThreadManager

            runCatching { BRPeerManager.getInstance().updateFixedPeer(ctx) }

            verifyOrder {
                BRPeerManager.resolveTrustedFixedPeer(ctx)
                syncThreadManager.stopSyncing()
            }
        } finally {
            unmockkObject(SyncThreadManager.Companion)
            unmockkStatic(BRPeerManager::class)
        }
    }
}
