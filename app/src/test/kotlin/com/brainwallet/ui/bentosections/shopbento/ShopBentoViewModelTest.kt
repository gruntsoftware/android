package com.brainwallet.ui.bentosections.shopbento

import android.app.Application
import app.cash.turbine.turbineScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.ShopProxy
import com.brainwallet.data.repository.ShopProxyRepository
import com.brainwallet.testing.FlakyTest
import com.brainwallet.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import io.mockk.coEvery

class ShopBentoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var app: Application
    private lateinit var settingRepository: SettingRepository
    private lateinit var shopProxyRepository: ShopProxyRepository
    private lateinit var settingsFlow: MutableStateFlow<AppSetting>
    private lateinit var shopProxyFlow: MutableStateFlow<List<ShopProxy>>

    private fun buildViewModel() = ShopBentoViewModel(
        app = app,
        settingRepository = settingRepository,
        shopProxyRepository = shopProxyRepository
    )

    @Before
    fun setup() {
        app = mockk()
        settingRepository = mockk()
        shopProxyRepository = mockk()
        settingsFlow = MutableStateFlow(AppSetting())
        shopProxyFlow = MutableStateFlow(emptyList())

        every { settingRepository.settings } returns settingsFlow
        coEvery { shopProxyRepository.refresh() } returns Unit
        every { shopProxyRepository.shopProxy } returns shopProxyFlow
    }

    @Test
    fun `init - countryIso defaults to Locale`() = runTest {
        turbineScope {
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
    fun `init - sets shopBaseUrl from widget`() = runTest {
        turbineScope {
            shopProxyFlow.emit(listOf(ShopProxy(widget = "https://embed.bitrefill.com", shopCards = emptyList())))

            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting())
            advanceUntilIdle()

            val state = turbine.expectMostRecentItem()
            assertEquals("https://embed.bitrefill.com", state.shopBaseUrl)
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @FlakyTest(reason = "advanceTimeBy timing sensitive under load and in remote test runner server")
    @Test
    fun `onEvent OnTapShop - updates shouldSlide to true`() = runTest {
        turbineScope {
            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting())
            advanceTimeBy(500)

            viewModel.onEvent(ShopBentoEvent.OnTapShop)
            advanceTimeBy(500)

            assertEquals(true, turbine.expectMostRecentItem().shouldSlide)
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onEvent OnLoad - does not change state`() = runTest {
        turbineScope {
            val viewModel = buildViewModel()
            val turbine = viewModel.state.testIn(backgroundScope)

            settingsFlow.emit(AppSetting(isDarkMode = true))
            advanceTimeBy(100)

            val stateBefore = turbine.expectMostRecentItem()

            viewModel.onEvent(ShopBentoEvent.OnLoad)
            advanceTimeBy(100)

            assertEquals(stateBefore, viewModel.state.value)
            turbine.cancelAndIgnoreRemainingEvents()
        }
    }
}
