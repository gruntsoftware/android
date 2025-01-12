package flow

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.breadwallet.BuildConfig
import com.breadwallet.R
import com.breadwallet.presenter.activities.intro.IntroActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.FalconScreenshotStrategy
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule


@RunWith(JUnit4::class)
@LargeTest
class RecoverWalletTest {

    @Rule
    @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(IntroActivity::class.java)

    @Test
    fun onRecoverFlowSuccess() {
        activityScenarioRule.scenario.onActivity {
            Screengrab.setDefaultScreenshotStrategy(FalconScreenshotStrategy(it))
        }

        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        Screengrab.screenshot("1_intro_screen")

        onView(withId(R.id.button_recover_wallet)).perform(click())

        Screengrab.screenshot("2_recover_screen")

        onView(withId(R.id.send_button)).perform(click()) //actually next button

        Screengrab.screenshot("3_input_paperkey_screen")

        val editTextIds = listOf(
            R.id.word1,
            R.id.word2,
            R.id.word3,
            R.id.word4,
            R.id.word5,
            R.id.word6,
            R.id.word7,
            R.id.word8,
            R.id.word9,
            R.id.word10,
            R.id.word11,
            R.id.word12
        )
        val paperKey = BuildConfig.SCREENGRAB_PAPERKEY

        editTextIds.zip(paperKey).forEachIndexed { index, (editTextId, value) ->
            onView(withId(editTextId)).perform(typeText(value))
            device.pressEnter()

        }

        Screengrab.screenshot("4_input_paperkey_screen_2")

        Screengrab.screenshot("5_setpin_screen")

        repeat(6) {
            onView(withId(R.id.num1)).perform(click())
        }

        Screengrab.screenshot("6_setpin_confirm_screen")

        repeat(6) {
            onView(withId(R.id.num1)).perform(click())
        }

        //todo

        Screengrab.screenshot("7_main_screen")
    }

}