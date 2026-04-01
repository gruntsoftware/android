package com.brainwallet.ui.bentosections.transactionbento

import com.brainwallet.data.model.AppSetting
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionBentoViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var settingRepository: SettingRepository
    private lateinit var viewModel: TransactionBentoViewModel

    private val settingsFlow = MutableStateFlow(AppSetting())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        settingRepository = mockk(relaxed = true)

        every { settingRepository.settings } returns settingsFlow

        viewModel = TransactionBentoViewModel(
            settingRepository = settingRepository,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // ── settings subscription ──────────────────────────────────────────────

    @Test
    fun `darkMode updates when settings emit isDarkMode true`() = runTest {
        advanceUntilIdle()

        settingsFlow.emit(AppSetting(isDarkMode = true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.darkMode)
    }

    @Test
    fun `darkMode updates when settings emit isDarkMode false`() = runTest {
        settingsFlow.emit(AppSetting(isDarkMode = true))
        advanceUntilIdle()

        settingsFlow.emit(AppSetting(isDarkMode = false))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.darkMode)
    }

    @Test
    fun `duplicate settings emissions do not cause redundant state updates`() = runTest {
        settingsFlow.emit(AppSetting(isDarkMode = true))
        advanceUntilIdle()
        settingsFlow.emit(AppSetting(isDarkMode = true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.darkMode)
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
