package com.brainwallet.util

import org.junit.Test

class TimeProviderTest {

    @Test
    fun `returns provided nowGetter value`() {
        val fixedTime = 123456789L
        val timeProvider = TimeProvider { fixedTime }

        val result = timeProvider.now()

        assert(result == fixedTime) { "Expected $fixedTime but got $result" }
    }
}
