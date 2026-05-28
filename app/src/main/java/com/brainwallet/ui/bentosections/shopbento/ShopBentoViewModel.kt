package com.brainwallet.ui.bentosections.shopbento

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import java.util.Locale
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.ShopProxyRepository
import com.brainwallet.tools.manager.AnalyticsManager

@KoinViewModel
class ShopBentoViewModel(
    private val app: Application,
    private val settingRepository: SettingRepository,
    private val shopProxyRepository: ShopProxyRepository
) : BrainwalletViewModel<ShopBentoEvent>() {

    private val _state = MutableStateFlow(ShopBentoState())
    val state: StateFlow<ShopBentoState> = _state.asStateFlow()

    val currentCountryISO: String = Locale.getDefault().country.ifEmpty { "US" }
    init {
        viewModelScope.launch {
            settingRepository.settings.collect { setting ->
                _state.update {
                    it.copy(
                        darkMode = setting.isDarkMode,
                        countryIso = currentCountryISO
                    )
                }
            }
        }
        viewModelScope.launch {
            shopProxyRepository.refresh()
            shopProxyRepository.shopProxy.collect { shopList ->
                val widget = shopList.firstOrNull()?.widget.orEmpty()
                val cards = shopList.firstOrNull()?.shopCards.orEmpty()
                    .filter { it.countryCode == currentCountryISO }
                var imageUrl1 = ""
                var imageUrl2 = ""
                var imageUrl3 = ""

                if (cards.count() >= 3) {
                    imageUrl1 = cards[0].cardImageWebP
                    imageUrl2 = cards[1].cardImageWebP
                    imageUrl3 = cards[2].cardImageWebP
                }
                _state.update {
                    it.copy(
                        shopBaseUrl = widget,
                        shopCards = cards,
                        cardImageURL1 = imageUrl1,
                        cardImageURL2 = imageUrl2,
                        cardImageURL3 = imageUrl3
                    )
                }
            }
        }
    }

    override fun onEvent(event: ShopBentoEvent) {
        when (event) {
            is ShopBentoEvent.OnLoad -> {
            }
            is ShopBentoEvent.OnTapShop -> {
                _state.update {
                    it.copy(
                        shouldSlide = true
                    )
                }
            }
            is ShopBentoEvent.InvoiceCreated -> {
                AnalyticsManager
                    .logCustomEventWithParams(
                        "user_shop_invoice_created",
                        null
                    )
            }
        }
    }
}
