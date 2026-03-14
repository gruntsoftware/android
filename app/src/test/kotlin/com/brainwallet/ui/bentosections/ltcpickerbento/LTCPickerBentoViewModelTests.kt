package com.brainwallet.ui.bentosections.ltcpickerbento
import android.content.Context
import app.cash.turbine.test
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.sqlite.CurrencyDataSource
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LTCPickerBentoViewModelTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Test dispatcher + collaborators
    // ─────────────────────────────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()

    private val settingsFlow = MutableSharedFlow<AppSetting>(replay = 1)

    private val settingRepository: SettingRepository = mockk(relaxed = true) {
        every { settings } returns settingsFlow
    }
    private val currencyDataSource: CurrencyDataSource = mockk()
    private val mockContext: Context = mockk(relaxed = true)

    private lateinit var viewModel: LTCPickerBentoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LTCPickerBentoViewModel(settingRepository, currencyDataSource)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Initial state
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `initial state has default values`() {
        val state = viewModel.state.value

        assertTrue(state.darkMode)
        assertEquals("USD", state.selectedCurrency.code)
        assertEquals(GlobalCurrency.entries, state.globalCurrencies)
        assertEquals(GlobalCurrency.USD, state.selectedGlobalCurrency)
        assertEquals("", state.formattedTimeStamp)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // settings flow → state updates
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `when settings flow emits darkMode true, state reflects darkMode true`() = runTest {
        val eurCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        val setting = AppSetting(isDarkMode = true, currency = eurCurrency)

        viewModel.state.test {
            awaitItem() // consume initial state

            settingsFlow.emit(setting)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertTrue(updated.darkMode)
            assertEquals("EUR", updated.selectedCurrency.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when settings flow emits darkMode false, state reflects darkMode false`() = runTest {
        val setting = AppSetting(isDarkMode = false)

        viewModel.state.test {
            awaitItem() // consume initial state

            settingsFlow.emit(setting)
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertFalse(updated.darkMode)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when settings flow emits, formattedTimeStamp is non-empty`() = runTest {
        viewModel.state.test {
            awaitItem() // consume initial state

            settingsFlow.emit(AppSetting())
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertTrue(
                "Expected a non-empty timestamp after settings emission",
                updated.formattedTimeStamp.isNotEmpty()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when settings flow emits same value twice, state updates only once`() = runTest {
        val setting = AppSetting(isDarkMode = true)

        viewModel.state.test {
            awaitItem() // initial

            settingsFlow.emit(setting)
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem() // first emission update

            settingsFlow.emit(setting) // identical — distinctUntilChanged should suppress
            testDispatcher.scheduler.advanceUntilIdle()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OnGlobalCurrencyChange — currency found
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `OnGlobalCurrencyChange updates selectedCurrency when currency is found`() = runTest {
        val eurEntity = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        every { currencyDataSource.getCurrencyByIso("EUR") } returns eurEntity

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onEvent(LTCPickerBentoEvent.OnGlobalCurrencyChange(GlobalCurrency.EUR))
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals("EUR", updated.selectedCurrency.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnGlobalCurrencyChange saves updated AppSetting to repository when currency is found`() = runTest {
        val eurEntity = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        every { currencyDataSource.getCurrencyByIso("EUR") } returns eurEntity
        settingsFlow.emit(AppSetting())
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(LTCPickerBentoEvent.OnGlobalCurrencyChange(GlobalCurrency.EUR))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify {
            settingRepository.save(
                match { it.currency.code == "EUR" }
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OnGlobalCurrencyChange — currency NOT found
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `OnGlobalCurrencyChange does not update state when currency is not found`() = runTest {
        every { currencyDataSource.getCurrencyByIso(any()) } returns null

        val stateBefore = viewModel.state.value

        viewModel.state.test {
            awaitItem() // initial

            viewModel.onEvent(LTCPickerBentoEvent.OnGlobalCurrencyChange(GlobalCurrency.EUR))
            testDispatcher.scheduler.advanceUntilIdle()

            // No new emission expected — state is unchanged
            expectNoEvents()
            assertEquals(stateBefore.selectedCurrency.code, viewModel.state.value.selectedCurrency.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnGlobalCurrencyChange does not call save when currency is not found`() = runTest {
        every { currencyDataSource.getCurrencyByIso(any()) } returns null

        viewModel.onEvent(LTCPickerBentoEvent.OnGlobalCurrencyChange(GlobalCurrency.EUR))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { settingRepository.save(any()) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OnLoad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `OnLoad updates formattedTimeStamp to a non-empty string`() = runTest {
        viewModel.state.test {
            awaitItem() // initial (formattedTimeStamp is "")

            viewModel.onEvent(LTCPickerBentoEvent.OnLoad(mockContext))
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertTrue(
                "Expected formattedTimeStamp to be non-empty after OnLoad",
                updated.formattedTimeStamp.isNotEmpty()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `OnLoad does not change selectedCurrency`() = runTest {
        val currencyBefore = viewModel.state.value.selectedCurrency

        viewModel.state.test {
            awaitItem()

            viewModel.onEvent(LTCPickerBentoEvent.OnLoad(mockContext))
            testDispatcher.scheduler.advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(currencyBefore.code, updated.selectedCurrency.code)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSelectedFiatRateIndex extension
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getSelectedFiatRateIndex returns correct index for matching currency`() {
        val usdEntity = CurrencyEntity("USD", "US Dollar", -1f, "$")
        val state = LTCPickerBentoState(
            selectedCurrency = usdEntity,
            globalCurrencies = GlobalCurrency.entries
        )
        val expectedIndex = GlobalCurrency.entries.indexOfFirst {
            it.code.lowercase() == "usd"
        }

        assertEquals(expectedIndex, state.getSelectedFiatRateIndex())
        assertNotEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns -1 when no currency matches`() {
        val unknownEntity = CurrencyEntity("XYZ", "Unknown", -1f, "?")
        val state = LTCPickerBentoState(selectedCurrency = unknownEntity)

        assertEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex is case-insensitive`() {
        val upperState = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        )
        val lowerState = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("eur", "Euro", 1.1f, "€")
        )

        assertEquals(upperState.getSelectedFiatRateIndex(), lowerState.getSelectedFiatRateIndex())
    }
}
