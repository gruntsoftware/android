package com.brainwallet.ui.screens.send

import java.math.BigDecimal

data class SendState(
    val biometricEnabled: Boolean = false,
    val darkMode: Boolean = false,
    val formattedCurrency: String = "$ USD",
    val recipientLTCAddress: String = "",
    val amountInFiat: String = "",
    val userMemorandum: String = "",
    val networkFees: BigDecimal = BigDecimal(0),
    val serviceFees: BigDecimal = BigDecimal(0),
    val amountInLTCString: String = "",
    val userViewsFiat: Boolean = false,
    val isReadyToSend: Boolean = false,
    val isLTCAddressValid: Boolean = false,
    val isAmountBelowBalance: Boolean = false
)
