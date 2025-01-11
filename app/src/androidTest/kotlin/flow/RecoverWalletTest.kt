package flow

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
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

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(IntroActivity::class.java)

    @Test
    fun onChangeLanguageThenYes() {
        activityScenarioRule.scenario.onActivity {
            Screengrab.setDefaultScreenshotStrategy(FalconScreenshotStrategy(it))
        }

        Screengrab.screenshot("intro_screen_1")

        onView(withId(R.id.button_recover_wallet)).perform(click())

        Screengrab.screenshot("intro_screen_2")

        onView(withId(R.id.send_button)).perform(click()) //actually next button

        Screengrab.screenshot("intro_screen_3")

        //TODO

    }

}