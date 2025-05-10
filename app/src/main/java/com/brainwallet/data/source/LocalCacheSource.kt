package com.brainwallet.data.source

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.di.json
import kotlinx.serialization.encodeToString

/**
 * Generic function to handle caching data in shared preferences
 * @param key The key to store the data under
 * @param cachedAtKey The key to store the timestamp under
 * @param cacheTimeMs How long the cache should be valid in milliseconds
 * @param fetchData Suspending function to fetch new data
 * @return The data, either from cache or freshly fetched
 */
suspend inline fun <reified T> SharedPreferences.fetchWithCache(
    key: String,
    cachedAtKey: String,
    cacheTimeMs: Long,
    crossinline fetchData: suspend () -> T,
    defaultValue: T? = null
): T {
    val lastUpdateTime = getLong(cachedAtKey, 0)
    val currentTime = System.currentTimeMillis()
    val cached = getString(key, null)
        ?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() }

    // Return cached value if it exists and is not expired
    if (cached != null && (currentTime - lastUpdateTime) < cacheTimeMs) {
        return cached
    }

    return runCatching {
        // Fetch fresh data
        val result = fetchData()
        // Save to cache
        edit {
            putString(key, json.encodeToString(result))
            putLong(cachedAtKey, currentTime)
        }
        result
    }.getOrElse {
        cached ?: (defaultValue ?: throw it)
    }
}