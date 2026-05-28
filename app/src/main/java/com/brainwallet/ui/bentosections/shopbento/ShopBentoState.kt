package com.brainwallet.ui.bentosections.shopbento

import com.brainwallet.presenter.entities.ShopCard

data class ShopBentoState(
    val cardData: String = "",
    val shopCards: List<ShopCard> = emptyList(),
    val regionData: String = "",
    val countryIso: String = "US",
    val darkMode: Boolean = true,
    val shouldSlide: Boolean = false,
    val shopBaseUrl: String = "",
    val cardImageURL1: String = "",
    val cardImageURL2: String = "",
    val cardImageURL3: String = ""
)
