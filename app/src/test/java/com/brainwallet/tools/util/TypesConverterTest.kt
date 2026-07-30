package com.brainwallet.tools.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class TypesConverterTest {

    @Test
    fun `given positive int, when intToBytes then bytesToInt, then round trips exactly`() {
        val value = 123456789
        assertEquals(value, TypesConverter.bytesToInt(TypesConverter.intToBytes(value)))
    }

    @Test
    fun `given negative int, when intToBytes then bytesToInt, then round trips exactly`() {
        val value = -42
        assertEquals(value, TypesConverter.bytesToInt(TypesConverter.intToBytes(value)))
    }

    @Test
    fun `given zero int, when intToBytes, then returns four zero bytes`() {
        assertArrayEquals(byteArrayOf(0, 0, 0, 0), TypesConverter.intToBytes(0))
    }

    @Test
    fun `given positive long, when long2byteArray then byteArray2long, then round trips exactly`() {
        val value = 9876543210123L
        assertEquals(value, TypesConverter.byteArray2long(TypesConverter.long2byteArray(value)))
    }

    @Test
    fun `given negative long, when long2byteArray then byteArray2long, then round trips exactly`() {
        val value = -1L
        assertEquals(value, TypesConverter.byteArray2long(TypesConverter.long2byteArray(value)))
    }

    @Test
    fun `given char array, when toBytes then toChars, then values match as unsigned bytes`() {
        val chars = charArrayOf('a', 'B', 'z', '1')
        val bytes = TypesConverter.toBytes(chars)
        val roundTripped = TypesConverter.toChars(bytes)

        assertArrayEquals(chars, roundTripped)
    }

    @Test
    fun `given mixed case char array, when lowerCaseCharArray, then all chars are lower case`() {
        val chars = charArrayOf('A', 'b', 'C', '1', 'D')
        assertArrayEquals(charArrayOf('a', 'b', 'c', '1', 'd'), TypesConverter.lowerCaseCharArray(chars))
    }

    @Test
    fun `given char array, when charsToBytes, then matches UTF-8 encoded bytes`() {
        val chars = "hello".toCharArray()
        assertArrayEquals("hello".toByteArray(Charsets.UTF_8), TypesConverter.charsToBytes(chars))
    }

    @Test
    fun `given seed bytes, when getNullTerminatedPhrase, then appends zero byte and zeroes original`() {
        val rawSeed = byteArrayOf(1, 2, 3, 4)
        val result = TypesConverter.getNullTerminatedPhrase(rawSeed)

        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 0), result)
        // The original array must be wiped for security once copied.
        assertArrayEquals(byteArrayOf(0, 0, 0, 0), rawSeed)
    }
}
