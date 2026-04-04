package com.brainwallet.consants

import com.brainwallet.constants.BWConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.RoundingMode
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore

class BWConstantsTest {

    // ── app version ────────────────────────────────────────────────────────

    @Test
    fun `APP_VERSION_NAME_CODE is non-empty`() {
        assertTrue(BWConstants.APP_VERSION_NAME_CODE.isNotEmpty())
    }

    @Test
    fun `APP_VERSION_NAME_CODE contains opening and closing parentheses`() {
        assertTrue(BWConstants.APP_VERSION_NAME_CODE.contains("("))
        assertTrue(BWConstants.APP_VERSION_NAME_CODE.contains(")"))
    }

    // ── animation durations ────────────────────────────────────────────────

    @Test
    fun `FADE_IN_DURATION is 400`() {
        assertEquals(400, BWConstants.FADE_IN_DURATION)
    }

    @Test
    fun `FADE_OUT_DURATION is 400`() {
        assertEquals(400, BWConstants.FADE_OUT_DURATION)
    }

    @Test
    fun `SHRINK_DURATION is 700`() {
        assertEquals(700, BWConstants.SHRINK_DURATION)
    }

    @Test
    fun `EXPAND_DURATION is 700`() {
        assertEquals(700, BWConstants.EXPAND_DURATION)
    }

    // ── pin length ─────────────────────────────────────────────────────────

    @Test
    fun `BW_PIN_LENGTH is 4`() {
        assertEquals(4, BWConstants.BW_PIN_LENGTH)
    }

    // ── currency units ─────────────────────────────────────────────────────

    @Test
    fun `CURRENT_UNIT_PHOTONS is 0`() {
        assertEquals(0, BWConstants.CURRENT_UNIT_PHOTONS)
    }

    @Test
    fun `CURRENT_UNIT_LITES is 1`() {
        assertEquals(1, BWConstants.CURRENT_UNIT_LITES)
    }

    @Test
    fun `CURRENT_UNIT_LITECOINS is 2`() {
        assertEquals(2, BWConstants.CURRENT_UNIT_LITECOINS)
    }

    @Test
    fun `currency unit values are distinct`() {
        val units = setOf(
            BWConstants.CURRENT_UNIT_PHOTONS,
            BWConstants.CURRENT_UNIT_LITES,
            BWConstants.CURRENT_UNIT_LITECOINS
        )
        assertEquals(3, units.size)
    }

    // ── litecoin symbols ───────────────────────────────────────────────────

    @Test
    fun `litecoinLowercase is correct unicode`() {
        assertEquals("\u0142", BWConstants.litecoinLowercase)
    }

    @Test
    fun `litecoinUppercase is correct unicode`() {
        assertEquals("\u0141", BWConstants.litecoinUppercase)
    }

    @Test
    fun `litecoin symbols are distinct`() {
        assertTrue(BWConstants.litecoinLowercase != BWConstants.litecoinUppercase)
    }

    // ── rounding mode ──────────────────────────────────────────────────────

    @Test
    fun `ROUNDING_MODE is HALF_EVEN`() {
        assertEquals(RoundingMode.HALF_EVEN, BWConstants.ROUNDING_MODE)
    }

    // ── false positive rates ───────────────────────────────────────────────

    @Test
    fun `FALSE_POS_RATE_LOW_PRIVACY is less than SEMI_PRIVACY`() {
        assertTrue(BWConstants.FALSE_POS_RATE_LOW_PRIVACY < BWConstants.FALSE_POS_RATE_SEMI_PRIVACY)
    }

    @Test
    fun `FALSE_POS_RATE_SEMI_PRIVACY is less than ANONYMOUS`() {
        assertTrue(BWConstants.FALSE_POS_RATE_SEMI_PRIVACY < BWConstants.FALSE_POS_RATE_ANONYMOUS)
    }

    @Test
    fun `all false positive rates are positive`() {
        assertTrue(BWConstants.FALSE_POS_RATE_LOW_PRIVACY > 0f)
        assertTrue(BWConstants.FALSE_POS_RATE_SEMI_PRIVACY > 0f)
        assertTrue(BWConstants.FALSE_POS_RATE_ANONYMOUS > 0f)
    }

    // ── request codes are distinct ─────────────────────────────────────────

    @Test
    fun `all request codes are distinct`() {
        val codes = setOf(
            BWConstants.SHOW_PHRASE_REQUEST_CODE,
            BWConstants.PAY_REQUEST_CODE,
            BWConstants.CANARY_REQUEST_CODE,
            BWConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE,
            BWConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE,
            BWConstants.PROVE_PHRASE_REQUEST,
            BWConstants.SCANNER_REQUEST
        )
        assertEquals(7, codes.size)
    }

    // ── URLs are non-empty and well-formed ─────────────────────────────────

