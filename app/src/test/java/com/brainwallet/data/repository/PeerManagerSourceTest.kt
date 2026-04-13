package com.brainwallet.data.source

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PeerManagerSourceTest {

    private lateinit var proxy: BRPeerManagerProxy
    private lateinit var peerManagerSource: PeerManagerSourceImpl

    @Before
    fun setUp() {
        proxy = mockk()
        every { proxy.getCurrentBlockHeight() } returns 0
        every { proxy.getLastBlockTimestamp() } returns 0L
        every { proxy.getSyncProgress() } returns 0.0
    }

    @Test
    fun `Given proxy returns block height When calling getCurrentBlockHeight Then it should return expected block height`() {
        val expectedHeight = 12345
        every { proxy.getCurrentBlockHeight() } returns expectedHeight

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        val actual = peerManagerSource.getCurrentBlockHeight()
        assertEquals(expectedHeight, actual)
    }

    @Test
    fun `Given proxy returns last block timestamp When calling getLastBlockTimestamp Then it should return expected timestamp`() {
        val expectedTimestamp = 1699999999L
        every { proxy.getLastBlockTimestamp() } returns expectedTimestamp

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        val actual = peerManagerSource.getLastBlockTimestamp()
        assertEquals(expectedTimestamp, actual)
    }

    @Test
    fun `Given proxy returns sync progress When calling getSyncProgress Then it should return expected progress`() {
        val expectedProgress = 0.75
        every { proxy.getSyncProgress() } returns expectedProgress

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        val actual = peerManagerSource.getSyncProgress()
        assertEquals(expectedProgress, actual, 0.0001)
    }

    @Test
    fun `Given proxy values When initialising Then blockInfo StateFlow should emit initial values`() = runTest {
        every { proxy.getCurrentBlockHeight() } returns 800_000
        every { proxy.getLastBlockTimestamp() } returns 1700000000L
        every { proxy.getSyncProgress() } returns 1.0

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        peerManagerSource.blockInfo.test {
            val emission = awaitItem()
            assertEquals(800_000, emission.blockHeight)
            assertEquals(1700000000L, emission.timestamp)
            assertEquals(1.0f, emission.syncProgress, 0.0001f)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given proxy values change When polling delay elapses Then blockInfo StateFlow should emit updated values`() = runTest {
        var callCount = 0
        every { proxy.getCurrentBlockHeight() } answers { if (callCount++ < 1) 800_000 else 800_001 }
        every { proxy.getLastBlockTimestamp() } returns 1700000000L
        every { proxy.getSyncProgress() } returns 1.0

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        peerManagerSource.blockInfo.test {
            val initial = awaitItem()
            assertEquals(800_000, initial.blockHeight)

            advanceTimeBy(350L) // past the 300ms poll delay

            val updated = awaitItem()
            assertEquals(800_001, updated.blockHeight)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Given proxy is delegated When calling proxy methods Then proxy is invoked`() {
        every { proxy.getCurrentBlockHeight() } returns 12345
        every { proxy.getLastBlockTimestamp() } returns 9999L
        every { proxy.getSyncProgress() } returns 0.5

        peerManagerSource = PeerManagerSourceImpl(app = mockk(), proxy = proxy)

        peerManagerSource.getCurrentBlockHeight()
        peerManagerSource.getLastBlockTimestamp()
        peerManagerSource.getSyncProgress()

        verify(exactly = 2) { proxy.getCurrentBlockHeight() } // once in init, once in call
        verify(exactly = 2) { proxy.getLastBlockTimestamp() }
        verify(exactly = 2) { proxy.getSyncProgress() }
    }
}
