package com.brainwallet.tools.constants

import com.brainwallet.constants.BWConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.RoundingMode

class BWConstantsTests {

    // ─────────────────────────────────────────────────────────────────────────
    // Litecoin symbol
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate Litecoin lowercase symbol constant`() {
        assertSame(BWConstants.litecoinLowercase, "ł")
    }

    @Test
    fun `validate Litecoin uppercase symbol constant`() {
        assertSame(BWConstants.litecoinUppercase, "Ł")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // App external URLs
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate App external URL constants`() {
        assertSame(BWConstants.TWITTER_LINK, "https://twitter.com/Brainwallet_App")
        assertSame(BWConstants.INSTAGRAM_LINK, "https://www.instagram.com/brainwalletapp")
        assertSame(BWConstants.WEB_LINK, "https://brainwallet.co")
        assertSame(BWConstants.SUPPORT_WEB_LINK, "https://www.brainwallet.co/support")
        assertSame(BWConstants.TOS_LINK, "https://www.brainwallet.co/privacypolicy")
        assertSame(BWConstants.LINKTREE_URL, "https://linktr.ee/brainwallet")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // API hosts
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate prod API host`() {
        assertSame(BWConstants.BW_API_PROD_HOST, "https://api.grunt.ltd")
    }

    @Test
    fun `validate block explorer base URL`() {
        assertSame(BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL, "https://blockchair.com/litecoin/transaction/")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Numeric constants
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate pin length`() {
        assertEquals(4, BWConstants.BW_PIN_LENGTH)
    }

    @Test
    fun `validate ONE_BITCOIN is 100 million satoshis`() {
        assertEquals(100_000_000, BWConstants.ONE_BITCOIN)
    }

    @Test
    fun `validate MIN_MASTERPUBKEY_LENGTH`() {
        assertEquals(33, BWConstants.MIN_MASTERPUBKEY_LENGTH)
    }

    @Test
    fun `validate currency unit ordinals are sequential`() {
        assertEquals(0, BWConstants.CURRENT_UNIT_PHOTONS)
        assertEquals(1, BWConstants.CURRENT_UNIT_LITES)
        assertEquals(2, BWConstants.CURRENT_UNIT_LITECOINS)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Auth and camera request codes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate auth request codes are unique`() {
        val codes = setOf(
            BWConstants.SHOW_PHRASE_REQUEST_CODE,
            BWConstants.PAY_REQUEST_CODE,
            BWConstants.CANARY_REQUEST_CODE,
            BWConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE,
            BWConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE,
            BWConstants.PROVE_PHRASE_REQUEST,
            BWConstants.SCANNER_REQUEST,
            BWConstants.CAMERA_REQUEST_ID
        )
        assertEquals(8, codes.size)
    }

    @Test
    fun `validate auth request code values`() {
        assertEquals(111, BWConstants.SHOW_PHRASE_REQUEST_CODE)
        assertEquals(112, BWConstants.PAY_REQUEST_CODE)
        assertEquals(113, BWConstants.CANARY_REQUEST_CODE)
        assertEquals(114, BWConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE)
        assertEquals(115, BWConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE)
        assertEquals(119, BWConstants.PROVE_PHRASE_REQUEST)
        assertEquals(201, BWConstants.SCANNER_REQUEST)
        assertEquals(34, BWConstants.CAMERA_REQUEST_ID)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SharedPrefs keys
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate SharedPrefs key constants`() {
        assertSame(BWConstants.PREFS_NAME, "MyPrefsFile")
        assertSame(BWConstants.RECEIVE_ADDRESS, "receive_address")
        assertSame(BWConstants.START_HEIGHT, "startHeight")
        assertSame(BWConstants.LAST_BLOCK_HEIGHT, "lastBlockHeight")
        assertSame(BWConstants.CURRENT_UNIT, "currencyUnit")
        assertSame(BWConstants.POSITION, "position")
        assertSame(BWConstants.PHRASE_WRITTEN, "phraseWritten")
        assertSame(BWConstants.ALLOW_SPEND, "allowSpend")
        assertSame(BWConstants.USER_ID, "userId")
        assertSame(BWConstants.GEO_PERMISSIONS_REQUESTED, "geoPermissionsRequested")
        assertSame(BWConstants.CANARY_STRING, "canary")
        assertSame(BWConstants.FIRST_ADDRESS, "firstAddress")
        assertSame(BWConstants.SECURE_TIME_PREFS, "secureTime")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rounding and WAL
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate ROUNDING_MODE is HALF_EVEN`() {
        assertEquals(RoundingMode.HALF_EVEN, BWConstants.ROUNDING_MODE)
    }

    @Test
    fun `validate WAL is true`() {
        assertTrue(BWConstants.WAL)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Analytics keys
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate analytics timing keys`() {
        assertSame(BWConstants.START_TIME, "start_time")
        assertSame(BWConstants.SUCCESS_TIME, "success_time")
        assertSame(BWConstants.FAILURE_TIME, "failure_time")
        assertSame(BWConstants.ERROR, "error")
    }

    @Test
    fun `validate active Firebase analytics event constants`() {
        assertSame(BWConstants._20191105_VSC, "visit_send_controller")
        assertSame(BWConstants._20202116_VRC, "visit_receive_controller")
        assertSame(BWConstants._20191105_DSL, "did_send_ltc")
        assertSame(BWConstants._20191105_DTBT, "did_tap_buy_tab")
        assertSame(BWConstants._20200111_FNI, "feeperkb_not_initialized")
        assertSame(BWConstants._20200111_TNI, "transaction_not_initialized")
        assertSame(BWConstants._20200111_WNI, "wallet_not_initialized")
        assertSame(BWConstants._20200112_ERR, "brainwallet_android_error")
        assertSame(BWConstants._20200112_DSR, "did_start_resync")
        assertSame(BWConstants._20201118_DTGS, "did_tap_get_support")
        assertSame(BWConstants._20200217_DU, "did_unlock")
    }

    @Test
    fun `validate recent Firebase analytics event constants`() {
        assertSame(BWConstants._20241006_DRR, "did_request_rating")
        assertSame(BWConstants._20241006_UCR, "user_completed_rating")
        assertSame(BWConstants._20250517_WCINFO, "wallet_callback_info")
    }

    // ─────────────────────────────────────────────────────────────────────────
    // False positive filter rates
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate false positive rates are in ascending order`() {
        assertTrue(BWConstants.FALSE_POS_RATE_LOW_PRIVACY < BWConstants.FALSE_POS_RATE_SEMI_PRIVACY)
        assertTrue(BWConstants.FALSE_POS_RATE_SEMI_PRIVACY < BWConstants.FALSE_POS_RATE_ANONYMOUS)
    }

    @Test
    fun `validate false positive rate values`() {
        assertEquals(0.00005f, BWConstants.FALSE_POS_RATE_LOW_PRIVACY)
        assertEquals(0.00008f, BWConstants.FALSE_POS_RATE_SEMI_PRIVACY)
        assertEquals(0.0005f, BWConstants.FALSE_POS_RATE_ANONYMOUS)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Native lib
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `validate native library name`() {
        assertSame(BWConstants.NATIVE_LIB_NAME, "core-lib")
    }
}
