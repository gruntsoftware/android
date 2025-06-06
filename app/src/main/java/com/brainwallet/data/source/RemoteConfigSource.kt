package com.brainwallet.data.source

import com.brainwallet.BuildConfig
import com.brainwallet.R
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import timber.log.Timber

interface RemoteConfigSource {

    companion object {
        const val KEY_FEATURE_MENU_HIDDEN_EXAMPLE = "feature_menu_hidden_example"
        const val KEY_FEATURE_SELECTED_PEERS_ENABLED = "feature_selected_peers_enabled"
    }

    fun initialize()
    fun getString(key: String): String
    fun getNumber(key: String): Double
    fun getBoolean(key: String): Boolean

    class FirebaseImpl(
        private val remoteConfig: FirebaseRemoteConfig
    ) : RemoteConfigSource {

        init {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                    0 // fetch every time in debug mode
                } else {
                    60 * 180 // fetch every 3 hours in production mode
                }
            }
            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        }

        override fun initialize() {
            remoteConfig.fetchAndActivate()
                .addOnSuccessListener { Timber.d("timber: RemoteConfig Success fetchAndActivate") }
                .addOnFailureListener {
                    Timber.d(
                        it,
                        "timber: RemoteConfig Failure fetchAndActivate"
                    )
                }
            remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Timber.d("timber: [RemoteConfig] onUpdate ${configUpdate.updatedKeys}")
                }

                override fun onError(error: FirebaseRemoteConfigException) {
                    Timber.d("timber: [RemoteConfig] onError ${error.code} | ${error.message}")
                }

            })
        }

        override fun getString(key: String): String {
            return remoteConfig.getString(key)
        }

        override fun getNumber(key: String): Double {
            return remoteConfig.getDouble(key)
        }

        override fun getBoolean(key: String): Boolean {
            return remoteConfig.getBoolean(key)
        }
    }
}