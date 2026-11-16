package com.brainwallet.ui.screens.main

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.brainwallet.R
import com.brainwallet.ui.theme.DesignTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The button shows the open lock (labelled with its action, "Lock") until tapped. 400ms after the
 * tap it crossfades to the closed lock ("Unlock") and calls onLock once the fade finishes.
 * Time is driven by the compose test clock, so the delays are exact and nothing sleeps.
 */
@RunWith(AndroidJUnit4::class)
class LockButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var lockCalls = 0
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val openLockLabel = resources.getString(R.string.settings_title_lock)
    private val closedLockLabel = resources.getString(R.string.settings_title_unlock)

    @Before
    fun setUp() {
        lockCalls = 0
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            DesignTheme(false) {
                LockButton(modifier = Modifier.testTag(TAG)) { lockCalls++ }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
    }

    @Test
    fun startsUnlocked() {
        composeRule.onNodeWithContentDescription(openLockLabel).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(closedLockLabel).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(TOTAL_MS * 2)
        assertEquals("Doesn't lock without a tap", 0, lockCalls)
    }

    @Test
    fun keepsTheUnlockIconForTheFirst400ms() {
        composeRule.onNodeWithTag(TAG).performClick()
        composeRule.mainClock.advanceTimeBy(LOCK_BUTTON_DELAY_MS - FRAME_MS)

        composeRule.onNodeWithContentDescription(openLockLabel).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(closedLockLabel).assertDoesNotExist()
        assertEquals(0, lockCalls)
    }

    @Test
    fun turnsIntoTheLockAfter400msBeforeLocking() {
        composeRule.onNodeWithTag(TAG).performClick()
        // Past the delay, part way through the crossfade.
        composeRule.mainClock.advanceTimeBy(LOCK_BUTTON_DELAY_MS + LOCK_BUTTON_ANIMATION_MS / 2)

        composeRule.onNodeWithContentDescription(closedLockLabel).assertExists()
        assertEquals("Waits for the animation to finish", 0, lockCalls)
    }

    @Test
    fun locksOnceTheAnimationFinishes() {
        composeRule.onNodeWithTag(TAG).performClick()
        composeRule.mainClock.advanceTimeBy(TOTAL_MS + FRAME_MS)
        assertEquals(1, lockCalls)

        // Crossfade drops the outgoing icon a few frames after the fade ends.
        composeRule.mainClock.advanceTimeBy(FRAME_MS * 4)
        composeRule.onNodeWithContentDescription(closedLockLabel).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(openLockLabel).assertDoesNotExist()
        assertEquals("Still only locked once", 1, lockCalls)
    }

    @Test
    fun extraTapsWhileAnimatingDoNotLockAgain() {
        composeRule.onNodeWithTag(TAG).performClick()
        composeRule.mainClock.advanceTimeBy(LOCK_BUTTON_DELAY_MS / 2)
        composeRule.onNodeWithTag(TAG).performClick()
        composeRule.mainClock.advanceTimeBy(LOCK_BUTTON_DELAY_MS)
        composeRule.onNodeWithTag(TAG).performClick()
        composeRule.mainClock.advanceTimeBy(TOTAL_MS * 2)

        assertEquals(1, lockCalls)
    }

    private companion object {
        const val TAG = "lockButton"
        const val FRAME_MS = 16L
        const val TOTAL_MS = LOCK_BUTTON_DELAY_MS + LOCK_BUTTON_ANIMATION_MS
    }
}
