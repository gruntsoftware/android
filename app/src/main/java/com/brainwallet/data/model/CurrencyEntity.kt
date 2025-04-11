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
) : Serializable {
//    @JvmField
//    var code: String? = null
//    @JvmField
//    var name: String? = null
//    @JvmField
//    var rate: Float = 0f
//    @JvmField
//    var symbol: String? = null
//
//    constructor(code: String?, name: String?, rate: Float, symbol: String?) {
//        this.code = code
//        this.name = name
//        this.rate = rate
//        this.symbol = symbol
//    }
//
//    constructor()
//
//    companion object {
//        //Change this after modifying the class
//        private const val serialVersionUID = 7526472295622776147L
//
//        val TAG: String = CurrencyEntity::class.java.name
//    }
}
