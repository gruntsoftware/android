package com.brainwallet.ui.screens.ready

import androidx.work.WorkManager
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.worker.SyncBlockWorker

class ReadyViewModel : BrainwalletViewModel<ReadyEvent>() {

    override fun onEvent(event: ReadyEvent) {
        when (event) {
            is ReadyEvent.OnLoad -> {
                /**
                 * inside [generateRandomSeed]
                 * if seed phrase exists, then will using it
                 */
                BRWalletManager.getInstance().generateRandomSeed(event.context)
                WorkManager.getInstance(event.context).enqueue(SyncBlockWorker.request)
            }
        }
    }
}