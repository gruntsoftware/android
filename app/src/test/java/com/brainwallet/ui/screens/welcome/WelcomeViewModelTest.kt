package com.brainwallet.ui.screens.welcome

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.WorkManager
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.worker.SyncBlockWorker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

@ExperimentalCoroutinesApi
class WelcomeViewModelTest {

    private lateinit var viewModel: WelcomeViewModel
    private lateinit var settingRepository: SettingRepository
    private val settingFlow = MutableStateFlow(AppSetting())
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Mock repository
        settingRepository = mockk {
            every { settings } returns settingFlow
            coEvery { save(any()) } returns Unit
        }

        // Mock static objects
        mockkStatic(AppCompatDelegate::class)
        mockkStatic(BRWalletManager::class)

        viewModel = WelcomeViewModel(settingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state has default values`() = runTest {
        val state = viewModel.state.value
        assertTrue(state.darkMode)
        assertEquals(Language.ENGLISH, state.selectedLanguage)
        assertEquals("USD", state.selectedCurrency.code)
        assertFalse(state.fiatSelectorBottomSheetVisible)
        assertFalse(state.languageSelectorBottomSheetVisible)
    }

    @Test
    fun `test OnToggleDarkMode toggles darkMode and saves to repository`() = runTest {
        // Given
        val initialDarkMode = viewModel.state.value.darkMode

        // When
        viewModel.onEvent(WelcomeEvent.OnToggleDarkMode)
        advanceUntilIdle()

        // Then
        val newDarkMode = viewModel.state.value.darkMode
        assertEquals(!initialDarkMode, newDarkMode)

        val settingSlot = slot<AppSetting>()
        coVerify { settingRepository.save(capture(settingSlot)) }
        assertEquals(newDarkMode, settingSlot.captured.isDarkMode)
    }

    @Test
    fun `test OnFiatButtonClick shows fiat selector bottom sheet`() {
        // When
        viewModel.onEvent(WelcomeEvent.OnFiatButtonClick)

        // Then
        assertTrue(viewModel.state.value.fiatSelectorBottomSheetVisible)
    }

    @Test
    fun `test OnLanguageSelectorButtonClick shows language selector bottom sheet`() {
        // When
        viewModel.onEvent(WelcomeEvent.OnLanguageSelectorButtonClick)

        // Then
        assertTrue(viewModel.state.value.languageSelectorBottomSheetVisible)
    }

    @Test
    fun `test OnFiatSelectorDismiss hides fiat selector bottom sheet`() {
        // Given
        viewModel.onEvent(WelcomeEvent.OnFiatButtonClick)

        // When
        viewModel.onEvent(WelcomeEvent.OnFiatSelectorDismiss)

        // Then
        assertFalse(viewModel.state.value.fiatSelectorBottomSheetVisible)
    }

    @Test
    fun `test OnLanguageSelectorDismiss hides language selector bottom sheet`() {
        // Given
        viewModel.onEvent(WelcomeEvent.OnLanguageSelectorButtonClick)

        // When
        viewModel.onEvent(WelcomeEvent.OnLanguageSelectorDismiss)

        // Then
        assertFalse(viewModel.state.value.languageSelectorBottomSheetVisible)
    }

    @Test
    fun `test OnLanguageChange updates language and saves to repository`() = runTest {
        // Given
        val newLanguage = Language.INDONESIAN

        // When
        viewModel.onEvent(WelcomeEvent.OnLanguageChange(newLanguage))
        advanceUntilIdle()

        // Then
        assertEquals(newLanguage, viewModel.state.value.selectedLanguage)
        assertFalse(viewModel.state.value.languageSelectorBottomSheetVisible)

        val settingSlot = slot<AppSetting>()
        coVerify { settingRepository.save(capture(settingSlot)) }
        assertEquals(newLanguage.code, settingSlot.captured.languageCode)

        verify { AppCompatDelegate.setApplicationLocales(any()) }
    }

    @Test
    fun `test OnFiatChange updates currency and saves to repository`() = runTest {
        // Given
        val newCurrency = CurrencyEntity(
            code = "EUR",
            name = "Euro",
            symbol = "€"
        )

        // When
        viewModel.onEvent(WelcomeEvent.OnFiatChange(newCurrency))
        advanceUntilIdle()

        // Then
        assertEquals(newCurrency, viewModel.state.value.selectedCurrency)
        assertFalse(viewModel.state.value.fiatSelectorBottomSheetVisible)

        val settingSlot = slot<AppSetting>()
        coVerify { settingRepository.save(capture(settingSlot)) }
        assertEquals(newCurrency, settingSlot.captured.currency)
    }

    @Test
    fun `test OnLoad generates wallet if none exists and starts sync`() = runTest {
        //given
        mockkStatic(WorkManager::class)
        val workManager = mockk<WorkManager>(relaxed = true)
        every { BRWalletManager.getInstance().noWallet(context) } returns true
        every { BRWalletManager.getInstance().generateRandomSeed(context) } returns true
        every { WorkManager.getInstance(context) } returns workManager

        // When
        viewModel.onEvent(WelcomeEvent.OnLoad(context))

        // Then
        verify { BRWalletManager.getInstance().noWallet(context) }
        verify { BRWalletManager.getInstance().generateRandomSeed(context) }
        verify { workManager.enqueue(SyncBlockWorker.request) }
    }

    @Test
    fun `test updating setting from repository updates state`() = runTest {

        val currencyGBP = CurrencyEntity(
            code = "GBP",
            name = "British Pound",
            symbol = "£"
        )

        // When
        settingFlow.update {
            it.copy(
                isDarkMode = true,
                languageCode = Language.SPANISH.code,
                currency = currencyGBP
            )
        }
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertTrue(state.darkMode)
        assertEquals(Language.SPANISH, state.selectedLanguage)
        assertEquals(currencyGBP, state.selectedCurrency)
    }
}