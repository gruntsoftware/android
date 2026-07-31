package com.brainwallet.tools.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class Base58Test {

    @Test
    fun `given empty byte array, when encode, then returns empty string`() {
        assertEquals("", Base58.encode(ByteArray(0)))
    }

    @Test
    fun `given empty string, when decode, then returns empty byte array`() {
        assertArrayEquals(ByteArray(0), Base58.decode(""))
    }

    @Test
    fun `given known byte sequence, when encode, then matches known base58 vector`() {
        // "Hello World" in bytes is a well known Base58 test vector.
        val input = "Hello World".toByteArray(Charsets.UTF_8)
        assertEquals("JxF12TrwUP45BMd", Base58.encode(input))
    }

    @Test
    fun `given known base58 string, when decode, then matches known byte vector`() {
        val decoded = Base58.decode("JxF12TrwUP45BMd")
        assertArrayEquals("Hello World".toByteArray(Charsets.UTF_8), decoded)
    }

    @Test
    fun `given bytes with leading zeroes, when encode then decode, then round trips exactly`() {
        val input = byteArrayOf(0, 0, 0, 1, 2, 3, 4, 5)
        val encoded = Base58.encode(input)

        // Leading zero bytes are represented as leading '1' characters.
        assertEquals('1', encoded[0])
        assertEquals('1', encoded[1])
        assertEquals('1', encoded[2])

        assertArrayEquals(input, Base58.decode(encoded))
    }

    @Test
    fun `given all zero bytes, when encode then decode, then round trips exactly`() {
        val input = ByteArray(5)
        val encoded = Base58.encode(input)

        assertEquals("11111", encoded)
        assertArrayEquals(input, Base58.decode(encoded))
    }

    @Test
    fun `given arbitrary byte array, when encode then decode, then round trips exactly`() {
        val input = byteArrayOf(
            0x00,
            0x01.toByte(),
            0xFF.toByte(),
            0x7F,
            0x80.toByte(),
            0x10,
            0x2A
        )
        val encoded = Base58.encode(input)
        val decoded = Base58.decode(encoded)

        assertArrayEquals(input, decoded)
    }

    @Test
    fun `given string with invalid base58 character, when decode, then throws RuntimeException`() {
        // '0', 'O', 'I', 'l' are excluded from the Base58 alphabet.
        assertThrows(RuntimeException::class.java) {
            Base58.decode("0OIl")
        }
    }
}
