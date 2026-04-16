package com.brainwallet.data.source

import android.app.Application
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.wallet.BRPeerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

interface BRPeerManagerProxy {
    fun getCurrentBlockHeight(): Int
    fun getLastBlockTimestamp(): Long
    fun getSyncProgress(): Double
}

interface PeerManagerSource : BRPeerManagerProxy {
    val blockInfo: StateFlow<BlockInfo>
}

data class BlockInfo(
    val blockHeight: Int,
    val timestamp: Long,
    val syncProgress: Float
)

@Single(binds = [PeerManagerSource::class])
class PeerManagerSourceImpl(
    private val app: Application,
    private val proxy: BRPeerManagerProxy = object : BRPeerManagerProxy {
        override fun getCurrentBlockHeight(): Int = BRPeerManager.getCurrentBlockHeight()
        override fun getLastBlockTimestamp(): Long = BRPeerManager.getInstance().lastBlockTimestamp
        override fun getSyncProgress(): Double = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app))
    }
) : PeerManagerSource, BRPeerManagerProxy by proxy {

    private val _blockInfo = MutableStateFlow(
        BlockInfo(
            blockHeight = proxy.getCurrentBlockHeight(),
            timestamp = proxy.getLastBlockTimestamp(),
            syncProgress = proxy.getSyncProgress().toFloat()
        )
    )
    override val blockInfo: StateFlow<BlockInfo> = _blockInfo.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(300L)
                _blockInfo.update {
                    BlockInfo(
                        blockHeight = proxy.getCurrentBlockHeight(),
                        timestamp = proxy.getLastBlockTimestamp(),
                        syncProgress = proxy.getSyncProgress().toFloat()
                    )
                }
            }
        }
    }
}
