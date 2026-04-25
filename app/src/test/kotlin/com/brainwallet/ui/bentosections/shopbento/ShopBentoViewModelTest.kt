package com.brainwallet.ui.bentosections.shopbento

import android.app.Application
import android.content.Context
import android.telephony.TelephonyManager
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShopBentoViewModelTest {

    private lateinit var app: Application
    private lateinit var settingRepository: SettingRepository
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var settingsFlow: MutableStateFlow<AppSetting>

    private fun buildViewModel() = ShopBentoViewModel(
        app = app,
        settingRepository = settingRepository,
    )

    @Before
    fun setup() {
        app = mockk()
        settingRepository = mockk()
        telephonyManager = mockk()
        settingsFlow = MutableStateFlow(AppSetting())

        every { settingRepository.settings } returns settingsFlow
        every { app.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
        every { telephonyManager.simCountryIso } returns "gb"
        every { telephonyManager.networkCountryIso } returns ""
    }

    @Test
    fun `init - uses simCountryIso when available`() = runTest {
        turbineScope {
            every { telephonyManager.simCountryIso } returns "us"

            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting())
            advanceTimeBy(100)

            assertEquals("US", turbine.expectMostRecentItem().countryIso)
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init - falls back to Locale default when both sim and network are empty`() = runTest {
        turbineScope {
            every { telephonyManager.simCountryIso } returns ""
            every { telephonyManager.networkCountryIso } returns ""

            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting())
            advanceTimeBy(100)

            val countryIso = turbine.expectMostRecentItem().countryIso
            assert(countryIso.isNotEmpty())
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent OnTapShop - updates shouldSlide to true`() = runTest {
        turbineScope {
            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting())
            advanceTimeBy(100)

            viewModel.onEvent(ShopBentoEvent.OnTapShop)
            advanceTimeBy(100)

            assertEquals(true, turbine.expectMostRecentItem().shouldSlide)
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent OnLoad - does not change state`() = runTest {
        turbineScope {
            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            turbine.awaitItem()

            settingsFlow.emit(AppSetting(isDarkMode = true))
            advanceTimeBy(100)

            val stateBefore = turbine.awaitItem()

            viewModel.onEvent(ShopBentoEvent.OnLoad)
            advanceTimeBy(100)

            turbine.expectNoEvents()
            assertEquals(stateBefore, viewModel.state.value)

            turbine.cancelAndIgnoreRemainingEvents()
        }
    }
}
