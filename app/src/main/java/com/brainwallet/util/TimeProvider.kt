package com.brainwallet.util

import org.koin.core.annotation.Single

@Single
class TimeProvider(
    private val nowGetter: () -> Long = { System.currentTimeMillis() }
) {
    fun now(): Long {
        return nowGetter()
    }
}
