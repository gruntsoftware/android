package com.brainwallet.data.repository

import com.brainwallet.data.model.GameScoreRecord

/** The user's finished-game history. Mirrors iOS `UserDefaults.gameScores` / `appendGameScore`. */
interface GameScoreRepository {

    /** Finished games, oldest first. Empty when nothing is stored or the stored value can't be read. */
    fun gameScores(): List<GameScoreRecord>

    fun appendGameScore(record: GameScoreRecord)

    fun gamesPlayedCount(): Int = gameScores().size
}

/** Where the stringified JSON array lives. Split out so the repository is unit-testable. */
interface GameScoreStorage {
    fun read(): String?
    fun write(scoresJson: String)
}
