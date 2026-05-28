package com.brainwallet.ui.bentosections.shopbento

sealed class ShopBentoEvent {
    data object OnLoad : ShopBentoEvent()
    data object OnTapShop : ShopBentoEvent()

    object InvoiceCreated : ShopBentoEvent()
}
