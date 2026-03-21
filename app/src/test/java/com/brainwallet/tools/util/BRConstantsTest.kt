package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame

import org.junit.Test

// TODO: migrate from [com.brainwallet.analytics.ConstantsTests]
class BRConstantsTest {

    @Test
    fun `validate Litecoin symbol constant`() {
        assertSame(BWConstants.litecoinLowercase, "ł")
        assertSame(BWConstants.litecoinUppercase, "Ł")
    }

    @Test
    fun `validate App external URL constant`() {
        assertSame(BWConstants.TWITTER_LINK, "https://twitter.com/Brainwallet_App")
        assertSame(BWConstants.INSTAGRAM_LINK, "https://www.instagram.com/brainwalletapp")
        assertSame(BWConstants.WEB_LINK, "https://brainwallet.co")
        assertSame(BWConstants.TOS_LINK, "https://brainwallet.co/privacy-policy.html")
        assertSame(BWConstants.MOBILE_MP_LINK, "https://brainwallet.co/mobile-top-up.html")
        assertSame(BWConstants.BITREFILL_AFFILIATE_LINK, "https://www.bitrefill.com/")
    }

    @Test
    fun `validate Firebase Analytics constants`() {
        assertSame(BWConstants._20191105_AL, "app_launched")
        assertSame(BWConstants._20191105_VSC, "visit_send_controller")
        assertSame(BWConstants._20202116_VRC, "visit_receive_controller")
        assertSame(BWConstants._20191105_DSL, "did_send_ltc")
        assertSame(BWConstants._20191105_DTBT, "did_tap_buy_tab")
        assertSame(BWConstants._20200111_RNI, "rate_not_initialized")
        assertSame(BWConstants._20200111_FNI, "feeperkb_not_initialized")
        assertSame(BWConstants._20200111_TNI, "transaction_not_initialized")
        assertSame(BWConstants._20200111_WNI, "wallet_not_initialized")
        assertSame(BWConstants._20200111_PNI, "phrase_not_initialized")
        assertSame(BWConstants._20200111_UTST, "unable_to_sign_transaction")
        assertSame(BWConstants._20200112_ERR, "brainwallet_android_error")
        assertSame(BWConstants._20200112_DSR, "did_start_resync")
        assertSame(BWConstants._20200125_DSRR, "did_show_review_request")
        assertSame(BWConstants._20201118_DTGS, "did_tap_get_support")
        assertSame(BWConstants._20200217_DUWP, "did_unlock_with_pin")
        assertSame(BWConstants._20200217_DUWB, "did_unlock_with_biometrics")
        assertSame(BWConstants._20201121_SIL, "started_IFPS_lookup")
        assertSame(BWConstants._20201121_DRIA, "did_resolve_IPFS_address")
        assertSame(BWConstants._20201121_FRIA, "failed_resolve_IPFS_address")
        assertSame(BWConstants._20200207_DTHB, "did_tap_header_balance")
        assertSame(BWConstants._20210427_HCIEEH, "heartbeat_check_if_event_even_happens")
        assertSame(BWConstants._20220822_UTOU, "user_tapped_on_ud")
        assertSame(BWConstants._20230131_NENR, "no_error_nominal_response")
        assertSame(BWConstants._20230407_DCS, "did_complete_sync")
        assertSame(BWConstants._20240123_RAGI, "registered_android_general_interest")
        assertSame(BWConstants._20231225_UAP, "user_accepted_push")
        assertSame(BWConstants._20240101_US, "user_signup")
        assertSame(BWConstants._20241006_DRR, "did_request_rating")
        assertSame(BWConstants._20241006_UCR, "user_completed_rating")
    }

    @Test
    fun `validate pin length`() {
        assertEquals(4, BWConstants.BW_PIN_LENGTH)
    }
}

