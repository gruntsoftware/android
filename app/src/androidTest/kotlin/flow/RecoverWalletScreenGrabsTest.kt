package flow

import android.Manifest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import timber.log.Timber
import tools.fastlane.screengrab.FalconScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.cleanstatusbar.BluetoothState
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar
import tools.fastlane.screengrab.cleanstatusbar.IconVisibility
import tools.fastlane.screengrab.cleanstatusbar.MobileDataType
import tools.fastlane.screengrab.locale.LocaleTestRule

/**
 * TODO: revisit this, since breaking with new navigation
 *
 * this will require pixel 7 pro
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

    private val uiDevice
        get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun setUp() {
        CleanStatusBar()
            .setBluetoothState(BluetoothState.DISCONNECTED)
            .setMobileNetworkDataType(MobileDataType.LTE)
            .setWifiVisibility(IconVisibility.HIDE)
            .setShowNotifications(false)
            .setClock("0900")
            .setBatteryLevel(100)
            .enable()
    }

    @After
    fun tearDown() {
        CleanStatusBar.disable()
    }


    @OptIn(ExperimentalTestApi::class)
    @Test
    fun onRecoverFlowSuccess() {

        composeTestRule.activityRule.scenario.onActivity {
            Screengrab.setDefaultScreenshotStrategy(FalconScreenshotStrategy(it))
        }

        waitUntilReady()

        uiDevice.waitForIdle(60_000)
        composeTestRule.waitForIdle()

        Screengrab.screenshot("1_welcome_screen")

        composeTestRule.onNode(hasTestTag("buttonRestore")).performClick()

        uiDevice.waitForIdle()

        Screengrab.screenshot("2_input_words_screen")

        //seed words input
        val editTextTags = (0..11).map { index -> "textFieldSeedWord$index" }

        val paperKey = BuildConfig.SCREENGRAB_PAPERKEY

        editTextTags.zip(paperKey).forEachIndexed { index, (textFieldTag, value) ->
            uiDevice.waitForIdle()
            val textForTyping = value.dropLast(1)
            composeTestRule.onNodeWithTag(textFieldTag).onChild().performTextInput(textForTyping)
            composeTestRule.onNodeWithText(value).performClick()
        }

        Screengrab.screenshot("3_input_words_screen_2")

        composeTestRule.onNodeWithTag("buttonRestore").performScrollTo()
        composeTestRule.onNodeWithTag("buttonRestore").assertExists()
        composeTestRule.onNodeWithTag("buttonRestore").performClick()

        uiDevice.waitForIdle()

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("keypad1"))

        repeat(4) {
            uiDevice.waitForIdle()
            composeTestRule.onNodeWithTag("keypad1").assertExists()
            composeTestRule.onNodeWithTag("keypad1").performClick()
        }

        Screengrab.screenshot("4_set_passcode_screen")

        composeTestRule.waitUntilAtLeastOneExists(hasTestTag("keypad1"))

        repeat(4) {
            uiDevice.waitForIdle()
            composeTestRule.onNodeWithTag("keypad1").assertExists()
            composeTestRule.onNodeWithTag("keypad1").performClick()
        }

        Screengrab.screenshot("5_set_passcode_confirm_screen")

        uiDevice.waitForIdle()

        Thread.sleep(1000)

        Screengrab.screenshot("6_main_screen")

        //setting drawer
        onView(withId(R.id.menuBut)).perform(click())

        Screengrab.screenshot("7_setting_drawer_open")

        composeTestRule.onNodeWithTag("settingSecurity").assertExists()
        composeTestRule.onNodeWithTag("settingSecurity").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("8_setting_drawer_open_security")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settingLanguage").assertExists()
        composeTestRule.onNodeWithTag("settingLanguage").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("9_setting_drawer_open_language")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settingCurrency").assertExists()
        composeTestRule.onNodeWithTag("settingCurrency").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("10_setting_drawer_open_currency")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settingGames").assertExists()
        composeTestRule.onNodeWithTag("settingGames").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("11_setting_drawer_open_games")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lazyColumnSetting").performScrollToNode(hasTestTag("settingBlockchain"))
        composeTestRule.onNodeWithTag("settingBlockchain").assertExists()
        composeTestRule.onNodeWithTag("settingBlockchain").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("12_setting_drawer_open_blockchain")

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("lazyColumnSetting").performScrollToNode(hasTestTag("settingLock"))
        composeTestRule.onNodeWithTag("settingLock").assertExists()
        composeTestRule.onNodeWithTag("settingLock").performClick()

        composeTestRule.waitForIdle()

        Screengrab.screenshot("13_setting_drawer_lock")

        repeat(4) {
            uiDevice.waitForIdle()
            composeTestRule.onNodeWithTag("keypad1").assertExists()
            composeTestRule.onNodeWithTag("keypad1").performClick()
        }

        uiDevice.waitForIdle()
        Thread.sleep(1000)


        //tx send ui
        onView(withId(R.id.nav_send)).perform(click())

        onView(withId(R.id.amount_edit)).perform(click())

        Screengrab.screenshot("14_transaction_send")

        onView(withId(R.id.close_button)).perform(click())


        //tx buy/receive ui
        onView(withId(R.id.nav_receive)).perform(click())

        Screengrab.screenshot("15_transaction_buy_receive")

        composeTestRule.onNodeWithTag("buttonClose").performClick()

        uiDevice.waitForIdle()

    }

    private fun waitUntilReady() {
        var attempts = 0
        val maxAttempts = 5

        while (attempts < maxAttempts) {
            try {
                composeTestRule.waitUntil(timeoutMillis = 60_000) {
                    try {
                        composeTestRule.onRoot().fetchSemanticsNode()
                        true
                    } catch (e: Exception) {
                        Timber.e("[waitForComposeHierarchy] Error1 : ${e.message}", e)
                        false
                    }
                }
                return
            } catch (e: Exception) {
                Timber.e("[waitForComposeHierarchy] Error2 : ${e.message}", e)
                attempts++
                if (attempts >= maxAttempts) throw e
                Thread.sleep(1000)
            }
        }
    }

}