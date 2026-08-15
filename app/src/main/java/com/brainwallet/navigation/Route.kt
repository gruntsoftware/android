package com.brainwallet.navigation

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable

sealed class Route : JavaSerializable {

    @Serializable
    object Welcome : Route()

    @Serializable
    object Ready : Route()

    @Serializable
    object TopUp : Route()

    @Serializable
    object Settings : Route()

    @Serializable
    data class SetPasscode(
        val passcode: List<Int> = emptyList(),
        val isUpdatePin: Boolean = false,
    ) : Route()

    @Serializable
    data class Restore(val source: Source? = null) : Route() {
        enum class Source {
            RESET_PIN,
            SETTING_WIPE
        }
    }

    @Serializable
    data class YourSeedWords(val seedWords: List<String>) : Route()

    @Serializable
    data class YourSeedProveIt(val seedWords: List<String>) : Route()

    @Serializable
    @Immutable
    data class YourEmojis(val emojis: List<String>) : Route()

    @Serializable
    data class Main(
        // Address from a scanned litecoin: QR code, carried through the unlock flow once
        // the PIN is verified and the wallet is fully synced (see BrainwalletActivity.
        // onUnlock) - MainScreen opens its Send modal with this address on load, the same
        // way tapping the bottom nav's Send tab does (BentoBottomNavBar's onItemClick with
        // Route.Send), rather than navigating to Route.Send as a disconnected top-level
        // destination outside the normal Main nav/bottom-bar stack.
        val pendingSendAddress: String? = null
    ) : Route()

    @Serializable
    data class UnLock @JvmOverloads constructor(
        val isUpdatePin: Boolean = false,
        // Address from a scanned litecoin: QR code, carried through the unlock flow so
        // it can be pasted into Send once the PIN is verified - but only if the wallet
        // is fully synced by then (see BrainwalletActivity.onUnlock).
        val pendingSendAddress: String? = null
    ) : Route()

    @Serializable
    object BuyReceive : Route()

    @Serializable
    object History : Route()

    @Serializable
    data class Send(val address: String? = null) : Route()

    @Serializable
    object TutorialWalkthrough : Route()

    @Serializable
    object TutorialSend : Route()

    @Serializable
    object GameHub : Route()

    @Serializable
    object MoonPayBuy : Route()

    @Serializable
    data class BitrefillWeb(val url: String) : Route()

    @Serializable
    object EmojiPickerPager : Route()

    @Serializable
    object HowToSetEmojis : Route()

    @Serializable
    object PickEmojis : Route()

    @Serializable
    object LinktreeWeb : Route()
}
