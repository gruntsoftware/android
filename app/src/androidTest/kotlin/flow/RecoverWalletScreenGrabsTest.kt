package flow

import android.Manifest
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import com.brainwallet.R
import com.brainwallet.test.BuildConfig
import com.brainwallet.ui.BrainwalletActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.FalconScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * TODO: revisit this, since breaking with new navigation
 */
@RunWith(JUnit4::class)
@LargeTest
class RecoverWalletScreenGrabsTest {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA,
        Manifest.permission.POST_NOTIFICATIONS,
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<BrainwalletActivity>()

    @Test
    fun onRecoverFlowSuccess() {
        composeTestRule.activityRule.scenario.onActivity {
            Screengrab.setDefaultScreenshotStrategy(FalconScreenshotStrategy(it))
        }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        composeTestRule.onRoot().printToLog("screengrab")

        composeTestRule.waitForIdle()

        Screengrab.screenshot("1_welcome_screen")

        composeTestRule.onNode(hasTestTag("buttonRestore")).performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("2_input_words_screen")

        //seed words input
        val editTextTags = (0..11).map { index -> "textFieldSeedWord$index" }

        val paperKey = BuildConfig.SCREENGRAB_PAPERKEY

        editTextTags.zip(paperKey).forEachIndexed { index, (textFieldTag, value) ->
            device.waitForIdle(100)
            val textForTyping = value.dropLast(1)
            composeTestRule.onNodeWithTag(textFieldTag).onChild().performTextInput(textForTyping)
            composeTestRule.onNodeWithText(value).performClick()

        }

        Screengrab.screenshot("3_input_words_screen_2")

        composeTestRule.onNodeWithTag("buttonRestore").performClick()

        device.waitForIdle()

        repeat(4) {
            composeTestRule.onNodeWithTag("keypad1").performClick()
        }

        Screengrab.screenshot("4_set_passcode_screen")

        repeat(4) {
            composeTestRule.onNodeWithTag("keypad1").performClick()
        }

        Screengrab.screenshot("5_set_passcode_confirm_screen")

        composeTestRule.waitForIdle()
        device.waitForIdle(3000)

        Screengrab.screenshot("6_main_screen")

        //setting drawer
        onView(withId(R.id.menuBut)).perform(click())

        Screengrab.screenshot("7_setting_drawer_open")

        composeTestRule.onNodeWithTag("settingSecurity").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("8_setting_drawer_open_security")

        composeTestRule.onNodeWithTag("settingLanguage").performClick()
//
        composeTestRule.waitForIdle()
//
        Screengrab.screenshot("9_setting_drawer_open_language")
//
//        composeTestRule.onNodeWithTag("settingLanguage").performClick() //collapsed
//
//        composeTestRule.waitForIdle()
//
        composeTestRule.onNodeWithTag("settingCurrency").performClick()
//
        composeTestRule.waitForIdle()
//
        Screengrab.screenshot("10_setting_drawer_open_currency")
//
//        composeTestRule.onNodeWithTag("settingCurrency").performClick() //collapsed
//
//        composeTestRule.waitForIdle()
//
        composeTestRule.onNodeWithTag("settingGames").performClick()
//
        composeTestRule.waitForIdle()
//
        Screengrab.screenshot("11_setting_drawer_open_games")

        composeTestRule.onNodeWithTag("lazyColumnSetting").performScrollToIndex(10)
//
//        composeTestRule.onNodeWithTag("settingGames").performClick() //collapsed
//
//        composeTestRule.waitForIdle()
//
//        composeTestRule.onNodeWithTag("settingBlockchain").performClick()
//
//        composeTestRule.waitForIdle()
//
//        Screengrab.screenshot("8_setting_drawer_open_blockchain")
//
//        composeTestRule.onNodeWithTag("settingBlockchain").performClick() //collapsed
//
//        composeTestRule.waitForIdle()

        device.waitForIdle(1000)

//        onView(withText(R.string.MenuButton_security)).perform(click())
//
//        Screengrab.screenshot("9_security_center_screen")
//
//        onView(withId(R.id.close_button)).perform(click())
//
//        onView(withId(R.id.menuBut)).perform(click())
//
//        onView(withText(R.string.MenuButton_settings)).perform(click())
//
//        Screengrab.screenshot("10_settings_screen")
//
//        onView(withText(R.string.settings_show_seed)).perform(click())
//
//        onView(withId(R.id.show_seed_button)).perform(click())
//
//        Screengrab.screenshot("11_settings_seed_phrase")
//
//        device.pressBack()
//
//        onView(withText(R.string.Settings_wipe)).perform(click())
//
//        Screengrab.screenshot("12_settings_recover_another_wallet")
//
//        onView(withId(R.id.close_button)).perform(click())
//
//        onView(withText(R.string.Settings_languages)).perform(click())
//
//        Screengrab.screenshot("13_settings_language")
//
//        device.pressBack()
//
//        onView(withText(R.string.Settings_currency)).perform(click())
//
//        Screengrab.screenshot("14_settings_currency")
//
//        device.pressBack()
//
//        onView(withText(R.string.Settings_sync)).perform(click())
//
//        Screengrab.screenshot("15_settings_sync")
//
//        device.pressBack()
//
//        onView(withText(R.string.Settings_shareData)).perform(click())
//
//        Screengrab.screenshot("16_settings_share_anonymous_data")
//
//        device.pressBack()
//
//        onView(withText(R.string.Settings_about)).perform(click())
//
//        Screengrab.screenshot("17_settings_about")
//
//        device.pressBack()
//
//        device.pressBack()
//
//        onView(withId(R.id.nav_send)).perform(click())
//
//        onView(withId(R.id.amount_edit)).perform(click())
//
//        Screengrab.screenshot("18_transaction_send")
//
//        onView(withId(R.id.close_button)).perform(click())
//
//        onView(withId(R.id.nav_receive)).perform(click())
//
//        Screengrab.screenshot("19_transaction_receive")
//
//        onView(withId(R.id.close_button)).perform(click())
//
////        onView(withId(R.id.nav_buy)).perform(click())
//
////        Screengrab.screenshot("20_transaction_buy")
//
//        onView(withId(R.id.nav_history)).perform(click())
//
//        onView(withId(R.id.menuBut)).perform(click())
//
//        onView(withText(R.string.MenuButton_lock)).perform(click())
//
//        Screengrab.screenshot("21_lock_screen")

    }

}