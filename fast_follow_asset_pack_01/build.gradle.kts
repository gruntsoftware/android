plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("fast_follow_asset_pack_01")
    dynamicDelivery {
        deliveryType.set("fast-follow")
    }
}