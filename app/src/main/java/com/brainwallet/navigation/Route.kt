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
    object Main : Route()

    @Serializable
    data class UnLock(val isUpdatePin: Boolean = false) : Route()

    @Serializable
    object BuyReceive : Route()

    @Serializable
    object History : Route()

    @Serializable
    object Send : Route()

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
