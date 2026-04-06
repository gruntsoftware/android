package com.brainwallet.ui.screens.send

data class SendState(
    val biometricEnabled: Boolean = false,
    val darkMode: Boolean = false,
    val formattedCurrency: String = "$ USD",
    val recipientLTCAddress: String = "ltc1qzfcf9ust6cadla80umk7faupf8lp92rjm3ghfj",
    val amountInFiat: String = "$34333.00",
    val memo: String = "",
    val networkFees: Float = 0.044430F,
    val serviceFees: Float = 34311.09F,
    val amountInLTC: Float = 2323.0F,
    val userViewsFiat: Boolean = false,
    val isReadyToSend: Boolean = false,
    val isLTCAddressValue: Boolean = false,
    val isAmountBelowBalance: Boolean = false
)
