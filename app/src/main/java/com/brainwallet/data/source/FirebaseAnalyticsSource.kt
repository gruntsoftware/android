package com.brainwallet.data.source

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.core.annotation.Single

@Single
class FirebaseAnalyticsSource(
    private val context: Context,
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)
) : AnalyticsSource {

    override fun logEvent(event: String) {
        analytics.logEvent(event, null)
    }

    override fun logEventWithParams(event: String, params: Map<String, Any?>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                    // add more types if needed
                }
            }
        }
        analytics.logEvent(event, bundle)
    }
}
