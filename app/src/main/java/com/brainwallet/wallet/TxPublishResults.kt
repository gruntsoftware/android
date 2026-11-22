package com.brainwallet.wallet

import kotlinx.coroutines.CompletableDeferred

/**
 * Hands the core's asynchronous publish outcome (`BRWalletManager.publishCallback`) to whoever
 * is waiting on that transaction.
 *
 * The core only reports a publish as successful once a peer relays the transaction back, and it
 * reports rejections (`EINVAL`, `ETIMEDOUT`, `ENOTCONN`) through the same callback, sometimes
 * synchronously before `publishSerializedTransaction` has even returned the hash. Results that
 * arrive before anyone awaits them are therefore buffered (bounded) until [await] picks them up.
 */
object TxPublishResults {

    /** errno value the core uses for a successful publish. */
    const val SUCCESS = 0

    private const val MAX_BUFFERED_RESULTS = 16

    private val lock = Any()
    private val waiters = HashMap<String, CompletableDeferred<Int>>()
    private val buffered = LinkedHashMap<String, Int>()

    /** Called from the native publish callback thread. */
    @JvmStatic
    fun onPublishResult(txHash: ByteArray?, error: Int) {
        val key = txHash?.toKey() ?: return
        val waiter = synchronized(lock) {
            waiters.remove(key) ?: run {
                buffered[key] = error
                while (buffered.size > MAX_BUFFERED_RESULTS) {
                    buffered.remove(buffered.keys.first())
                }
                null
            }
        }
        waiter?.complete(error)
    }

    /**
     * Suspends until the core reports the outcome for [txHash]; returns its errno
     * ([SUCCESS] when the network accepted the transaction). Wrap in a timeout.
     */
    suspend fun await(txHash: ByteArray): Int {
        val key = txHash.toKey()
        val deferred = synchronized(lock) {
            buffered.remove(key)?.let { return it }
            waiters.getOrPut(key) { CompletableDeferred() }
        }
        try {
            return deferred.await()
        } finally {
            synchronized(lock) {
                if (waiters[key] === deferred) waiters.remove(key)
            }
        }
    }

    internal fun reset() = synchronized(lock) {
        waiters.clear()
        buffered.clear()
    }

    private fun ByteArray.toKey(): String = joinToString("") { "%02x".format(it) }
}
