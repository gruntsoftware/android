package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame

import org.junit.Test

//TODO: migrate from [com.brainwallet.analytics.ConstantsTests]
class BRConstantsTest {

    @Test
    fun `validate Litecoin symbol constant`() {
        assertSame(BRConstants.litecoinLowercase,"ł")
        assertSame(BRConstants.litecoinUppercase,"Ł")
    }

    @Test
    fun `validate App external URL constant`(){
        assertSame(BRConstants.TWITTER_LINK,"https://twitter.com/Brainwallet_App");
        assertSame(BRConstants.INSTAGRAM_LINK,"https://www.instagram.com/brainwalletapp");
        assertSame(BRConstants.WEB_LINK,"https://brainwallet.co");
        assertSame(BRConstants.TOS_LINK,"https://brainwallet.co/privacy-policy.html");
        assertSame(BRConstants.MOBILE_MP_LINK,"https://brainwallet.co/mobile-top-up.html");
        assertSame(BRConstants.BITREFILL_AFFILIATE_LINK,"https://www.bitrefill.com/");
    }

    @Test
    fun `validate Firebase Analytics constants`(){
        assertSame(BRConstants._20191105_AL,"app_launched");
        assertSame(BRConstants._20191105_VSC,"visit_send_controller");
        assertSame(BRConstants._20202116_VRC,"visit_receive_controller");
        assertSame(BRConstants._20191105_DSL,"did_send_ltc");
        assertSame(BRConstants._20191105_DTBT,"did_tap_buy_tab");
        assertSame(BRConstants._20200111_RNI,"rate_not_initialized");
        assertSame(BRConstants._20200111_FNI,"feeperkb_not_initialized");
        assertSame(BRConstants._20200111_TNI,"transaction_not_initialized");
        assertSame(BRConstants._20200111_WNI,"wallet_not_initialized");
        assertSame(BRConstants._20200111_PNI,"phrase_not_initialized");
        assertSame(BRConstants._20200111_UTST,"unable_to_sign_transaction");
        assertSame(BRConstants._20200112_ERR,"brainwallet_android_error");
        assertSame(BRConstants._20200112_DSR,"did_start_resync");
        assertSame(BRConstants._20200125_DSRR,"did_show_review_request");
        assertSame(BRConstants._20201118_DTGS,"did_tap_get_support");
        assertSame(BRConstants._20200217_DUWP,"did_unlock_with_pin");
        assertSame(BRConstants._20200217_DUWB,"did_unlock_with_biometrics");
        assertSame(BRConstants._20201121_SIL,"started_IFPS_lookup");
        assertSame(BRConstants._20201121_DRIA,"did_resolve_IPFS_address");
        assertSame(BRConstants._20201121_FRIA,"failed_resolve_IPFS_address");
        assertSame(BRConstants._20200207_DTHB,"did_tap_header_balance");
        assertSame(BRConstants._20210427_HCIEEH,"heartbeat_check_if_event_even_happens");
        assertSame(BRConstants._20220822_UTOU,"user_tapped_on_ud");
        assertSame(BRConstants._20230131_NENR,"no_error_nominal_response");
        assertSame(BRConstants._20230407_DCS,"did_complete_sync");
        assertSame(BRConstants._20240123_RAGI,"registered_android_general_interest");
        assertSame(BRConstants._20231225_UAP,"user_accepted_push");
        assertSame(BRConstants._20240101_US,"user_signup");
        assertSame(BRConstants._20241006_DRR,"did_request_rating");
        assertSame(BRConstants._20241006_UCR,"user_completed_rating");
    }

    @Test
    fun `validate pin length`() {
        assertEquals(4, BRConstants.BW_PIN_LENGTH)
    }
}


