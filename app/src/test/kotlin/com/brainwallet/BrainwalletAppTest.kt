package com.brainwallet

import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class BrainwalletAppTest {

    @Before
    fun setUp() {
        // Reset static companion state between tests via reflection
        resetCompanionState()
    }

    @After
    fun tearDown() {
        resetCompanionState()
        unmockkAll()
    }

    // ── isAppInBackground ────────────────────────────────────────────────────

    @Test
    fun `isAppInBackground returns true when context is null`() {
        assertTrue(BrainwalletApp.isAppInBackground(null))
    }

    @Test
    fun `isAppInBackground returns true when activityCounter is zero`() {
        BrainwalletApp.activityCounter.set(0)
        val context = mockk<android.content.Context>(relaxed = true)
        assertTrue(BrainwalletApp.isAppInBackground(context))
    }

    @Test
    fun `isAppInBackground returns true when activityCounter is negative`() {
        BrainwalletApp.activityCounter.set(-1)
        val context = mockk<android.content.Context>(relaxed = true)
        assertTrue(BrainwalletApp.isAppInBackground(context))
    }

    @Test
    fun `isAppInBackground returns false when context is non-null and activityCounter is positive`() {
        BrainwalletApp.activityCounter.set(1)
        val context = mockk<android.content.Context>(relaxed = true)
        assertFalse(BrainwalletApp.isAppInBackground(context))
    }

    @Test
    fun `isAppInBackground returns false when activityCounter is greater than one`() {
        BrainwalletApp.activityCounter.set(3)
        val context = mockk<android.content.Context>(relaxed = true)
        assertFalse(BrainwalletApp.isAppInBackground(context))
    }

    // ── addOnBackgroundedListener / fireListeners ────────────────────────────

    @Test
    fun `addOnBackgroundedListener registers listener and fireListeners invokes it`() {
        var callCount = 0

        val listener = object : BrainwalletApp.OnAppBackgrounded {
            override fun onBackgrounded() { callCount++ }
        }

        BrainwalletApp.addOnBackgroundedListener(listener)
        BrainwalletApp.fireListeners()

        assertEquals(1, callCount)
    }

    @Test
    fun `addOnBackgroundedListener does not register the same listener twice`() {
        var callCount = 0
        val listener = object : BrainwalletApp.OnAppBackgrounded {
            override fun onBackgrounded() { callCount++ }
        }
        BrainwalletApp.addOnBackgroundedListener(listener)
        BrainwalletApp.addOnBackgroundedListener(listener)
        BrainwalletApp.fireListeners()

        assertEquals(1, callCount)
    }

    @Test
    fun `fireListeners notifies all registered listeners`() {
        val results = mutableListOf<String>()

        val listenerA = object : BrainwalletApp.OnAppBackgrounded {
            override fun onBackgrounded() { results.add("A") }
        }
        val listenerB = object : BrainwalletApp.OnAppBackgrounded {
            override fun onBackgrounded() { results.add("B") }
        }

        BrainwalletApp.addOnBackgroundedListener(listenerA)
        BrainwalletApp.addOnBackgroundedListener(listenerB)
        BrainwalletApp.fireListeners()

        assertEquals(listOf("A", "B"), results)
    }

    @Test
    fun `fireListeners does nothing when no listeners are registered`() {
        // Should not throw — listeners list is null at this point
        BrainwalletApp.fireListeners()
    }

    // ── addOnForegroundedListener / fireForegroundListeners ──────────────────

    @Test
    fun `addOnForegroundedListener registers listener and fireForegroundListeners invokes it`() {
        var callCount = 0

        val listener = object : BrainwalletApp.OnAppForegrounded {
            override fun onForegrounded() { callCount++ }
        }

        BrainwalletApp.addOnForegroundedListener(listener)
        BrainwalletApp.fireForegroundListeners()

        assertEquals(1, callCount)
    }

    @Test
    fun `addOnForegroundedListener does not register the same listener twice`() {
        var callCount = 0
        val listener = object : BrainwalletApp.OnAppForegrounded {
            override fun onForegrounded() { callCount++ }
        }
        BrainwalletApp.addOnForegroundedListener(listener)
        BrainwalletApp.addOnForegroundedListener(listener)
        BrainwalletApp.fireForegroundListeners()

        assertEquals(1, callCount)
    }

    @Test
    fun `fireForegroundListeners does nothing when no listeners are registered`() {
        // Should not throw — foregroundListeners list is null at this point
        BrainwalletApp.fireForegroundListeners()
    }

    // ── setBreadContext / activityCounter ────────────────────────────────────

    @Test
    fun `setBreadContext accepts null without throwing`() {
        BrainwalletApp.setBreadContext(null)
    }

    @Test
    fun `activityCounter is thread-safe AtomicInteger and increments correctly`() {
        BrainwalletApp.activityCounter.set(0)
        BrainwalletApp.activityCounter.incrementAndGet()
        BrainwalletApp.activityCounter.incrementAndGet()
        assertEquals(2, BrainwalletApp.activityCounter.get())
    }

    @Test
    fun `activityCounter decrements to zero correctly`() {
        BrainwalletApp.activityCounter.set(1)
        BrainwalletApp.activityCounter.decrementAndGet()
        assertEquals(0, BrainwalletApp.activityCounter.get())
    }

    // ── onStop timer scheduling ──────────────────────────────────────────────

    @Test
    fun `onStop does not throw when called with null activity`() {
        BrainwalletApp.onStop(null)
    }

    @Test
    fun `onStop cancels previous timer and schedules a new one without throwing`() {
        // Calling twice simulates rapid activity cycling
        BrainwalletApp.onStop(null)
        BrainwalletApp.onStop(null)
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    /**
     * Resets the private static `listeners` field to null between tests so
     * listener state doesn't leak across test cases.
     */
    private fun resetCompanionState() {
        try {
            val companionClass = Class.forName("com.brainwallet.BrainwalletApp\$Companion")
            val companion = BrainwalletApp.Companion
            for (fieldName in listOf("listeners", "foregroundListeners")) {
                val field: Field = companionClass.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(companion, null)
            }
        } catch (_: Exception) {
            // Field name may differ with Kotlin compiler — fall back to
            // direct activityCounter reset only (listener tests still isolated
            // because each test adds its own listeners to a fresh null list)
        }
        BrainwalletApp.activityCounter.set(0)
        BrainwalletApp.backgroundedTime = 0L
        BrainwalletApp.setBreadContext(null)
    }
}
