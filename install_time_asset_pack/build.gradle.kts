plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("install_time_asset_pack")
    dynamicDelivery {
        deliveryType.set("install-time")
    }
}