package com.brainwallet.data.source

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class FirebaseAnalyticsSourceTest {

    @RelaxedMockK
    private lateinit var context: Context

    @MockK
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var sut: FirebaseAnalyticsSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        sut = FirebaseAnalyticsSource(context, firebaseAnalytics)
    }

    @Test
    fun `given valid event name when logCustomEvent is called then event should be logged without params`() {
        val eventName = "test_event"

        sut.logEvent(eventName)

        verify(exactly = 1) { firebaseAnalytics.logEvent(eventName, null) }
        assert(true) { "Expected logEvent to be called with eventName=$eventName and params=null" }
    }

    @Test
    fun `given valid event name and params when logCustomEventWithParams is called then event should be logged with params`() {
        val eventName = "test_event_with_params"

        sut.logEventWithParams(eventName, mapOf())

        verify(exactly = 1) { firebaseAnalytics.logEvent(eventName, any()) }
        assert(true) { "Expected logEvent to be called with eventName=$eventName and provided params" }
    }
}
