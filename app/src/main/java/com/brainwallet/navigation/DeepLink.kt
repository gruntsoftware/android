package com.brainwallet.navigation

import android.app.Activity

/**
 * A deep link resolved from a source outside the Compose tree - today, specifically a
 * litecoin: QR code scanned by the device camera/another app and resolved via
 * BreadActivity's manifest litecoin: scheme intent-filter (see
 * com.brainwallet.tools.util.LitecoinURIHandler, which owns URI parsing/address validation
 * and constructs these once an address checks out).
 *
 * Routes entirely through Compose Navigation - [open] restarts the single BrainwalletActivity
 * instance on [Route.UnLock] carrying the pending address; once the PIN is verified *and* the
 * wallet is fully synced (checked at that moment, not when the link was opened - see
 * BrainwalletActivity.onUnlock), it continues on to [Route.Main] carrying that same address as
 * `pendingSendAddress`, which MainScreen picks up to open its Send modal on load - the same
 * modal state tapping the bottom nav's Send tab sets - with the address already pasted in.
 * Never a standalone top-level Send destination (there is no `composable<Route.Send>` in
 * MainNavigationHost - see its comment) and never a second, disconnected Activity -
 * [LegacyNavigation.restartBrainwalletActivity] (which [LegacyNavigation.openComposeScreen]
 * eventually reaches for [Route.UnLock]) clears the task down to one.
 *
 * This intentionally never touches [com.brainwallet.util.EventBus] - see that class's
 * deprecation note on `postQRCodeScanned`/`Event.QRCodeScanned`, which remains only as the
 * bridge for the Send screen's own in-app "Scan" button (a different, narrower scenario: a
 * legacy Java Activity callback posting into an already-open Compose screen, not a deep link
 * arriving from outside the app).
 */
sealed class DeepLink {

    /** A verified litecoin: address to land the user on Send with, once authenticated. */
    data class SendToAddress(val address: String) : DeepLink()

    /**
     * Opens this deep link. See the class doc for the full Unlock -> Send routing sequence.
     */
    fun open(from: Activity) {
        when (this) {
            is SendToAddress -> LegacyNavigation.openComposeScreen(
                from,
                Route.UnLock(pendingSendAddress = address)
            )
        }
    }

    companion object {
        /** Java-friendly factory - see [SendToAddress]. */
        @JvmStatic
        fun sendToAddress(address: String): DeepLink = SendToAddress(address)
    }
}
