package com.brainwallet.data.repository

import com.brainwallet.data.model.GameScoreRecord
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import timber.log.Timber

class GameScoreRepositoryImpl(
    private val storage: GameScoreStorage,
) : GameScoreRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(GameScoreRecord.serializer())

    @Synchronized
    override fun gameScores(): List<GameScoreRecord> {
        val stored = runCatching { storage.read() }
            .onFailure { Timber.w(it, "Unable to read game scores") }
            .getOrNull()
        if (stored.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString(serializer, stored) }
            .onFailure { Timber.w(it, "Stored game scores are not valid JSON") }
            .getOrDefault(emptyList())
    }

    // Synchronized so two quick game exits can't both read N and write N + 1
    @Synchronized
    override fun appendGameScore(record: GameScoreRecord) {
        val updated = gameScores() + record
        runCatching { storage.write(json.encodeToString(serializer, updated)) }
            .onFailure { Timber.w(it, "Unable to save game score") }
    }
}
