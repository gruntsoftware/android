package com.brainwallet.initializer

import org.junit.Assert.assertTrue
import org.junit.Test
import com.grunt.brainwallet.iap.BuildConfig as IapBuildConfig

/**
 * Guards against shipping a build without a RevenueCat key.
 *
 * The :iap module bakes REVENUECAT_KEY from local.properties into its BuildConfig (blank when
 * missing), and IapInitializer passes it to Purchases.configure() during app startup. A blank
 * key makes RevenueCat throw "API key must be set" inside InitializationProvider, which crashes
 * every launch (Crashlytics ca4b0596b4a6eb1cfde6a8e80319c1f0, v4.14.0 / 202506366).
 *
 * To run locally, add REVENUECAT_KEY=<key> to local.properties. CI writes it from the
 * REVENUECAT_KEY environment variable.
 */
class RevenueCatKeyTest {

    private val key = IapBuildConfig.REVENUECAT_KEY

    @Test
    fun `REVENUECAT_KEY is set`() {
        assertTrue(
            "REVENUECAT_KEY is missing or blank. Add it to local.properties " +
                "(CI: set the REVENUECAT_KEY environment variable).",
            key.isNotBlank()
        )
    }

    @Test
    fun `REVENUECAT_KEY is a Google Play public SDK key`() {
        // RevenueCat's Android public SDK keys start with "goog_". Anything else (an iOS
        // "appl_" key, a secret "sk_" key, a stray placeholder) won't work on Android.
        assertTrue(
            "REVENUECAT_KEY must be the Google Play public SDK key (starts with \"goog_\").",
            key.startsWith(GOOGLE_PLAY_KEY_PREFIX)
        )
    }

    private companion object {
        const val GOOGLE_PLAY_KEY_PREFIX = "goog_"
    }
}
