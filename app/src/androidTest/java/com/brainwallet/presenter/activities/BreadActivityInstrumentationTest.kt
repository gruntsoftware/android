package com.brainwallet.presenter.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression coverage for Crashlytics issue e14e13f05768c2d8ecd7ade23505cccd:
 * [BreadActivity.onDestroy] used to unconditionally call `unregisterReceiver()` on a
 * `mConnectionReceiver` field that was never assigned or registered anywhere in the class,
 * so every single destroy of this activity threw
 * `IllegalArgumentException: Receiver not registered: null`.
 *
 * [BreadActivity] is `@Deprecated` in favor of [com.brainwallet.ui.BrainwalletActivity] and is
 * being phased out, so this intentionally stays a single lifecycle smoke test rather than full
 * behavioral coverage — its remaining logic (litecoin: URI dispatch) already has dedicated
 * coverage in `LitecoinURIHandlerTest`.
 *
 * Requires an emulator/device, same as [com.brainwallet.ui.screens.send.SendInstrumentationTest] —
 * not run as part of the CI unit-test job.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class BreadActivityInstrumentationTest {

    @Test
    fun lifecycle_createResumePauseDestroy_doesNotCrash() {
        // ActivityScenario.launch() drives the activity through CREATED -> STARTED -> RESUMED;
        // .close() drives it back down through PAUSED -> STOPPED -> DESTROYED. If onDestroy()
        // throws (the original bug), this call throws and the test fails.
        ActivityScenario.launch(BreadActivity::class.java).close()
    }

    @Test
    fun recreate_thenDestroy_doesNotCrash() {
        // Simulates a configuration change / process-death recreate, which destroys and
        // recreates the activity in place -- exercises onDestroy() a second time in the same
        // scenario, since the original crash fired on every single destroy.
        ActivityScenario.launch(BreadActivity::class.java).use { scenario ->
            scenario.recreate()
        }
    }
}
