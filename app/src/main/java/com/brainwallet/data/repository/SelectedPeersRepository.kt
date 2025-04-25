package com.brainwallet.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.di.json
import kotlinx.serialization.json.jsonObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.IOException
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface SelectedPeersRepository {

    suspend fun fetchSelectedPeers(): Set<String>

    class Impl(
        private val okHttpClient: OkHttpClient,
        private val sharedPreferences: SharedPreferences,
    ) : SelectedPeersRepository {

        private companion object {
            const val PREF_KEY_SELECTED_PEERS = "selected_peers"
            const val PREF_KEY_SELECTED_PEERS_CACHED_AT = "${PREF_KEY_SELECTED_PEERS}_cached_at"
        }

        override suspend fun fetchSelectedPeers(): Set<String> {
            val lastUpdateTime = sharedPreferences.getLong(PREF_KEY_SELECTED_PEERS_CACHED_AT, 0)
            val currentTime = System.currentTimeMillis()
            val cachedPeers = sharedPreferences.getStringSet(PREF_KEY_SELECTED_PEERS, null)

            // Check if cache exists and is less than 6 hours old
            if (!cachedPeers.isNullOrEmpty() && (currentTime - lastUpdateTime) < 6 * 60 * 60 * 1000) {
                return cachedPeers
            }

            val request = okhttp3.Request.Builder()
                .url(LITECOIN_NODES_URL)
                .build()

            return suspendCoroutine { continuation ->
                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        continuation.resume(emptySet()) //just return empty if failed or need hardcoded?
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val jsonString = response.body?.string()

                        if (response.isSuccessful.not()) {
                            continuation.resume(cachedPeers ?: emptySet())
                            return
                        }

                        val parsedResult = jsonString?.let {
                            val jsonElement = json.parseToJsonElement(it)
                            val dataObject = jsonElement.jsonObject["data"]?.jsonObject
                            val nodesObject = dataObject?.get("nodes")?.jsonObject

                            //filter criteria
                            val requiredServices = 0x01 or 0x04 // NODE_NETWORK | NODE_BLOOM

                            nodesObject?.entries
                                ?.filter { entry ->
                                    val flags =
                                        entry.value.jsonObject["flags"]?.toString()?.toIntOrNull()
                                    flags != null && (flags and requiredServices) == requiredServices
                                }
                                ?.map { it.key.replace(":9333", "") }
                                ?.toSet().also { Timber.d("Total Selected Peers ${it?.size}") }
                                ?: emptySet()

                        } ?: emptySet()

                        sharedPreferences.edit {
                            putStringSet(PREF_KEY_SELECTED_PEERS, parsedResult)
                            putLong(PREF_KEY_SELECTED_PEERS_CACHED_AT, currentTime)
                        }

                        continuation.resume(parsedResult)
                    }
                })
            }
        }

    }

    companion object {
        const val LITECOIN_NODES_URL = "https://api.blockchair.com/litecoin/nodes"
    }
}