// import androidx.test.ext.junit.runners.AndroidJUnit4;
// import android.util.Log;
// import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
// import com.breadwallet.presenter.activities.intro.IntroActivity;
// import com.breadwallet.tools.util.BRConstants;
//
// import org.junit.After;
// import org.junit.Assert;
// import org.junit.Before;
// import org.junit.Rule;
// import org.junit.Test;
// import org.junit.runner.RunWith;
//
// import java.net.URI;
//
// @Deprecated
// @RunWith(AndroidJUnit4.class)
//        public class ConstantsTests {
//    public static final String TAG = ConstantsTests.class.getName();
//    @Rule
//    public ActivityScenarioRule<IntroActivity> mActivityRule = new ActivityScenarioRule<>(IntroActivity.class);
//    @Before
//    public void setUp() {
//        Log.e(TAG, "setUp: ")
//    }
//
//    @After
//    public void tearDown() {
//    }
//    @Test
//    public void testLitecoinSymbolConstants() {
//        Assert.assertSame(BWConstants.litecoinLowercase,"ł")
//        Assert.assertSame(BWConstants.litecoinUppercase,"Ł")
//    }
//    @Test
//    public void testAppExternalURLConstants() {
//        Assert.assertSame(BWConstants.TWITTER_LINK,"https://twitter.com/Brainwallet_App")
//        Assert.assertSame(BWConstants.WEB_LINK,"https://brainwallet.co")
//        Assert.assertSame(BWConstants.TOS_LINK,"https://brainwallet.co/privacy")
//        Assert.assertSame(BWConstants.CUSTOMER_SUPPORT_LINK,"https://support.brainwallet.co/hc/en-us/requests/new")
//        Assert.assertSame(BWConstants.BITREFILL_AFFILIATE_LINK,"https://www.bitrefill.com/")
//    }
//    @Test
//    public void testFirebaseAnalyticsConstants() {
//        Assert.assertSame(BWConstants._20191105_AL,"app_launched");
//        Assert.assertSame(BWConstants._20191105_VSC,"visit_send_controller");
//        Assert.assertSame(BWConstants._20202116_VRC,"visit_receive_controller");
//        Assert.assertSame(BWConstants._20191105_DSL,"did_send_ltc");
//        Assert.assertSame(BWConstants._20191105_DTBT,"did_tap_buy_tab");
//        Assert.assertSame(BWConstants._20200111_RNI,"rate_not_initialized");
//        Assert.assertSame(BWConstants._20200111_FNI,"feeperkb_not_initialized");
//        Assert.assertSame(BWConstants._20200111_TNI,"transaction_not_initialized");
//        Assert.assertSame(BWConstants._20200111_WNI,"wallet_not_initialized");
//        Assert.assertSame(BWConstants._20200111_PNI,"phrase_not_initialized");
//        Assert.assertSame(BWConstants._20200111_UTST,"unable_to_sign_transaction");
//        Assert.assertSame(BWConstants._20200112_ERR,"error");
//        Assert.assertSame(BWConstants._20200112_DSR,"did_start_resync");
//        Assert.assertSame(BWConstants._20200125_DSRR,"did_show_review_request");
//        Assert.assertSame(BWConstants._20201118_DTGS,"did_tap_get_support");
//        Assert.assertSame(BWConstants._20200217_DUWP,"did_unlock_with_pin");
//        Assert.assertSame(BWConstants._20200217_DUWB,"did_unlock_with_biometrics");
//        Assert.assertSame(BWConstants._20201121_SIL,"started_IFPS_lookup");
//        Assert.assertSame(BWConstants._20201121_DRIA,"did_resolve_IPFS_address");
//        Assert.assertSame(BWConstants._20201121_FRIA,"failed_resolve_IPFS_address");
//        Assert.assertSame(BWConstants._20200207_DTHB,"did_tap_header_balance");
//        Assert.assertSame(BWConstants._20210427_HCIEEH,"heartbeat_check_if_event_even_happens");
//        Assert.assertSame(BWConstants._20220822_UTOU,"user_tapped_on_ud");
//        Assert.assertSame(BWConstants._20230131_NENR,"no_error_nominal_response");
//        Assert.assertSame(BWConstants._20230407_DCS,"did_complete_sync");
//        Assert.assertSame(BWConstants._20240123_RAGI,"registered_android_general_interest");
//        Assert.assertSame(BWConstants._20231225_UAP,"user_accepted_push");
//        Assert.assertSame(BWConstants._20240101_US,"user_signup");
//        Assert.assertSame(BWConstants._20241006_DRR,"did_request_rating");
//        Assert.assertSame(BWConstants._20241006_UCR,"user_completed_rating");
//    }
// }
