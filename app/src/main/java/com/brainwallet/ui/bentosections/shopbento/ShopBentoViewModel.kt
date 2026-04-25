package com.brainwallet.ui.bentosections.shopbento

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.Locale
import android.telephony.TelephonyManager
@KoinViewModel
class ShopBentoViewModel(
    private val app: Application,
) : BrainwalletViewModel<ShopBentoEvent>() {

    private val _state = MutableStateFlow(ShopBentoState())
    val state: StateFlow<ShopBentoState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    countryIso = getCountryIso()
                )
            }
        }
    }
    private fun getCountryIso(): String {
        val telephonyManager = app.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simCountry = telephonyManager.simCountryIso
        val networkCountry = telephonyManager.networkCountryIso

        return when {
            simCountry.isNotEmpty() -> simCountry.uppercase()
            networkCountry.isNotEmpty() -> networkCountry.uppercase()
            else -> Locale.getDefault().country.ifEmpty { "US" }
        }
    }
    override fun onEvent(event: ShopBentoEvent) {
        when (event) {
            is ShopBentoEvent.OnLoad -> {
            }
            is ShopBentoEvent.OnTapShop -> {
            }
        }
    }
}
