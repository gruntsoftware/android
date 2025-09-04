package com.brainwallet.data.source

import com.brainwallet.wallet.BRPeerManager
import org.koin.core.annotation.Single

interface BRPeerManagerProxy {
    fun getCurrentBlockHeight(): Int
    fun getLastBlockTimestamp(): Long
}

@Single
class PeerManagerSource(
    private val proxy: BRPeerManagerProxy = object : BRPeerManagerProxy {
        override fun getCurrentBlockHeight(): Int = BRPeerManager.getCurrentBlockHeight()
        override fun getLastBlockTimestamp(): Long = BRPeerManager.getInstance().lastBlockTimestamp
    }
) : BRPeerManagerProxy by proxy