//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import android.util.Log;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//
//import com.breadwallet.presenter.activities.intro.IntroActivity;
//import com.breadwallet.tools.util.BRConstants;
//
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import java.net.URI;
//
//@Deprecated
//@RunWith(AndroidJUnit4.class)
//        public class ConstantsTests {
//    public static final String TAG = ConstantsTests.class.getName();
//    @Rule
//    public ActivityScenarioRule<IntroActivity> mActivityRule = new ActivityScenarioRule<>(IntroActivity.class);
//    @Before
//    public void setUp() {
//        Log.e(TAG, "setUp: ");
//    }
//
//    @After
//    public void tearDown() {
//    }
//    @Test
//    public void testLitecoinSymbolConstants() {
//        Assert.assertSame(BRConstants.litecoinLowercase,"ł");
//        Assert.assertSame(BRConstants.litecoinUppercase,"Ł");
//    }
//    @Test
//    public void testAppExternalURLConstants() {
//        Assert.assertSame(BRConstants.TWITTER_LINK,"https://twitter.com/Brainwallet_App");
//        Assert.assertSame(BRConstants.WEB_LINK,"https://brainwallet.co");
//        Assert.assertSame(BRConstants.TOS_LINK,"https://brainwallet.co/privacy");
//        Assert.assertSame(BRConstants.CUSTOMER_SUPPORT_LINK,"https://support.brainwallet.co/hc/en-us/requests/new");
//        Assert.assertSame(BRConstants.BITREFILL_AFFILIATE_LINK,"https://www.bitrefill.com/");
//    }
//    @Test
//    public void testFirebaseAnalyticsConstants() {
//        Assert.assertSame(BRConstants._20191105_AL,"app_launched");
//        Assert.assertSame(BRConstants._20191105_VSC,"visit_send_controller");
//        Assert.assertSame(BRConstants._20202116_VRC,"visit_receive_controller");
//        Assert.assertSame(BRConstants._20191105_DSL,"did_send_ltc");
//        Assert.assertSame(BRConstants._20191105_DTBT,"did_tap_buy_tab");
//        Assert.assertSame(BRConstants._20200111_RNI,"rate_not_initialized");
//        Assert.assertSame(BRConstants._20200111_FNI,"feeperkb_not_initialized");
//        Assert.assertSame(BRConstants._20200111_TNI,"transaction_not_initialized");
//        Assert.assertSame(BRConstants._20200111_WNI,"wallet_not_initialized");
//        Assert.assertSame(BRConstants._20200111_PNI,"phrase_not_initialized");
//        Assert.assertSame(BRConstants._20200111_UTST,"unable_to_sign_transaction");
//        Assert.assertSame(BRConstants._20200112_ERR,"error");
//        Assert.assertSame(BRConstants._20200112_DSR,"did_start_resync");
//        Assert.assertSame(BRConstants._20200125_DSRR,"did_show_review_request");
//        Assert.assertSame(BRConstants._20201118_DTGS,"did_tap_get_support");
//        Assert.assertSame(BRConstants._20200217_DUWP,"did_unlock_with_pin");
//        Assert.assertSame(BRConstants._20200217_DUWB,"did_unlock_with_biometrics");
//        Assert.assertSame(BRConstants._20201121_SIL,"started_IFPS_lookup");
//        Assert.assertSame(BRConstants._20201121_DRIA,"did_resolve_IPFS_address");
//        Assert.assertSame(BRConstants._20201121_FRIA,"failed_resolve_IPFS_address");
//        Assert.assertSame(BRConstants._20200207_DTHB,"did_tap_header_balance");
//        Assert.assertSame(BRConstants._20210427_HCIEEH,"heartbeat_check_if_event_even_happens");
//        Assert.assertSame(BRConstants._20220822_UTOU,"user_tapped_on_ud");
//        Assert.assertSame(BRConstants._20230131_NENR,"no_error_nominal_response");
//        Assert.assertSame(BRConstants._20230407_DCS,"did_complete_sync");
//        Assert.assertSame(BRConstants._20240123_RAGI,"registered_android_general_interest");
//        Assert.assertSame(BRConstants._20231225_UAP,"user_accepted_push");
//        Assert.assertSame(BRConstants._20240101_US,"user_signup");
//        Assert.assertSame(BRConstants._20241006_DRR,"did_request_rating");
//        Assert.assertSame(BRConstants._20241006_UCR,"user_completed_rating");
//    }
//}
