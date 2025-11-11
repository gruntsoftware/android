package com.brainwallet.ltc.domain.model

sealed class PromptState {
    data class Syncing(val progress: Double) : PromptState()
    data class UpgradePin(val title: String, val description: String) : PromptState()
    data class RecommendRescan(val title: String, val description: String) : PromptState()
    data class NoPasscode(val title: String, val description: String) : PromptState()
}