    @Test
    fun `all external URLs start with https`() {
        listOf(
            BWConstants.TWITTER_LINK,
            BWConstants.INSTAGRAM_LINK,
            BWConstants.WEB_LINK,
            BWConstants.SUPPORT_WEB_LINK,
            BWConstants.TOS_LINK,
            BWConstants.LINKTREE_URL,
            BWConstants.BW_API_PROD_HOST,
            BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL,
            BWConstants.BLOCKCYPHER_EXPLORER_BASE_URL,
        ).forEach { url ->
            assertTrue("$url should start with https", url.startsWith("https://"))
        }
    }

    @Test
    @Ignore("Integration test - run manually or in dedicated network test job")
    fun `all external URLs are reachable`() = runBlocking {
        val urls = listOf(
            BWConstants.INSTAGRAM_LINK,
            BWConstants.WEB_LINK,
            BWConstants.SUPPORT_WEB_LINK,
            BWConstants.TOS_LINK,
            BWConstants.LINKTREE_URL,
        )

        urls.forEach { url ->
            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "HEAD"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            try {
                val responseCode = connection.responseCode
                assertTrue(
                    "$url returned $responseCode",
                    responseCode in 200..399
                )
            } finally {
                connection.disconnect()
            }
        }
    }

    @Test
    fun `API server is reachable`() = runBlocking {
        val connection = java.net.URL(BWConstants.BW_API_PROD_HOST)
            .openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        try {
            val responseCode = connection.responseCode
            // 404 is acceptable — server is up but root path has no handler
            assertTrue(
                "${BWConstants.BW_API_PROD_HOST} " +
                    "returned $responseCode — server unreachable",
                responseCode in 200..499
            )
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun `Blockcypher browser is reachable`() = runBlocking {
        val connection = java.net.URL(BWConstants.BLOCKCYPHER_EXPLORER_BASE_URL)
            .openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        try {
            val responseCode = connection.responseCode
            // 404 is acceptable — server is up but root path has no handler
            assertTrue(
                "${BWConstants.BLOCKCYPHER_EXPLORER_BASE_URL} " +
                    "returned $responseCode — server unreachable",
                responseCode in 200..499
            )
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun `Blockchair browser is reachable`() = runBlocking {
        val connection = java.net.URL(BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL)
            .openConnection() as java.net.HttpURLConnection
        connection.requestMethod = "HEAD"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        try {
            val responseCode = connection.responseCode
            // 404 is acceptable — server is up but root path has no handler
            assertTrue(
                "${BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL} " +
                    "returned $responseCode — server unreachable",
                responseCode in 200..499
            )
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun `BLOCKCHAIR_EXPLORER_BASE_URL ends with slash`() {
        assertTrue(BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL.endsWith("/"))
    }

    @Test
    fun `BLOCKCYPHER_EXPLORER_BASE_URL ends with slash`() {
        assertTrue(BWConstants.BLOCKCYPHER_EXPLORER_BASE_URL.endsWith("/"))
    }

    // ── ONE_BITCOIN ────────────────────────────────────────────────────────

    @Test
    fun `ONE_BITCOIN is 100 million satoshis`() {
        assertEquals(100_000_000, BWConstants.ONE_BITCOIN)
    }

    // ── analytics event keys are non-empty ────────────────────────────────

    @Test
    fun `all analytics event keys are non-empty`() {
        listOf(
            BWConstants._BW_MAIN_OPEN,
            BWConstants._20191105_AL,
            BWConstants._20191105_VSC,
            BWConstants._20202116_VRC,
            BWConstants._20191105_DSL,
            BWConstants._20191105_DTBT,
            BWConstants._20200111_FNI,
            BWConstants._20200111_TNI,
            BWConstants._20200111_WNI,
            BWConstants._20200112_ERR,
            BWConstants._20200112_DSR,
            BWConstants._20201118_DTGS,
            BWConstants._20200217_DUWB,
            BWConstants._20250303_DSTU,
            BWConstants._20250517_WCINFO,
            BWConstants._20241006_DRR,
            BWConstants._20241006_UCR,
            BWConstants._HOME_OPEN,
        ).forEach { key ->
            assertTrue("Analytics key should not be empty", key.isNotEmpty())
        }
    }

    @Test
    fun `all analytics event keys are distinct`() {
        val keys = listOf(
            BWConstants._20191105_AL,
            BWConstants._20191105_VSC,
            BWConstants._20202116_VRC,
            BWConstants._20191105_DSL,
            BWConstants._20191105_DTBT,
            BWConstants._20200111_FNI,
            BWConstants._20200111_TNI,
            BWConstants._20200111_WNI,
            BWConstants._20200112_ERR,
            BWConstants._20200112_DSR,
            BWConstants._20201118_DTGS,
            BWConstants._20200217_DUWB,
            BWConstants._20250303_DSTU,
            BWConstants._20250517_WCINFO,
            BWConstants._20241006_DRR,
            BWConstants._20241006_UCR,
            BWConstants._HOME_OPEN,
        )
        assertEquals(keys.size, keys.toSet().size)
    }
}
