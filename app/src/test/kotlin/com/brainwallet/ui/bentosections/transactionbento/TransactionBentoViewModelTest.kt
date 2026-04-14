package com.brainwallet.ui.bentosections.transactionbento

import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.LtcStats
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionBentoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingRepository: SettingRepository
    private lateinit var ltcRepository: LtcRepository
    private lateinit var viewModel: TransactionBentoViewModel
    private val currentSettingsFlow = MutableStateFlow(AppSetting())
    private val ltcStatsFlow = MutableStateFlow(
        LtcStats(
            0,
            0,
            0,
            0
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingRepository = mockk(relaxed = true)
        ltcRepository = mockk(relaxed = true)

        every { ltcRepository.ltcStats } returns ltcStatsFlow
        every { settingRepository.currentSettings } returns currentSettingsFlow

        viewModel = TransactionBentoViewModel(
            settingRepository = settingRepository,
            ltcRepository = ltcRepository,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // ── settings subscription ──────────────────────────────────────────────

    @Test
    fun `state reflects currency from currentSettings flow`() = runTest {
        val eur = CurrencyEntity(code = "EUR", symbol = "€", rate = 90.0F)
        currentSettingsFlow.value = AppSetting(currency = eur)
        advanceUntilIdle()

        assertEquals("EUR", viewModel.state.value.selectedCurrency?.code)
    }

    // ── formatter ─────────────────────────────────────────────────────────

    @Test
    fun `formatter pattern is correct`() {
        val pattern = (viewModel.formatter as java.text.SimpleDateFormat).toPattern()
        assertEquals("MMM dd, yyyy h:mm:ss a", pattern)
    }

    @Test
    fun `formatter produces non-empty string for epoch timestamp`() {
        val result = viewModel.formatter.format(java.util.Date(0L))
        assertTrue(result.isNotEmpty())
    }

    // ── events ────────────────────────────────────────────────────────────

    @Test
    fun `OnLoad event does not throw`() = runTest {
        viewModel.onEvent(TransactionBentoEvent.OnLoad)
        advanceUntilIdle()
    }
}
