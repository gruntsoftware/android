package com.brainwallet.data.source

interface AnalyticsSource {
    fun logEvent(event: String)
    fun logEventWithParams(event: String, params: Map<String, Any?>)
}
