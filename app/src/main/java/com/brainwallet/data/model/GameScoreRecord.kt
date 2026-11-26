package com.brainwallet.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One finished game, stored as part of a JSON array (see GameScoreRepository).
 * Same wire shape as iOS `GameScoreRecord`: {"total_score": Int, "timestamp": ms since epoch}.
 */
@Serializable
data class GameScoreRecord(
    @SerialName("total_score") val totalScore: Int,
    val timestamp: Long,
)
