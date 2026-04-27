package com.brainwallet.ui.bentosections.ltcpickerbento
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.util.CurrencyDataGetter
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

@OptIn(ExperimentalCoroutinesApi::class)
class LTCPickerBentoViewModelTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Test dispatcher + collaborators
    // ─────────────────────────────────────────────────────────────────────────

    private val testDispatcher = StandardTestDispatcher()
    private val settingsFlow = MutableSharedFlow<AppSetting>(replay = 1)
    private val ltcRepository: LtcRepository = mockk(relaxed = true)
    private val settingRepository: SettingRepository = mockk(relaxed = true) {
        every { settings } returns settingsFlow
    }
    private val currencyDataSource: CurrencyDataSource = mockk()
    private val currencyDataGetter: CurrencyDataGetter = mockk()

    private lateinit var viewModel: LTCPickerBentoViewModel
    private val mockCrashlytics: FirebaseCrashlytics = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns mockCrashlytics
        coEvery { ltcRepository.fetchRates() } returns emptyList()

        viewModel = LTCPickerBentoViewModel(
            settingRepository,
            currencyDataSource,
            currencyDataGetter,
            ltcRepository
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
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
