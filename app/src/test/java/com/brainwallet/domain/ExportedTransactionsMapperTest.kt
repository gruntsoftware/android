package com.brainwallet.domain

import com.brainwallet.presenter.entities.TxItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportedTransactionsMapperTest {

    private val mapper = ExportedTransactionsMapper()

    private fun txItem(
        timeStamp: Long = 1_700_000_000L,
        blockHeight: Int = 123,
        txReversed: String = "deadbeef",
        sent: Long = 100000L,
        received: Long = 0L,
        fee: Long = 5460L,
        to: Array<String> = arrayOf("LTC1address"),
    ) = TxItem(
        timeStamp,
        blockHeight,
        ByteArray(0),
        txReversed,
        sent,
        received,
        fee,
        to,
        arrayOf("LTC1sender"),
        1_000L,
        250,
        longArrayOf(6000L),
        true,
    )

    @Test
    fun `given an empty list, when invoked, then returns an empty list`() {
        val result = mapper(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `given a single tx item, when invoked, then maps every field across`() {
        val item = txItem()

        val result = mapper(listOf(item))

        assertEquals(1, result.size)
        val exported = result[0]
        assertEquals(item.timeStamp, exported.timeStamp)
        assertEquals(item.blockHeight, exported.blockHeight)
        assertEquals(item.txReversed, exported.txHashReversed)
        assertEquals(item.sent, exported.sent)
        assertEquals(item.received, exported.received)
        assertEquals(item.fee, exported.fee)
        assertEquals(item.to.toList(), exported.to)
    }

    @Test
    fun `given multiple tx items, when invoked, then preserves their order`() {
        val first = txItem(timeStamp = 1L, txReversed = "first")
        val second = txItem(timeStamp = 2L, txReversed = "second")

        val result = mapper(listOf(first, second))

        assertEquals(listOf("first", "second"), result.map { it.txHashReversed })
    }
}
