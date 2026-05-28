package com.brainwallet.presenter.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShopConfig(
    @SerialName("shop_data")
    val shopData: ShopData
)

@Serializable
data class ShopData(
    @SerialName("widget_url")
    val widgetUrl: String,
    val cards: List<ShopCard>
)

@Serializable
data class ShopCard(
    @SerialName("country_code")
    val countryCode: String,

    @SerialName("country_name")
    val countryName: String,

    @SerialName("product_slug")
    val productSlug: String,

    @SerialName("product_name")
    val productName: String,

    @SerialName("product_url")
    val productUrl: String,

    @SerialName("card_image_webp")
    val cardImageWebP: String
) {
    // Computed property — same as Swift's `var id`
    val id: String get() = productSlug
}
