package com.brainwallet.tools.manager

import android.content.Context
import android.os.Bundle
import com.brainwallet.constants.BWConstants
import com.grunt.brainwallet.af.AFLauncher

/**
 * Reports that the user has a confirmed wallet, to AppsFlyer ([BWConstants.AF_EVENT_E20261115A])
 * and Firebase ([EVENT_USER_CONFIRMED_WALLET]). A wallet is confirmed in one of two ways, each at
 * the point where the phrase is marked as written down:
 * - created: the user finished putting their seed words back in order ([onNewWalletConfirmed])
 * - restored: the restored phrase was saved to the keystore ([onRestoredWalletConfirmed])
 */
object WalletConfirmedEvents {

    const val EVENT_USER_CONFIRMED_WALLET = "user_confirmed_wallet"
    const val PARAM_COMPLETION = "completion"
    const val COMPLETION_CREATE = "create"
    const val COMPLETION_RESTORE = "restore"

    /**
     * Marks the new wallet's phrase as written down and reports it. Settings -> Seed phrase can
     * lead back through the prove-it screen for an existing wallet, so only the first
     * confirmation is reported.
     *
     * @return whether the events were sent.
     */
    @JvmStatic
    fun onNewWalletConfirmed(context: Context): Boolean {
        val firstConfirmation = !BRSharedPrefs.getPhraseWroteDown(context)
        BRSharedPrefs.putPhraseWroteDown(context, true)
        if (firstConfirmation) report(COMPLETION_CREATE)
        return firstConfirmation
    }

    /**
     * Reports a restored wallet. Call after the phrase is saved and marked as written down.
     * Restoring always starts from a wiped wallet, so every restore is reported.
     */
    @JvmStatic
    fun onRestoredWalletConfirmed() {
        report(COMPLETION_RESTORE)
    }

    private fun report(completion: String) {
        AFLauncher.logEvent(BWConstants.AF_EVENT_E20261115A)
        val params = Bundle().apply { putString(PARAM_COMPLETION, completion) }
        AnalyticsManager.logCustomAdHocEvent(EVENT_USER_CONFIRMED_WALLET, params)
    }
}
