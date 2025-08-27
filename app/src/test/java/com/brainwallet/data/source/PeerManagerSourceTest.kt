package com.brainwallet.data.source

import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class PeerManagerSourceTest {

    private lateinit var proxy: BRPeerManagerProxy
    private lateinit var peerManagerSource: PeerManagerSource

    @Before
    fun setUp() {
        proxy = mockk()
        peerManagerSource = PeerManagerSource(proxy)
    }

    @Test
    fun `Given proxy returns block height When calling getCurrentBlockHeight Then it should return expected block height`() {
        val expectedHeight = 12345
        every { proxy.getCurrentBlockHeight() } returns expectedHeight

        val actualHeight = peerManagerSource.getCurrentBlockHeight()

        assert(actualHeight == expectedHeight) {
            "Expected block height to be $expectedHeight but was $actualHeight"
        }
    }

    @Test
    fun `Given proxy returns last block timestamp When calling getLastBlockTimestamp Then it should return expected timestamp`() {
        val expectedTimestamp = 1699999999L
        every { proxy.getLastBlockTimestamp() } returns expectedTimestamp

        val actualTimestamp = peerManagerSource.getLastBlockTimestamp()

        assert(actualTimestamp == expectedTimestamp) {
            "Expected last block timestamp to be $expectedTimestamp but was $actualTimestamp"
        }
    }
}
