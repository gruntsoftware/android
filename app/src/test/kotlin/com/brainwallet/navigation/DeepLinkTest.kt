package com.brainwallet.navigation

import android.app.Activity
import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `SendToAddress open restarts BrainwalletActivity on Unlock carrying the address`() {
        // openComposeScreen is @JvmStatic, which generates a real static forwarding method
        // that mockkObject's instance-based interception doesn't cover - mockkStatic is what's
        // needed to intercept that.
        mockkStatic(LegacyNavigation::class)
        every { LegacyNavigation.openComposeScreen(any(), any()) } returns mockk<Intent>(relaxed = true)

        val activity = mockk<Activity>(relaxed = true)

        DeepLink.SendToAddress("LQRScannedAddr").open(activity)

        val routeSlot = slot<Route>()
        verify(exactly = 1) { LegacyNavigation.openComposeScreen(activity, capture(routeSlot)) }
        assertEquals(Route.UnLock(pendingSendAddress = "LQRScannedAddr"), routeSlot.captured)
    }

    @Test
    fun `sendToAddress factory produces an equal SendToAddress instance`() {
        assertEquals(DeepLink.SendToAddress("LQRScannedAddr"), DeepLink.sendToAddress("LQRScannedAddr"))
    }
}
