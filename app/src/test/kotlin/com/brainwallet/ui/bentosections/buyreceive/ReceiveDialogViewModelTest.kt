package com.brainwallet.ui.bentosections.buyreceive

import android.content.Context
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialogViewModel
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialogEvent
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getQuickFiatAmountOptions
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ReceiveDialogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingRepository: SettingRepository
    private lateinit var ltcRepository: LtcRepository
    private lateinit var context: Context
    private lateinit var viewModel: ReceiveDialogViewModel

    private val settingsFlow = MutableStateFlow(AppSetting())
    private val currentSettingsFlow = MutableStateFlow(AppSetting())
    private val fakeCurrencyEntity = CurrencyEntity(code = "USD", name = "US Dollar", rate = 1.0f, symbol = "USD")
    private val fakeFiatCurrencies = listOf(fakeCurrencyEntity)

    private lateinit var mockCurrencyDataSource: CurrencyDataSource

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        every { context.getSystemService(any()) } returns mockk(relaxed = true)

        settingRepository = mockk(relaxed = true)
        ltcRepository = mockk(relaxed = true)

        mockkStatic(BRSharedPrefs::class)
        mockkStatic(CurrencyDataSource::class)
        mockkStatic(QRUtils::class)

        mockCurrencyDataSource = mockk(relaxed = true)
        every { CurrencyDataSource.getInstance(any()) } returns mockCurrencyDataSource
        every { mockCurrencyDataSource.getCurrenciesForBuy() } returns fakeFiatCurrencies
        every { BRSharedPrefs.getReceiveAddress(any()) } returns "LTC_FAKE_ADDRESS"
        every { QRUtils.generateQR(any(), any()) } returns null
        every { settingRepository.settings } returns settingsFlow
        every { settingRepository.currentSettings } returns currentSettingsFlow

        coEvery { ltcRepository.fetchLimits(any()) } returns mockk(relaxed = true)
        coEvery { ltcRepository.fetchBuyQuote(any()) } returns mockk(relaxed = true)
        coEvery { ltcRepository.fetchMoonpaySignedUrl(any()) } returns "https://buy.moonpay.com/signed"

        viewModel = ReceiveDialogViewModel(
            settingRepository = settingRepository,
            ltcRepository = ltcRepository,
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
        assertEquals("", state.address)
        assertNull(state.qrBitmap)
        assertNull(state.moonpayBuySignedUrl)
    }

    // ── OnLoad ─────────────────────────────────────────────────────────────

    @Test
    fun `OnLoad sets address from BRSharedPrefs`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        assertEquals("LTC_FAKE_ADDRESS", viewModel.state.value.address)
    }

    @Test
    fun `OnLoad sets fiat currencies from CurrencyDataSource`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        assertEquals(fakeFiatCurrencies, viewModel.state.value.fiatCurrencies)
    }

    @Test
    fun `OnLoad sets qrBitmap from QRUtils`() = runTest {
        // non-null bitmap path — swap stub to return a real mock
        every { QRUtils.generateQR(any(), any()) } returns mockk(relaxed = true)

        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.qrBitmap)
    }

    // ── OnFiatCurrencyChange ───────────────────────────────────────────────

    @Test
    fun `OnFiatCurrencyChange updates selectedFiatCurrency`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(fakeCurrencyEntity))
        advanceUntilIdle()

        assertEquals(fakeCurrencyEntity, viewModel.state.value.selectedFiatCurrency)
    }

    @Test
    fun `OnFiatCurrencyChange resets selectedQuickFiatAmountOptionIndex to 1`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(fakeCurrencyEntity))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.selectedQuickFiatAmountOptionIndex)
    }

    // ── OnFiatAmountChange ─────────────────────────────────────────────────

    @Test
    fun `OnFiatAmountChange updates fiatAmount in state`() = runTest {
        // stub limits so validation passes for 50f
        val mockLimit = mockk<MoonpayCurrencyLimit>(relaxed = true) {
            every { data.baseCurrency.min } returns 1f
            every { data.baseCurrency.max } returns 1000f
        }
        coEvery { ltcRepository.fetchLimits(any()) } returns mockLimit

        // trigger a currency change first so limits are loaded into state
        viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(fakeCurrencyEntity))
        advanceUntilIdle()

        viewModel.onEvent(ReceiveDialogEvent.OnFiatAmountChange(50f, needFetch = false))
        advanceUntilIdle()

        assertEquals(50f, viewModel.state.value.fiatAmount)
    }

    // ── OnFiatAmountOptionIndexChange ──────────────────────────────────────
    @Test
    fun `OnFiatAmountOptionIndexChange updates selectedQuickFiatAmountOptionIndex`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(fakeCurrencyEntity))
        advanceUntilIdle()

        val options = viewModel.state.value.getQuickFiatAmountOptions()
        viewModel.onEvent(ReceiveDialogEvent.OnFiatAmountOptionIndexChange(0, options[0]))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.selectedQuickFiatAmountOptionIndex)
    }

    // ── OnMoonpayButtonClick ───────────────────────────────────────────────

    @Test
    fun `OnMoonpayButtonClick sets moonpayBuySignedUrl from ltcRepository`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        viewModel.onEvent(ReceiveDialogEvent.OnMoonpayButtonClick)
        advanceUntilIdle()

        assertEquals("https://buy.moonpay.com/signed", viewModel.state.value.moonpayBuySignedUrl)
    }

    @Test
    fun `OnMoonpayButtonClick includes walletAddress from state`() = runTest {
        val paramsSlot = slot<Map<String, String>>()
        coEvery { ltcRepository.fetchMoonpaySignedUrl(capture(paramsSlot)) } returns "https://buy.moonpay.com/signed"

        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        viewModel.onEvent(ReceiveDialogEvent.OnMoonpayButtonClick)
        advanceUntilIdle()

        assertEquals("LTC_FAKE_ADDRESS", paramsSlot.captured["walletAddress"])
    }

    @Test
    fun `OnMoonpayButtonClick calls fetchMoonpaySignedUrl exactly once`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        advanceUntilIdle()

        viewModel.onEvent(ReceiveDialogEvent.OnMoonpayButtonClick)
        advanceUntilIdle()

        coVerify(exactly = 1) { ltcRepository.fetchMoonpaySignedUrl(any()) }
    }

    // ── OnSignedUrlClear ───────────────────────────────────────────────────

    @Test
    fun `OnSignedUrlClear sets moonpayBuySignedUrl to null`() = runTest {
        viewModel.onEvent(ReceiveDialogEvent.OnSignedUrlClear)
        advanceUntilIdle()

        assertNull(viewModel.state.value.moonpayBuySignedUrl)
    }

    // ── settings subscription ──────────────────────────────────────────────

    @Test
    fun `settings emission triggers OnFiatCurrencyChange with new currency`() = runTest {
        val newCurrency = CurrencyEntity(code = "USD", name = "US Dollar", rate = 1.0f, symbol = "USD")
        coEvery { ltcRepository.fetchLimits("GBP") } returns mockk(relaxed = true)
        settingsFlow.emit(AppSetting(currency = newCurrency))
        advanceUntilIdle()

        assertEquals(newCurrency, viewModel.state.value.selectedFiatCurrency)
    }
}
