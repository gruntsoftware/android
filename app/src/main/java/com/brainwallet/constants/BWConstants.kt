package com.brainwallet.constants

import com.brainwallet.BuildConfig
import java.math.RoundingMode
import java.util.Locale

object BWConstants {

    const val MIN_MASTERPUBKEY_LENGTH = 33

    /**
     * App Version and Version Code
     */
    @JvmField
    val APP_VERSION_NAME_CODE: String = String.format(
        Locale.US,
        "%1\$s (%2\$s)",
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE
    )

    /**
     * Native library name
     */
    const val NATIVE_LIB_NAME: String = "core-lib"

    /**
     * Permissions
     */
    const val CAMERA_REQUEST_ID: Int = 34

    /**
     * Animation values
     */
    const val FADE_IN_DURATION: Int = 400
    const val FADE_OUT_DURATION: Int = 400
    const val SHRINK_DURATION: Int = 700
    const val EXPAND_DURATION: Int = 700

    /**
     * Request codes for auth
     */
    const val SHOW_PHRASE_REQUEST_CODE: Int = 111
    const val PAY_REQUEST_CODE: Int = 112
    const val CANARY_REQUEST_CODE: Int = 113
    const val PUT_PHRASE_NEW_WALLET_REQUEST_CODE: Int = 114
    const val PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE: Int = 115
    const val PROVE_PHRASE_REQUEST: Int = 119
    const val SHOW_EMOJIS_REQUEST_CODE: Int = 120

    const val PUT_EMOJIS_REQUEST_CODE: Int = 121

    /**
     * Request codes for taking pictures
     */
    const val SCANNER_REQUEST: Int = 201
    const val CANARY_STRING: String = "canary"
    const val FIRST_ADDRESS: String = "firstAddress"
    const val SECURE_TIME_PREFS: String = "secureTime"

    const val ONE_BITCOIN: Int = 100000000

    /**
     * BRSharedPrefs
     */
    const val PREFS_NAME: String = "MyPrefsFile"
    const val RECEIVE_ADDRESS: String = "receive_address"
    const val START_HEIGHT: String = "startHeight"
    const val LAST_BLOCK_HEIGHT: String = "lastBlockHeight"
    const val LTC_STATS_BLOCK_HEIGHT: String = "ltcStatsBlockHeight"
    const val LTC_STATS_MEMPOOL_TRANSACTIONS: String = "ltcStatsMempoolTransactions"
    const val LTC_STATS_MEMPOOL_SIZE: String = "ltcStatsMempoolSize"
    const val LTC_STATS_TRANSACTIONS_OVER_24H: String = "ltcStatsTransactionsOver24H"
    const val CURRENT_UNIT: String = "currencyUnit"
    const val POSITION: String = "position"
    const val PHRASE_WRITTEN: String = "phraseWritten"
    const val EMOJIS_CHOSEN: String = "emojisChosen"
    const val ALLOW_SPEND: String = "allowSpend"
    const val USER_ID: String = "userId"
    const val GEO_PERMISSIONS_REQUESTED: String = "geoPermissionsRequested"

    /**
     * Currency units
     */
    const val CURRENT_UNIT_PHOTONS: Int = 0 // formerly CURRENT_UNIT_BITS
    const val CURRENT_UNIT_LITES: Int = 1 // formerly CURRENT_UNIT_MBITS
    const val CURRENT_UNIT_LITECOINS: Int = 2
    const val litecoinLowercase: String = "\u0142"
    const val litecoinUppercase: String = "\u0141"

    @JvmField
    val ROUNDING_MODE: RoundingMode = RoundingMode.HALF_EVEN
    const val WAL: Boolean = true

    const val loopBug: String = "android-loop-bug"

    /**
     * brainwallet pin/passcode length
     */
    const val BW_PIN_LENGTH: Int = 4

    /**
     * App External URLs
     */
    const val TWITTER_LINK: String = "https://twitter.com/Brainwallet_App"
    const val INSTAGRAM_LINK: String = "https://www.instagram.com/brainwalletapp"
    const val WEB_LINK: String = "https://brainwallet.co"
    const val SUPPORT_WEB_LINK: String = "https://www.brainwallet.co/support"
    const val TOS_LINK: String = "https://www.brainwallet.co/privacypolicy"
    const val LINKTREE_URL: String = "https://linktr.ee/brainwallet"
    const val BITREFILL_URL: String = "https://www.bitrefill.com/?ref=bw_tk_26"

    /**
     * API Hosts
     */
    const val BW_API_PROD_HOST: String = "https://api.grunt.ltd"

    const val BLOCKCHAIR_EXPLORER_BASE_URL: String = "https://blockchair.com/litecoin/transaction/"
    const val BLOCKCYPHER_EXPLORER_BASE_URL: String = "https://live.blockcypher.com/ltc/"

    const val _20191105_AL: String = "app_launched"
    const val _20191105_VSC: String = "visit_send_controller"
    const val _20202116_VRC: String = "visit_receive_controller"
    const val _20191105_DSL: String = "did_send_ltc"
    const val _20191105_DTBT: String = "did_tap_buy_tab"
    const val _20200111_FNI: String = "feeperkb_not_initialized"
    const val _20200111_TNI: String = "transaction_not_initialized"
    const val _20200111_WNI: String = "wallet_not_initialized"
    const val _20200112_ERR: String = "brainwallet_android_error"
    const val _20200112_DSR: String = "did_start_resync"
    const val _20201118_DTGS: String = "did_tap_get_support"
    const val _20200217_DU: String = "did_unlock"

    const val _20250303_DSTU: String = "did_skip_top_up"
    const val _20250517_WCINFO: String = "wallet_callback_info"
    const val _20241006_DRR: String = "did_request_rating"
    const val _20241006_UCR: String = "user_completed_rating"
    const val _HOME_OPEN: String = "home_open"

    /**
     * Analytics keys
     */
    const val START_TIME: String = "start_time"
    const val SUCCESS_TIME: String = "success_time"
    const val FAILURE_TIME: String = "failure_time"
    const val ERROR: String = "error"

    /**
     * False Positive rate keys
     */
    const val FALSE_POS_RATE_LOW_PRIVACY: Float = 0.00005f
    const val FALSE_POS_RATE_SEMI_PRIVACY: Float = 0.00008f
    const val FALSE_POS_RATE_ANONYMOUS: Float = 0.0005f

    @Retention(AnnotationRetention.SOURCE)
    annotation class Event
    const val ON_QR_CODE_RESULT: String = "on_qr_code_result"
}
