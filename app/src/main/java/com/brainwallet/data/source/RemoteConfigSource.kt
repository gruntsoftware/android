package com.brainwallet.data.source

interface RemoteConfigSource {

    companion object {
        const val KEY_FEATURE_MENU_HIDDEN_EXAMPLE = "feature_menu_hidden_example"
        const val KEY_FEATURE_SELECTED_PEERS_ENABLED = "feature_selected_peers_enabled"
    }

    fun initialize()
    fun getString(key: String): String
    fun getNumber(key: String): Double
    fun getBoolean(key: String): Boolean
}
