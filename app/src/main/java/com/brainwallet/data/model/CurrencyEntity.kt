package com.brainwallet.data.model

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class CurrencyEntity(
    @JvmField
    var code: String ="",
    @JvmField
    var name: String = "",
    @JvmField
    @SerialName("n")
    var rate: Float = 0F,
    @JvmField
    var symbol: String = ""
) : Serializable