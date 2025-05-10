package com.brainwallet.data.model


data class QuickFiatAmountOption(
    val symbol: String = "custom",
    val value: Float = -1f
)

fun QuickFiatAmountOption.isCustom(): Boolean = symbol == "custom" && value == -1f

fun QuickFiatAmountOption.getFormattedText(): String = "${symbol}${value.toInt()}"