package com.brainwallet.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiLevelUpTest {

    // countEmojis

    @Test
    fun `countEmojis returns 0 for null or empty`() {
        assertEquals(0, EmojiLevelUp.countEmojis(null))
        assertEquals(0, EmojiLevelUp.countEmojis(""))
    }

    @Test
    fun `countEmojis counts surrogate-pair emojis once each`() {
        // Each of these is 2 UTF-16 chars, so String.length would report 6
        assertEquals(3, EmojiLevelUp.countEmojis("😂🔥🚀"))
    }

    @Test
    fun `countEmojis counts six emojis`() {
        assertEquals(6, EmojiLevelUp.countEmojis("😂🔥🚀🍔🎮🐶"))
    }

    // shouldSetNextTriplet

    @Test
    fun `shouldSetNextTriplet is false before 2 games`() {
        assertFalse(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 3, gamesPlayed = 0))
        assertFalse(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 3, gamesPlayed = 1))
    }

    @Test
    fun `shouldSetNextTriplet is true at and after 2 games with 3 emojis like iOS`() {
        assertTrue(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 3, gamesPlayed = 2))
        assertTrue(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 3, gamesPlayed = 10))
    }

    @Test
    fun `shouldSetNextTriplet is false when emojis are not exactly 3`() {
        assertFalse(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 0, gamesPlayed = 5))
        assertFalse(EmojiLevelUp.shouldSetNextTriplet(emojiCount = 6, gamesPlayed = 5))
    }

    // appendNextTriplet

    @Test
    fun `appendNextTriplet appends 3 new emojis to the first 3`() {
        assertEquals(
            "😂🔥🚀🍔🎮🐶",
            EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("🍔", "🎮", "🐶"))
        )
    }

    @Test
    fun `appendNextTriplet refuses when 6 are already stored`() {
        assertNull(EmojiLevelUp.appendNextTriplet("😂🔥🚀🍔🎮🐶", listOf("🐱", "🌮", "⚽")))
    }

    @Test
    fun `appendNextTriplet refuses when nothing is stored`() {
        assertNull(EmojiLevelUp.appendNextTriplet(null, listOf("🍔", "🎮", "🐶")))
        assertNull(EmojiLevelUp.appendNextTriplet("", listOf("🍔", "🎮", "🐶")))
    }

    @Test
    fun `appendNextTriplet refuses missing or blank picks`() {
        assertNull(EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("🍔", null, "🐶")))
        assertNull(EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("🍔", " ", "🐶")))
        assertNull(EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("🍔", "🎮")))
    }

    @Test
    fun `appendNextTriplet refuses duplicates within the new triplet`() {
        assertNull(EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("🍔", "🍔", "🐶")))
    }

    @Test
    fun `appendNextTriplet allows repeating an emoji from the first triplet like iOS`() {
        assertEquals(
            "😂🔥🚀😂🎮🐶",
            EmojiLevelUp.appendNextTriplet("😂🔥🚀", listOf("😂", "🎮", "🐶"))
        )
    }
}
