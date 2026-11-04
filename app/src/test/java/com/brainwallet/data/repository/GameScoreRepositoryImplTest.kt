package com.brainwallet.data.repository

import com.brainwallet.data.model.GameScoreRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameScoreRepositoryImplTest {

    private class FakeStorage(var stored: String? = null) : GameScoreStorage {
        var failReads = false
        override fun read(): String? {
            if (failReads) error("keystore unavailable")
            return stored
        }
        override fun write(scoresJson: String) {
            stored = scoresJson
        }
    }

    @Test
    fun `given nothing stored, when gameScores, then returns empty`() {
        val repository = GameScoreRepositoryImpl(FakeStorage())

        assertTrue(repository.gameScores().isEmpty())
        assertEquals(0, repository.gamesPlayedCount())
    }

    @Test
    fun `given appended games, when gameScores, then returns them oldest first`() {
        val repository = GameScoreRepositoryImpl(FakeStorage())
        val first = GameScoreRecord(totalScore = 10, timestamp = 1_000L)
        val second = GameScoreRecord(totalScore = 25, timestamp = 2_000L)

        repository.appendGameScore(first)
        repository.appendGameScore(second)

        assertEquals(listOf(first, second), repository.gameScores())
        assertEquals(2, repository.gamesPlayedCount())
    }

    @Test
    fun `when appendGameScore, then stores a JSON array in the iOS wire format`() {
        val storage = FakeStorage()
        val repository = GameScoreRepositoryImpl(storage)

        repository.appendGameScore(GameScoreRecord(totalScore = 42, timestamp = 1_700_000_000_000L))

        assertEquals("""[{"total_score":42,"timestamp":1700000000000}]""", storage.stored)
    }

    @Test
    fun `given history written by the iOS format, when gameScores, then decodes it`() {
        val storage = FakeStorage("""[{"total_score":5,"timestamp":1},{"total_score":9,"timestamp":2}]""")

        val scores = GameScoreRepositoryImpl(storage).gameScores()

        assertEquals(listOf(GameScoreRecord(5, 1L), GameScoreRecord(9, 2L)), scores)
    }

    @Test
    fun `given corrupt stored value, when gameScores, then returns empty instead of throwing`() {
        val repository = GameScoreRepositoryImpl(FakeStorage("not json"))

        assertTrue(repository.gameScores().isEmpty())
    }

    @Test
    fun `given storage read fails, when gameScores, then returns empty instead of throwing`() {
        val storage = FakeStorage().apply { failReads = true }

        assertTrue(GameScoreRepositoryImpl(storage).gameScores().isEmpty())
    }
}
