package com.brainwallet.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import com.brainwallet.wallet.BRWalletManager

class SyncBlockWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    //todo: maybe need ongoing notification?

    override suspend fun doWork(): Result {
        BRWalletManager.getInstance().initWallet(applicationContext)
        return Result.success()
    }

    companion object {
        @JvmStatic
        val request = OneTimeWorkRequestBuilder<SyncBlockWorker>()
            .setConstraints(
                Constraints(
                    requiredNetworkType = NetworkType.CONNECTED
                )
            )
            .build()
    }
}