package com.brainwallet.tools.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

class BytesUtilTest {

    @Test
    fun `given empty stream, when readBytesFromStream, then returns empty byte array`() {
        val result = BytesUtil.readBytesFromStream(ByteArrayInputStream(ByteArray(0)))
        assertArrayEquals(ByteArray(0), result)
    }

    @Test
    fun `given small stream, when readBytesFromStream, then returns all bytes`() {
        val data = "hello world".toByteArray(Charsets.UTF_8)
        val result = BytesUtil.readBytesFromStream(ByteArrayInputStream(data))
        assertArrayEquals(data, result)
    }

    @Test
    fun `given stream larger than internal buffer, when readBytesFromStream, then returns all bytes`() {
        // Internal buffer is 1024 bytes, so exercise multiple read iterations.
        val data = ByteArray(1024 * 3 + 17) { (it % 256).toByte() }
        val result = BytesUtil.readBytesFromStream(ByteArrayInputStream(data))
        assertArrayEquals(data, result)
    }

    @Test
    fun `given stream that throws IOException, when readBytesFromStream, then returns bytes read so far without throwing`() {
        val failingStream =
            object : InputStream() {
                private var callCount = 0

                override fun read(): Int = throw UnsupportedOperationException("not used")

                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int {
                    callCount++
                    if (callCount == 1) {
                        b[off] = 42
                        return 1
                    }
                    throw IOException("boom")
                }
            }

        val result = BytesUtil.readBytesFromStream(failingStream)
        assertEquals(1, result.size)
        assertEquals(42.toByte(), result[0])
    }
}
