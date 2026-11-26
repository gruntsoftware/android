package com.brainwallet.tools.manager.sync

import android.content.Context
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncKeepAliveTest {

    @Test
    fun `starts below 99 percent`() {
        listOf(0f, 0.05f, 0.5f, 0.9899f).forEach { assertTrue(SyncForegroundService.needsKeepAlive(it)) }
    }

    @Test
    fun `does not start from 99 percent`() {
        listOf(0.99f, 0.999f, 1f).forEach { assertFalse(SyncForegroundService.needsKeepAlive(it)) }
    }

    @Test
    fun `once started it stays until the sync is complete`() {
        assertEquals(0, SyncForegroundService.percentToShow(0f))
        assertEquals(98, SyncForegroundService.percentToShow(0.9899f))
        assertEquals(99, SyncForegroundService.percentToShow(0.99f))
        assertEquals(99, SyncForegroundService.percentToShow(0.9999f))
        assertNull(SyncForegroundService.percentToShow(1f))
    }

    // A generated definition would override the one in AppModule.dataModule and drop the keep-alive hook.
    @Test
    fun `SyncThreadManager is not also registered by annotation`() {
        val generatedMarker = "org.koin.ksp.generated._KSP_ComBrainwalletToolsManagerSyncSyncThreadManager"

        assertThrows(ClassNotFoundException::class.java) { Class.forName(generatedMarker) }
    }

    @Test
    fun `does not start the service when the sync is already past the limit`() {
        val context = mockk<Context>(relaxed = true)

        SyncForegroundService.startIfNeeded(context, 0.995f)

        verify(exactly = 0) { context.applicationContext }
    }

    @Test
    fun `reconnects while the sync is stopped and leaves a running sync alone`() = runTest {
        val syncing = SyncThreadManager.SyncState.Syncing(0.5f, 0L, 1)
        val state = MutableStateFlow<SyncThreadManager.SyncState>(syncing)
        var reconnects = 0
        backgroundScope.launch { SyncForegroundService.reconnectWhileIdle(state) { reconnects++ } }

        advanceTimeBy(SyncForegroundService.RETRY_MS * 5)
        assertEquals(0, reconnects)

        state.value = SyncThreadManager.SyncState.Idle
        advanceTimeBy(SyncForegroundService.RETRY_MS * 2 + 1)
        assertEquals(2, reconnects)

        state.value = syncing
        advanceTimeBy(SyncForegroundService.RETRY_MS * 5)
        assertEquals(2, reconnects)
    }
}
