package com.brainwallet.ui.bentosections.ltcpickerbento

import android.content.Context
import com.brainwallet.data.model.GlobalCurrency

sealed class LTCPickerBentoEvent {
    data class OnLoad(val context: Context) : LTCPickerBentoEvent()
    data class OnGlobalCurrencyChange(val globalCurrency: GlobalCurrency) : LTCPickerBentoEvent()
}
