package com.brainwallet.tools.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BRCompressorTest {

    @Test
    fun `given data, when gZipCompress then gZipExtract, then round trips exactly`() {
        val data = "The quick brown fox jumps over the lazy dog".repeat(20).toByteArray(Charsets.UTF_8)

        val compressed = BRCompressor.gZipCompress(data)
        val extracted = BRCompressor.gZipExtract(compressed)

        assertArrayEquals(data, extracted)
    }

    @Test
    fun `given null data, when gZipCompress, then returns null`() {
        assertNull(BRCompressor.gZipCompress(null))
    }

    @Test
    fun `given null or empty compressed data, when gZipExtract, then returns null`() {
        assertNull(BRCompressor.gZipExtract(null))
        assertNull(BRCompressor.gZipExtract(ByteArray(0)))
    }

    @Test
    fun `given data, when bz2Compress then bz2Extract, then round trips exactly`() {
        val data = "The quick brown fox jumps over the lazy dog".repeat(20).toByteArray(Charsets.UTF_8)

        val compressed = BRCompressor.bz2Compress(data)
        val extracted = BRCompressor.bz2Extract(compressed)

        assertArrayEquals(data, extracted)
    }

    @Test
    fun `given null or empty compressed data, when bz2Extract, then returns null`() {
        assertNull(BRCompressor.bz2Extract(null))
        assertNull(BRCompressor.bz2Extract(ByteArray(0)))
    }

    @Test
    fun `given gzip compressed bytes, when isGZIPStream, then returns true`() {
        val compressed = BRCompressor.gZipCompress("payload".toByteArray(Charsets.UTF_8))
        assertTrue(BRCompressor.isGZIPStream(compressed!!))
    }

    @Test
    fun `given non gzip bytes, when isGZIPStream, then returns false`() {
        assertFalse(BRCompressor.isGZIPStream(byteArrayOf(0x01, 0x02)))
    }
}
