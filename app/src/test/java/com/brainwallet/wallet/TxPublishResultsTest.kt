package com.brainwallet.wallet

import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class TxPublishResultsTest {

    private val hashA = ByteArray(32) { it.toByte() }
    private val hashB = ByteArray(32) { (it + 100).toByte() }

    @Before
    fun setUp() = TxPublishResults.reset()

    @After
    fun tearDown() = TxPublishResults.reset()

    @Test
    fun `result arriving after await completes the waiter`() = runTest {
        val result = async { TxPublishResults.await(hashA) }
        advanceUntilIdle()
        assertFalse(result.isCompleted)

        TxPublishResults.onPublishResult(hashA, TxPublishResults.SUCCESS)

        assertEquals(TxPublishResults.SUCCESS, result.await())
    }

    @Test
    fun `result arriving before await is buffered`() = runTest {
        // e.g. ENOTCONN is reported synchronously inside BRPeerManagerPublishTx
        TxPublishResults.onPublishResult(hashA, 107)

        assertEquals(107, TxPublishResults.await(hashA))
    }

    @Test
    fun `results for other transactions do not complete the waiter`() = runTest {
        val result = async { TxPublishResults.await(hashA) }
        advanceUntilIdle()

        TxPublishResults.onPublishResult(hashB, TxPublishResults.SUCCESS)
        advanceUntilIdle()
        assertFalse(result.isCompleted)

        TxPublishResults.onPublishResult(hashA, 110)
        assertEquals(110, result.await())
    }

    @Test
    fun `null hash is ignored`() = runTest {
        TxPublishResults.onPublishResult(null, TxPublishResults.SUCCESS)
        TxPublishResults.onPublishResult(hashA, 22)

        assertEquals(22, TxPublishResults.await(hashA))
    }
}
