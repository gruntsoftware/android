package com.brainwallet.initializer

import org.junit.Assert.assertTrue
import org.junit.Test
import com.grunt.brainwallet.iap.BuildConfig as IapBuildConfig

class RevenueCatKeyTest {

    @Test
    fun `REVENUECAT_KEY is set`() {
        assertTrue("REVENUECAT_KEY missing", IapBuildConfig.REVENUECAT_KEY.isNotBlank())
    }
}
