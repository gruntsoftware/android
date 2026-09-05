package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure-JVM coverage for [TrustedNode] - the host/port parsing + formatting helpers that sit
 * on the trusted-node persistence path (BRKeyStore + SettingsViewModel + BRPeerManager).
 */
class TrustedNodeTest {

    // ── STANDARD_PORT ────────────────────────────────────────────────────────

    @Test
    fun `STANDARD_PORT is Litecoin mainnet P2P port 9333`() {
        assertEquals(9333, TrustedNode.STANDARD_PORT)
    }

    // ── withPort ─────────────────────────────────────────────────────────────

    @Test
    fun `withPort keeps an explicit positive port`() {
        assertEquals("1.2.3.4:19335", TrustedNode.withPort("1.2.3.4", 19335))
    }

    @Test
    fun `withPort substitutes STANDARD_PORT when port is zero`() {
        assertEquals("1.2.3.4:9333", TrustedNode.withPort("1.2.3.4", 0))
    }

    @Test
    fun `withPort substitutes STANDARD_PORT when port is negative`() {
        assertEquals("1.2.3.4:9333", TrustedNode.withPort("1.2.3.4", -1))
    }

    // ── getNodeHost ──────────────────────────────────────────────────────────

    @Test
    fun `getNodeHost strips the port from a host colon port string`() {
        assertEquals("165.227.48.221", TrustedNode.getNodeHost("165.227.48.221:9333"))
    }

    @Test
    fun `getNodeHost returns the input unchanged when there is no port`() {
        assertEquals("165.227.48.221", TrustedNode.getNodeHost("165.227.48.221"))
    }

    // ── getNodePort ──────────────────────────────────────────────────────────

    @Test
    fun `getNodePort parses the port from a host colon port string`() {
        assertEquals(9333, TrustedNode.getNodePort("165.227.48.221:9333"))
    }

    @Test
    fun `getNodePort returns 0 when no port is present`() {
        assertEquals(0, TrustedNode.getNodePort("165.227.48.221"))
    }

    @Test
    fun `getNodePort returns 0 when the port is not a number`() {
        assertEquals(0, TrustedNode.getNodePort("165.227.48.221:abcd"))
    }

    // ── isValid ──────────────────────────────────────────────────────────────

    @Test
    fun `isValid accepts a bare IPv4 host`() {
        assertTrue(TrustedNode.isValid("192.168.1.10"))
    }

    @Test
    fun `isValid accepts an IPv4 host with a port`() {
        assertTrue(TrustedNode.isValid("192.168.1.10:9333"))
    }

    @Test
    fun `isValid rejects null`() {
        assertFalse(TrustedNode.isValid(null))
    }

    @Test
    fun `isValid rejects an empty string`() {
        assertFalse(TrustedNode.isValid(""))
    }

    @Test
    fun `isValid rejects a hostname with letters`() {
        assertFalse(TrustedNode.isValid("node.example.com"))
    }

    @Test
    fun `isValid rejects an octet above 255`() {
        assertFalse(TrustedNode.isValid("192.168.1.256"))
    }

    @Test
    fun `isValid rejects fewer than four octets`() {
        assertFalse(TrustedNode.isValid("192.168.1"))
    }

    @Test
    fun `isValid rejects more than one colon`() {
        assertFalse(TrustedNode.isValid("192.168.1.10:9333:9333"))
    }

    // ── round trip ───────────────────────────────────────────────────────────

    @Test
    fun `splitting then withPort re-forms a host colon port string`() {
        val input = "10.0.0.5:19335"
        val reformed = TrustedNode.withPort(
            TrustedNode.getNodeHost(input),
            TrustedNode.getNodePort(input)
        )
        assertEquals(input, reformed)
    }

    @Test
    fun `a portless host round-trips to an explicit STANDARD_PORT`() {
        val input = "10.0.0.5"
        val reformed = TrustedNode.withPort(
            TrustedNode.getNodeHost(input),
            TrustedNode.getNodePort(input)
        )
        assertEquals("10.0.0.5:9333", reformed)
    }
}
