package com.brainwallet.data.repository

interface SelectedPeersRepository {

    suspend fun fetchSelectedPeers(): Set<String>

    companion object {
        const val LITECOIN_NODES_URL = "https://api.blockchair.com/litecoin/nodes"
    }
}
