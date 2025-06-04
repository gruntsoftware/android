package com.brainwallet.data.repository

import android.content.SharedPreferences
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SettingRepositoryTest {

    private lateinit var repository: SettingRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyDataSource: CurrencyDataSource
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock SharedPreferences storage
    private val preferenceStore = mutableMapOf<String, Any?>()

    // Currency instances
    private val dummyUsdCurrency = CurrencyEntity("USD", "US Dollar", 1.0f, "$")
    private val dummyEurCurrency = CurrencyEntity("EUR", "Euro", 0.85f, "€")

    @Before
    fun setUp() {
        // Setup SharedPreferences mock
        sharedPreferences = mockk(relaxed = true) {
            every { getBoolean(SettingRepository.KEY_IS_DARK_MODE, true) } answers {
                preferenceStore[SettingRepository.KEY_IS_DARK_MODE] as? Boolean ?: true
            }

            every {
                getString(
                    SettingRepository.KEY_LANGUAGE_CODE,
                    Language.ENGLISH.code
                )
            } answers {
                preferenceStore[SettingRepository.KEY_LANGUAGE_CODE] as? String
                    ?: Language.ENGLISH.code
            }

            every { getString(SettingRepository.KEY_FIAT_CURRENCY_CODE, "USD") } answers {
                preferenceStore[SettingRepository.KEY_FIAT_CURRENCY_CODE] as? String ?: "USD"
            }

            every {
                getString(
                    SettingRepository.KEY_SELECTED_FEE_TYPE,
                    FeeManager.REGULAR
                )
            } answers {
                preferenceStore[SettingRepository.KEY_SELECTED_FEE_TYPE] as? String
                    ?: FeeManager.REGULAR
            }

            every { edit() } answers {
                val mockEditor = mockk<SharedPreferences.Editor>(relaxed = true) {
                    every { putBoolean(any(), any()) } answers {
                        val key = firstArg<String>()
                        val value = secondArg<Boolean>()
                        preferenceStore[key] = value
                        this@mockk
                    }

                    every { putString(any(), any()) } answers {
                        val key = firstArg<String>()
                        val value = secondArg<String>()
                        preferenceStore[key] = value
                        this@mockk
                    }

                    every { apply() } returns Unit
                    every { commit() } returns true
                }
                mockEditor
            }

        }

        // Setup CurrencyDataSource mock
        currencyDataSource = mockk {
            every { getCurrencyByIso("USD") } returns dummyUsdCurrency
            every { getCurrencyByIso("EUR") } returns dummyEurCurrency
            every { getCurrencyByIso(null) } returns null
        }

        // Initialize repository with mocks
        repository = SettingRepository.Impl(sharedPreferences, currencyDataSource)
    }

    @After
    fun tearDown() {
        preferenceStore.clear()
    }

    @Test
    fun `load initializes with default values when preferences are empty`() = runTest {
        // When settings are loaded (happens during initialization)
        val settings = repository.settings.first()

        // Then default values should be set
        assertTrue(settings.isDarkMode) // Default is true
        assertEquals(Language.ENGLISH.code, settings.languageCode)
        assertEquals("USD", settings.currency.code)
    }

    @Test
    fun `save persists values to SharedPreferences and updates state`() = runTest {
        // Given
        val newSetting = AppSetting(
            isDarkMode = false,
            languageCode = Language.SPANISH.code,
            currency = dummyEurCurrency
        )

        // When
        repository.save(newSetting)

        // Then SharedPreferences should be updated
        verify { sharedPreferences.edit() }
        assertEquals(false, preferenceStore[SettingRepository.KEY_IS_DARK_MODE])
        assertEquals(Language.SPANISH.code, preferenceStore[SettingRepository.KEY_LANGUAGE_CODE])
        assertEquals("EUR", preferenceStore[SettingRepository.KEY_FIAT_CURRENCY_CODE])

        // And state should be updated
        val updatedSettings = repository.settings.first()
        assertEquals(false, updatedSettings.isDarkMode)
        assertEquals(Language.SPANISH.code, updatedSettings.languageCode)
        assertEquals("EUR", updatedSettings.currency.code)
    }

    @Test
    fun `getCurrentLanguage returns correct language from preferences`() {
        // Given
        preferenceStore[SettingRepository.KEY_LANGUAGE_CODE] = Language.INDONESIAN.code

        // When
        val language = repository.getCurrentLanguage()

        // Then
        assertEquals(Language.INDONESIAN, language)
    }

    @Test
    fun `updateCurrentLanguage updates language in preferences and state`() = runTest {
        // When
        repository.updateCurrentLanguage(Language.JAPANESE.code)

        // Then SharedPreferences should be updated
        verify { sharedPreferences.edit() }
        assertEquals(Language.JAPANESE.code, preferenceStore[SettingRepository.KEY_LANGUAGE_CODE])

        // And state should be updated
        val updatedSettings = repository.settings.first()
        assertEquals(Language.JAPANESE.code, updatedSettings.languageCode)
    }

    @Test
    fun `isDarkMode returns correct value from preferences`() {
        // Given
        preferenceStore[SettingRepository.KEY_IS_DARK_MODE] = false

        // When
        val isDarkMode = repository.isDarkMode()

        // Then
        assertFalse(isDarkMode)
    }

    @Test
    fun `toggleDarkMode updates darkMode in preferences and state`() = runTest {
        // When
        repository.toggleDarkMode(false)

        // Then SharedPreferences should be updated
        verify { sharedPreferences.edit() }
        assertEquals(false, preferenceStore[SettingRepository.KEY_IS_DARK_MODE])

        // And state should be updated
        val updatedSettings = repository.settings.first()
        assertFalse(updatedSettings.isDarkMode)
    }

    @Test
    fun `putSelectedFeeType updates fee type in preferences`() {
        // Given
        val newFeeType = FeeManager.ECONOMY

        // When
        repository.putSelectedFeeType(newFeeType)

        // Then
        verify { sharedPreferences.edit() }
        assertEquals(newFeeType, preferenceStore[SettingRepository.KEY_SELECTED_FEE_TYPE])
    }

    @Test
    fun `getSelectedFeeType returns correct value from preferences`() {
        // Given
        preferenceStore[SettingRepository.KEY_SELECTED_FEE_TYPE] = FeeManager.ECONOMY

        // When
        val feeType = repository.getSelectedFeeType()

        // Then
        assertEquals(FeeManager.ECONOMY, feeType)
    }

    @Test
    fun `getSelectedFeeType returns REGULAR when no value in preferences`() {
        // Given
        preferenceStore.remove(SettingRepository.KEY_SELECTED_FEE_TYPE)

        // When
        val feeType = repository.getSelectedFeeType()

        // Then
        assertEquals(FeeManager.REGULAR, feeType)
    }

    @Test
    fun `OnFiatChange updates currency and saves to repository`() = runTest {
        // Given
        val newCurrency = dummyEurCurrency

        // When
        val newSetting = AppSetting(
            isDarkMode = true,
            languageCode = Language.ENGLISH.code,
            currency = newCurrency
        )
        repository.save(newSetting)

        // Then
        val updatedSettings = repository.settings.first()
        assertEquals("EUR", updatedSettings.currency.code)
        assertEquals("Euro", updatedSettings.currency.name)
        assertEquals(0.85f, updatedSettings.currency.rate)
        assertEquals("€", updatedSettings.currency.symbol)
    }
}