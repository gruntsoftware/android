package com.brainwallet.ui.bentosections.shopbento

data class ShopBentoState(
    val cardData: String = "",
    val regionData: String = "",
    val countryIso: String = "US",
    val darkMode: Boolean = true,
    val shouldSlide: Boolean = false,
    val shopBaseUrl: String = ""
)
