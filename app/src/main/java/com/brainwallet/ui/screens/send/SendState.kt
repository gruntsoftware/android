package com.brainwallet.ui.screens.send

import com.brainwallet.data.model.CurrencyEntity
import java.math.BigDecimal

data class SendState(
    val biometricEnabled: Boolean = false,
    val darkMode: Boolean = false,
    val formattedCurrency: String = "$ USD",
    val recipientLTCAddress: String = "",
    val userMemorandum: String = "",
    val networkFees: BigDecimal = BigDecimal(0),
    val serviceFees: BigDecimal = BigDecimal(0),
    val amountInLitoshi: BigDecimal = BigDecimal(0),
    val amountString: String = "",
    val userViewsFiat: Boolean = false,
    val isReadyToSend: Boolean = false,
    val isLTCAddressValid: Boolean = false,
    val isAmountBelowBalance: Boolean = false,
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    )
)
