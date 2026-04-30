package com.brainwallet.data.source

interface RemoteConfigSource {

    suspend fun fetchAndActivate(): Boolean

    companion object {
        const val KEY_FEATURE_MENU_HIDDEN_EXAMPLE = "feature_menu_hidden_example"
        const val KEY_FEATURE_SELECTED_PEERS_ENABLED = "feature_selected_peers_enabled"
        const val KEY_FEATURE_GAMEHUB_CONTENT = "feature_gamehub_content"
        const val PATH_SHOP_CONTENT = "path_shop_content"
        const val KEY_SYNC_POLLER = "key_sync_poller"
        const val KEY_API_BASEURL_PROD_NEW_ENABLED = "key_api_baseurl_prod_new_enabled"
        const val KEY_FEATURE_LTC_BROWSER_CONTENT = "feature_ltc_browser_content"
        const val KEY_KEYSTORE_MANAGER_ENABLED = "key_keystore_manager_enabled"
        const val KEY_DEV_API_BASEURL = "key_dev_api_baseurl"
        const val KEY_API_BASEURL_DEV_NEW_ENABLED = "key_api_baseurl_dev_new_enabled"
        const val KEY_PROD_API_BASEURL = "key_prod_api_baseurl"
    }

    fun initialize()
    fun getString(key: String): String
    fun getNumber(key: String): Double
    fun getBoolean(key: String): Boolean
}
