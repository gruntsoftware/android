package com.brainwallet.domain

import java.text.BreakIterator

/**
 * Rules for growing the user's emoji phrase from 3 to 6 emojis.
 *
 * The first 3 emojis are set before the first game. Once the user has finished
 * [GAMES_TO_UNLOCK_NEXT_TRIPLET] games, opening the game asks for the PIN and then
 * emojis 4 - 6 (matching seed words 4 - 6) before launching.
 * Mirrors iOS `SetSixthEmojiView` / `GameHubViewModel.setSecondEmojiTriplet`.
 */
object EmojiLevelUp {

    const val TRIPLET_SIZE = 3
    const val GAMES_TO_UNLOCK_NEXT_TRIPLET = 2

    /** Seed word index (0-based) that emoji 4 maps to. */
    const val NEXT_TRIPLET_WORD_OFFSET = TRIPLET_SIZE

    /**
     * Number of user-visible emojis in [emojis]. Counts grapheme clusters, not chars,
     * since most emojis are surrogate pairs or ZWJ sequences.
     */
    fun countEmojis(emojis: String?): Int = splitEmojis(emojis).size

    fun splitEmojis(emojis: String?): List<String> {
        if (emojis.isNullOrEmpty()) return emptyList()
        val boundary = BreakIterator.getCharacterInstance()
        boundary.setText(emojis)
        val result = mutableListOf<String>()
        var start = boundary.first()
        var end = boundary.next()
        while (end != BreakIterator.DONE) {
            result.add(emojis.substring(start, end))
            start = end
            end = boundary.next()
        }
        return result
    }

    /** True when exactly the first triplet is set and enough games have been played. */
    fun shouldSetNextTriplet(emojiCount: Int, gamesPlayed: Int): Boolean =
        emojiCount == TRIPLET_SIZE && gamesPlayed >= GAMES_TO_UNLOCK_NEXT_TRIPLET

    /**
     * Appends [next] to [existing], or returns null when the result would be invalid:
     * [existing] isn't exactly one triplet (guards against storing 9), or [next] isn't 3
     * distinct, non-blank emojis. Like iOS, a new emoji may repeat one from the first 3.
     */
    fun appendNextTriplet(existing: String?, next: List<String?>): String? {
        val current = splitEmojis(existing)
        if (current.size != TRIPLET_SIZE) return null
        if (next.size != TRIPLET_SIZE || next.any { it.isNullOrBlank() }) return null
        val nextEmojis = next.filterNotNull()
        if (nextEmojis.toSet().size != nextEmojis.size) return null
        return current.joinToString("") + nextEmojis.joinToString("")
    }
}
