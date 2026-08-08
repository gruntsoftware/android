package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.crypto.Cipher

/**
 * Covers the fix for Utils.getEncryptedAgentString()'s public key parsing.
 *
 * The key provisioned in service-data.json's `agent-base64-pubkey` is a base64-encoded
 * OpenSSH "ssh-rsa <base64-blob> <comment>" line, not base64-encoded PEM. The previous
 * implementation stripped whitespace before re-decoding, which merged the "ssh-rsa" prefix
 * and the trailing email comment into the base64 payload and made it undecodable
 * (surfacing as "ERROR-CANNOT-KEYBYTES-DO-CONVERSION").
 */
class UtilsAgentStringTest {

    // The actual (public, non-secret) key currently provisioned in
    // app/src/main/assets/service-data.json's "agent-base64-pubkey" field.
    // Decodes to: "ssh-rsa AAAAB3NzaC1yc2E... kerry@grunt.ltd"
    private val realProvisionedAgentPubkey =
        "c3NoLXJzYSBBQUFBQjNOemFDMXljMkVBQUFBREFRQUJBQUFCQVFEUUpnWmRqbDh6QWk4QWNObUJQd28y" +
            "ZEZDR3pHcWdIK0RPeEpEb09ZRmN6b0pSK3FoR2xPcUoxT1o5UmtEUWVyTHZrMG05czd2RkFuYzlpWDJm" +
            "akpReWIzTFlwZ0R5RE85ZjVFcHl3MWRuMkpoMFhJRTF6ZXRVMHdZMDlpNmZWVjhMUFFpa05UUGZyMSt1" +
            "b3c2R1NsbGJOYWpidmR5TGkwdVorc2ZkZmJFazlkM2RCTGR4STlMb1hoanE3ZmZuZGx5MmlTcUNxUEND" +
            "ZU9BY3poSUY0OGlSdEJsRjNtNzdFQzVnSDVuVHdzbW1hd1REV0VPSUZiNzZOQmJKR3RUUWpZOStxeDRV" +
            "dnJ3dmdZcXpzMW9PalNhMFd6QWtSWVV2MzUzSG1la3lSU3doeEMybm14QVZGRU5uSERaWE1oREllVHp4" +
            "VWZ3ejllSXd0SDhHeENaMEUyb0oga2VycnlAZ3J1bnQubHRk"

    @Test
    fun `parseOpenSshRsaPublicKey decodes the real provisioned agent pubkey`() {
        val keySpec = Utils.parseOpenSshRsaPublicKey(realProvisionedAgentPubkey)

        assertEquals(BigInteger.valueOf(65537), keySpec.publicExponent)
        assertEquals(2048, keySpec.modulus.bitLength())
    }

    @Test
    fun `parseOpenSshRsaPublicKey produces a key KeyFactory can build`() {
        val keySpec = Utils.parseOpenSshRsaPublicKey(realProvisionedAgentPubkey)

        val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

        assertEquals("RSA", publicKey.algorithm)
    }

    @Test
    fun `full round trip - generated keypair encrypts and decrypts through the new parsing path`() {
        val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.genKeyPair()
        val rsaPublicKey = keyPair.public as RSAPublicKey
        val agentPubkeyLine = buildOpenSshRsaLine(rsaPublicKey.publicExponent, rsaPublicKey.modulus)
        val base64EncodedAgentPubkey = Base64.getEncoder().encodeToString(
            agentPubkeyLine.toByteArray(StandardCharsets.UTF_8)
        )

        val keySpec = Utils.parseOpenSshRsaPublicKey(base64EncodedAgentPubkey)
        val publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec)

        val encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val plaintext = "brainwallet-android,1.0,manufacturer-device-model,some-uuid"
        val encrypted = encryptCipher.doFinal(plaintext.toByteArray())

        val decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        decryptCipher.init(Cipher.DECRYPT_MODE, keyPair.private as RSAPrivateKey)
        val decrypted = String(decryptCipher.doFinal(encrypted))

        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `parseSshRsaPublicKeyBlob rejects a non-RSA SSH key type`() {
        val blob = buildSshWireFormat(
            keyType = "ssh-ed25519",
            fields = listOf(ByteArray(32))
        )

        assertThrows(IllegalArgumentException::class.java) {
            Utils.parseSshRsaPublicKeyBlob(blob)
        }
    }

    @Test
    fun `parseOpenSshRsaPublicKey throws when the OpenSSH line has no key blob field`() {
        val lineWithoutKeyBlob = "ssh-rsa"
        val base64Encoded = Base64.getEncoder().encodeToString(
            lineWithoutKeyBlob.toByteArray(StandardCharsets.UTF_8)
        )

        assertThrows(IllegalArgumentException::class.java) {
            Utils.parseOpenSshRsaPublicKey(base64Encoded)
        }
    }

    @Test
    fun `parseOpenSshRsaPublicKey throws on garbage input instead of silently misparsing`() {
        val notBase64AtAll = "%%%not-base64%%%"

        assertThrows(IllegalArgumentException::class.java) {
            Utils.parseOpenSshRsaPublicKey(notBase64AtAll)
        }
    }

    @Test
    fun `regression - the old PEM-header-stripping bug is not reproduced by the fix`() {
        // This is exactly the shape of value that broke the previous implementation:
        // an OpenSSH line whose fields, once whitespace is stripped, are no longer valid
        // base64 (the "ssh-rsa" prefix and "kerry@grunt.ltd" comment corrupt the payload).
        // The fix must not fall into that trap: it correctly isolates the key blob field
        // instead of concatenating the whole line.
        val keySpec = Utils.parseOpenSshRsaPublicKey(realProvisionedAgentPubkey)
        assertEquals(BigInteger.valueOf(65537), keySpec.publicExponent)
    }

    private fun buildOpenSshRsaLine(exponent: BigInteger, modulus: BigInteger): String {
        val blob = buildSshWireFormat(
            keyType = "ssh-rsa",
            fields = listOf(exponent.toByteArray(), modulus.toByteArray())
        )
        val blobBase64 = Base64.getEncoder().encodeToString(blob)
        return "ssh-rsa $blobBase64 test@brainwallet"
    }

    private fun buildSshWireFormat(keyType: String, fields: List<ByteArray>): ByteArray {
        val typeBytes = keyType.toByteArray(StandardCharsets.UTF_8)
        val totalSize = 4 + typeBytes.size + fields.sumOf { 4 + it.size }
        val buffer = ByteBuffer.allocate(totalSize)
        buffer.putInt(typeBytes.size)
        buffer.put(typeBytes)
        for (field in fields) {
            buffer.putInt(field.size)
            buffer.put(field)
        }
        return buffer.array()
    }
}
