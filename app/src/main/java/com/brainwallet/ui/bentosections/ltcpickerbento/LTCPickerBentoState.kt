package com.brainwallet.ui.bentosections.ltcpickerbento

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import kotlin.text.lowercase

data class LTCPickerBentoState(
    val darkMode: Boolean = true,
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ),
    val iso: String = "USD",
    val formattedFiat: String = "",
    val formattedTimeStamp: String = "",
    var globalCurrencies: List<GlobalCurrency> = GlobalCurrency.entries,
    var selectedGlobalCurrency: GlobalCurrency = GlobalCurrency.USD
)
fun LTCPickerBentoState.getSelectedFiatRateIndex(): Int = globalCurrencies
    .indexOfFirst { it.code.lowercase() == selectedCurrency.code.lowercase() }
