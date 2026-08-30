package com.brainwallet.tools.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CryptoHelperTest {

    private fun hexToBytes(hex: String): ByteArray =
        ByteArray(hex.length / 2) { i ->
            hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }

    @Test
    fun `given known input, when sha256, then matches known digest`() {
        val digest = CryptoHelper.sha256("hellobrainwallet".toByteArray(Charsets.UTF_8))

        assertArrayEquals(
            hexToBytes("250c7aea0eaa096ffbeb5becc11847c9bf2b1330d8eb1ad90bfff85e17f74e8e"),
            digest
        )
    }

    @Test
    fun `given empty input, when sha256, then matches known digest`() {
        val digest = CryptoHelper.sha256(ByteArray(0))

        assertArrayEquals(
            hexToBytes("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
            digest
        )
    }

    @Test
    fun `given known input, when doubleSha256, then matches sha256 applied twice`() {
        val digest = CryptoHelper.doubleSha256("hello".toByteArray(Charsets.UTF_8))

        assertArrayEquals(
            hexToBytes("9595c9df90075148eb06860365df33584b75bff782a510c6cd4883a419833d50"),
            digest
        )
    }

    @Test
    fun `given input, when doubleSha256, then equals sha256 of sha256`() {
        val data = "brainwallet".toByteArray(Charsets.UTF_8)

        val expected = CryptoHelper.sha256(CryptoHelper.sha256(data))
        val actual = CryptoHelper.doubleSha256(data)

        assertArrayEquals(expected, actual)
    }

    @Test
    fun `given known input, when md5, then matches known digest`() {
        val digest = CryptoHelper.md5("hello".toByteArray(Charsets.UTF_8))

        assertArrayEquals(hexToBytes("5d41402abc4b2a76b9719d911017c592"), digest)
    }

    @Test
    fun `given known input, when base58ofSha256, then matches known base58 vector`() {
        val encoded = CryptoHelper.base58ofSha256("hello".toByteArray(Charsets.UTF_8))

        assertEquals("42TEXg1vFAbcJ65y7qdYG9iCPvYfy3NDdVLd75akX2P5", encoded)
    }

    @Test
    fun `given empty input, when base58ofSha256, then round trips through Base58 decode`() {
        val encoded = CryptoHelper.base58ofSha256(ByteArray(0))

        assertNotNull(encoded)
        assertArrayEquals(CryptoHelper.sha256(ByteArray(0)), Base58.decode(encoded))
    }
}
