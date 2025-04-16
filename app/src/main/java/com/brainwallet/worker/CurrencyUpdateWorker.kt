package com.brainwallet.worker

import com.brainwallet.data.repository.LtcRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CurrencyUpdateWorker(
    private val ltcRepository: LtcRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true && job != null) {
            job?.cancel()
        }

        job = scope.launch(Dispatchers.IO) {
            while (isActive) {
                ltcRepository.fetchRates()
                delay(4000L) //4secs
            }
        }
    }


}