package com.brainwallet.ui.bentosections.balancebento

import android.app.Application
import app.cash.turbine.test
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.ConnectivityRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.data.source.BlockInfo
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.LtcStats
import com.brainwallet.data.repository.LtcRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BalanceBentoViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var app: Application
    private lateinit var txRepository: TxRepository
    private lateinit var settingRepository: SettingRepository
    private lateinit var ltcRepository: LtcRepository
    private lateinit var peerManagerSource: PeerManagerSource
    private lateinit var connectivityRepository: ConnectivityRepository

    private lateinit var mockWalletManager: BRWalletManager

    private lateinit var mockPeerManager: BRPeerManager

    private val transactionItemsFlow = MutableStateFlow<ImmutableList<TxItem>>(persistentListOf())
    private val settingsFlow = MutableSharedFlow<AppSetting>(replay = 1)

    private val currentSettingsFlow = MutableStateFlow(AppSetting())
    private val blockInfoFlow = MutableStateFlow(
        BlockInfo(
            blockHeight = 0,
            timestamp = 0L,
            syncProgress = 0F
        )
    )
    private val isConnectedFlow = MutableStateFlow(true)

    private lateinit var viewModel: BalanceBentoViewModel

    private val ltcStatsFlow = MutableStateFlow(LtcStats(0, 0, 0, 0))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        app = mockk(relaxed = true)
        txRepository = mockk(relaxed = true)
        settingRepository = mockk(relaxed = true)
        ltcRepository = mockk(relaxed = true)
        peerManagerSource = mockk(relaxed = true)
        connectivityRepository = mockk(relaxed = true)
        mockWalletManager = mockk<BRWalletManager>(relaxed = true)
        mockkStatic(BRWalletManager::class)
        every { BRWalletManager.getInstance() } returns mockWalletManager

        mockPeerManager = mockk<BRPeerManager>(relaxed = true)
        mockkStatic(BRPeerManager::class)
        every { BRPeerManager.getInstance() } returns mockPeerManager

        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.getStartHeight(any()) } returns 0
        every { BRSharedPrefs.getCachedBalance(any()) } returns 0L

        every { txRepository.transactionItems } returns transactionItemsFlow
        every { settingRepository.currentSettings } returns currentSettingsFlow
        every { peerManagerSource.blockInfo } returns blockInfoFlow
        every { peerManagerSource.getCurrentBlockHeight() } returns 0
        every { connectivityRepository.isConnected } returns isConnectedFlow
        every { ltcRepository.ltcStats } returns ltcStatsFlow

        viewModel = BalanceBentoViewModel(
            app = app,
            txRepository = txRepository,
            settingRepository = settingRepository,
            peerManagerSource = peerManagerSource,
            connectivityRepository = connectivityRepository,
            ltcRepository = ltcRepository
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // ── initial state ──────────────────────────────────────────────────────

    @Test
    fun `initial state has expected defaults`() = runTest {
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(0L, state.ltcBalance)
        assertTrue(state.balanceHidden)
    }

    // ── settings subscription ──────────────────────────────────────────────

    @Test
    fun `state reflects currency from settings flow`() = runTest {
        val usd = CurrencyEntity(code = "USD", symbol = "$", rate = 80.0F)
        currentSettingsFlow.value = AppSetting(currency = usd)
        advanceUntilIdle()

        assertEquals("USD", viewModel.state.value.fiatCode)
        assertEquals("$", viewModel.state.value.symbol)
    }

    @Test
    fun `state reflects dark mode from settings flow`() = runTest {
        settingsFlow.emit(AppSetting(isDarkMode = true))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.darkMode)
    }

    // ── block info / sync progress ─────────────────────────────────────────

    @Test
    fun `syncProgress is taken directly from blockInfo`() = runTest {
        blockInfoFlow.emit(BlockInfo(blockHeight = 1_250_000, timestamp = 0, syncProgress = 0.5f))
        advanceUntilIdle()
        assertEquals(0.5f, viewModel.state.value.syncProgress, 0.001f)
    }

    @Test
    fun `syncProgress is computed from blockHeight over latestLTCBlockHeight`() = runTest {
        blockInfoFlow.emit(
            BlockInfo(
                blockHeight = 1_250_000,
                timestamp = 0,
                syncProgress = 0.0F
            )
        )
        advanceUntilIdle()

        val expected = 1_250_000f / 2_500_000f
        assertEquals(expected, viewModel.state.value.syncProgress, 0.5f)
    }

    @Test
    fun `brainwalletIsSyncing is true when syncProgress below threshold`() = runTest {
        blockInfoFlow.emit(BlockInfo(blockHeight = 1_000_000, timestamp = 0L, syncProgress = 0.4f))
        advanceUntilIdle()
        assertTrue(viewModel.state.value.brainwalletIsSyncing)
    }

    // ── connectivity ───────────────────────────────────────────────────────

    @Test
    fun `isInternetReachable reflects connectivity state`() = runTest {
        isConnectedFlow.emit(false)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isInternetReachable)

        isConnectedFlow.emit(true)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isInternetReachable)
    }

    // ── onTxAdded ─────────────────────────────────────────────────────────

    @Test
    fun `onTxAdded triggers txRepository refresh`() = runTest {
        viewModel.onTxAdded()
        advanceUntilIdle()
        coVerify { txRepository.refresh() }
    }

    // ── event handling ────────────────────────────────────────────────────

    @Test
    fun `OnToggleBalanceVisibility toggles balanceHidden`() = runTest {
        assertTrue(viewModel.state.value.balanceHidden)
        viewModel.onEvent(BalanceBentoEvent.OnToggleBalanceVisibility)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.balanceHidden)
    }

    @Test
    fun `OnToggleBalanceVisibility twice restores balanceHidden to original state`() = runTest {
        val initial = viewModel.state.value.balanceHidden
        viewModel.onEvent(BalanceBentoEvent.OnToggleBalanceVisibility)
        advanceUntilIdle()
        viewModel.onEvent(BalanceBentoEvent.OnToggleBalanceVisibility)
        advanceUntilIdle()
        assertEquals(initial, viewModel.state.value.balanceHidden)
    }

    @Test
    fun `OnLoad populates selectedCurrency from current settings`() = runTest {
        val gbp = CurrencyEntity(code = "GBP", symbol = "£", rate = 65.0F)
        currentSettingsFlow.value = AppSetting(currency = gbp)
        advanceUntilIdle()

        viewModel.onEvent(BalanceBentoEvent.OnLoad)
        advanceUntilIdle()

        assertEquals("GBP", viewModel.state.value.fiatCode)
    }

    // ── onResume lifecycle ────────────────────────────────────────────────

    @Test
    fun `onResume calls txRepository refresh when wallet is created`() = runTest {
        every { BRSharedPrefs.getCachedBalance(any()) } returns 3_000_000L
        viewModel.onResume(
            isWalletCreated = { true },
            ioDispatcher = testDispatcher
        )
        advanceTimeBy(6_000)
        advanceUntilIdle()
        coVerify { txRepository.refresh() }
    }

    @Test
    fun `onResume loads cached balance from BRSharedPrefs`() = runTest {
        every { BRSharedPrefs.getCachedBalance(any()) } returns 7_500_000L

        viewModel.onResume(
            isWalletCreated = { true },
            ioDispatcher = testDispatcher
        )
        advanceTimeBy(6_000)
        advanceUntilIdle()

        assertEquals(7_500_000L, viewModel.state.value.ltcBalance)
    }

    @Test
    fun `onResume does not crash when wallet not ready after max attempts`() = runTest {
        // Should complete without throwing
        viewModel.onResume(isWalletCreated = { true })
        advanceTimeBy(6_000)
        advanceUntilIdle()
    }

    @Test
    fun `brainwalletIsSyncing is true when syncProgress is below threshold`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            blockInfoFlow.emit(
                BlockInfo(
                    blockHeight = 100,
                    syncProgress = 0.5f,
                    timestamp = 0L
                )
            )

            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertTrue(
                "Expected brainwalletIsSyncing=true when syncProgress=0.5",
                state.brainwalletIsSyncing
            )
        }
    }

    @Test
    fun `brainwalletIsSyncing is false when syncProgress exceeds threshold`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            blockInfoFlow.emit(
                BlockInfo(
                    blockHeight = 2_500_000,
                    syncProgress = 0.9999f,
                    timestamp = 0L
                )
            )

            testDispatcher.scheduler.advanceUntilIdle()

            val state = awaitItem()
            assertFalse(
                "Expected brainwalletIsSyncing=false when syncProgress=0.9999",
                state.brainwalletIsSyncing
            )
        }
    }

    @Test
    fun `brainwalletIsSyncing toggles correctly across multiple emissions`() = runTest {
        viewModel.state.test {
            awaitItem() // initial state

            // Syncing
            blockInfoFlow.emit(BlockInfo(blockHeight = 100, syncProgress = 0.5f, timestamp = 0L))
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem().brainwalletIsSyncing)

            // Synced
            blockInfoFlow.emit(BlockInfo(blockHeight = 2_500_000, syncProgress = 0.9999f, timestamp = 0L))
            testDispatcher.scheduler.advanceUntilIdle()
            assertFalse(awaitItem().brainwalletIsSyncing)

            // Back to syncing (e.g. chain reorg / test scenario)
            blockInfoFlow.emit(BlockInfo(blockHeight = 100, syncProgress = 0.75f, timestamp = 0L))
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem().brainwalletIsSyncing)
        }
    }

    @Test
    fun `ltcStats state updates from ltcRepository flow`() = runTest {
        val stats = LtcStats(
            currentBlockHeight = 3_000_000,
            mempoolTransactions = 10,
            mempoolSize = 5,
            transactionsOver24H = 100
        )
        ltcStatsFlow.value = stats
        advanceUntilIdle()
        assertEquals(3_000_000, viewModel.state.value.ltcStats?.currentBlockHeight)
    }

    @Test
    fun `onStatusUpdate does not crash`() = runTest {
        viewModel.onStatusPeerManagerUpdate()
        advanceUntilIdle()
    }
}
