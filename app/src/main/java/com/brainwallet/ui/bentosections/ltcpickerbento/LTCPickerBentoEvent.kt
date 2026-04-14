package com.brainwallet.ui.bentosections.ltcpickerbento

import com.brainwallet.data.model.GlobalCurrency

sealed class LTCPickerBentoEvent {
    data object OnLoad : LTCPickerBentoEvent()
    data class OnGlobalCurrencyChange(val globalCurrency: GlobalCurrency) : LTCPickerBentoEvent()
    data class OnLiveCurrencyUpdate(val globalCurrency: GlobalCurrency) : LTCPickerBentoEvent()
}
