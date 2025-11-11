package com.brainwallet.ltc.domain.flow

import android.app.Activity
import com.brainwallet.ltc.domain.model.PromptState
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface PromptStateFlow : StateFlow<PromptState?> {
    fun onStart()
    fun onSyncProgress(progress: Double)
    fun onSynced()
    fun dismissPrompt()
    fun triggerAction(activity: Activity)
}
