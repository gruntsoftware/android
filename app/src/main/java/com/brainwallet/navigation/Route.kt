package com.brainwallet.navigation

import kotlinx.serialization.Serializable
import java.io.Serializable as JavaSerializable


sealed class Route : JavaSerializable {

    @Serializable
    object Welcome : Route()

    @Serializable
    object Ready : Route()

    @Serializable
    object SetPasscode : Route()

    @Serializable
    object SetPasscodeConfirm : Route()

    @Serializable
    data class InputWords(val source: Source? = null) : Route() {
        enum class Source {
            RESET_PIN,
            SETTING_WIPE
        }
    }

    @Serializable
    data class YourSeedWords(val seedWords: List<String>) : Route()

    @Serializable
    data class YourSeedProveIt(val seedWords: List<String>) : Route()

//    for now, still using old activity & fragment
//    @Serializable
//    object Home : Route()

    @Serializable
    object UnLock : Route()
}