package com.brainwallet.domain.flow

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.brainwallet.R
import com.brainwallet.ltc.domain.flow.PromptStateFlow
import com.brainwallet.ltc.domain.model.PromptState
import com.brainwallet.presenter.activities.UpdatePinActivity
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.PromptManager
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.wallet.BRPeerManager
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import timber.log.Timber

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class PromptStateFlowImpl(
    private val context: Context,
    private val upstream: MutableStateFlow<PromptState?> = MutableStateFlow(null)
) : PromptStateFlow, StateFlow<PromptState?> by upstream {

    override fun onStart() {
        if (upstream.value is PromptState.Syncing) {
            upstream.update { null }
            showNextPrompt()
        }
    }

    override fun onSyncProgress(progress: Double) {
        if (upstream.value !is PromptState.Syncing) {
            upstream.update { PromptState.Syncing(progress) }
        }
    }

    override fun onSynced() {
        if (upstream.value is PromptState.Syncing) {
            upstream.update { null }
            showNextPrompt()
        }
    }

    override fun dismissPrompt() {
        val wasSync = upstream.value is PromptState.Syncing
        upstream.update { null }

        if (wasSync) {
            showNextPrompt()
        }
    }

    override fun triggerAction(activity: Activity) {
        when (upstream.value) {
            is PromptState.RecommendRescan -> {
                val intent = Intent(activity, UpdatePinActivity::class.java)
                activity.startActivity(intent)
            }

            is PromptState.UpgradePin -> {
                BRExecutor.getInstance().forLightWeightBackgroundTasks().execute {
                    BRSharedPrefs.putStartHeight(activity, 0)
                    BRPeerManager.getInstance().rescan()
                    BRSharedPrefs.putScanRecommended(activity, false)
                    BRSharedPrefs.putAllowSpend(activity, false)
                }
            }

            else -> {
                /** Do Nothing **/
            }
        }
    }

    private fun showNextPrompt() {
        if (upstream.value is PromptState.Syncing) {
            return
        }

        val promptManager = PromptManager.getInstance()
        val nextPromptItem = promptManager.nextPrompt(context)

        upstream.update {
            when (nextPromptItem) {
                PromptManager.PromptItem.UPGRADE_PIN -> {
                    val title = context.getString(R.string.Prompts_UpgradePin_title)
                    val desc = context.getString(R.string.Prompts_UpgradePin_body)
                    PromptState.UpgradePin(title, desc)
                }

                PromptManager.PromptItem.RECOMMEND_RESCAN -> {
                    val title = context.getString(R.string.Prompts_RecommendRescan_title)
                    val desc = context.getString(R.string.Prompts_RecommendRescan_body)
                    PromptState.RecommendRescan(title, desc)
                }

                else -> null
            }
        }

        Timber.d("showNextPrompt: ${upstream.value}")
    }
}
