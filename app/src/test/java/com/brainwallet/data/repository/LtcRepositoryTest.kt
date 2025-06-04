package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse
import com.brainwallet.data.source.response.GetMoonpaySignUrlResponse
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LtcRepositoryTest {

    private lateinit var repository: LtcRepository
    private lateinit var context: Context
    private lateinit var remoteApiSource: RemoteApiSource
    private lateinit var currencyDataSource: CurrencyDataSource
    private lateinit var sharedPreferences: SharedPreferences
    private val preferenceStore = mutableMapOf<String, String>()

    // Test data
    private val dummyCurrencies = listOf(
        CurrencyEntity("USD", "US Dollar", 90.705f, "$"),

        /**
         * {
         *     "code": "EUR",
         *     "n": 79.655,
         *     "price": "€79.655",
         *     "name": "Euro"
         *   },
         */
        CurrencyEntity("EUR", "Euro", 79.655f, "€"),
    )

    private val dummyLimit = MoonpayCurrencyLimit(
        data = MoonpayCurrencyLimit.Data(
            paymentMethod = "moonpay_balance",
            quoteCurrency = MoonpayCurrencyLimit.CurrencyLimit(
                code = "ltc",
                min = 0.213F,
                max = 319.451F
            ),
            baseCurrency = MoonpayCurrencyLimit.CurrencyLimit(
                code = "USD",
                min = 21.0F,
                max = 29849.0F
            ),
        )
    )

    private val dummyQuote = GetMoonpayBuyQuoteResponse(
        data = GetMoonpayBuyQuoteResponse.Data(
            baseCurrencyCode = "USD",
            quoteCurrencyAmount = 0.5F,
            totalAmount = 104.99F
        ),
    )

    private val dummyGetMoonpaySignUrlResponse = GetMoonpaySignUrlResponse(
        signedUrl = "https://buy.moonpay.com?apiKey=test&signature=test",
    )

    private val dummyCachedJson = """
        {
            "data": {
                "paymentMethod": "moonpay_balance",
                "quoteCurrency": {
                    "code": "ltc",
                    "minBuyAmount": 0.213,
                    "maxBuyAmount": 319.451
                },
                "baseCurrency": {
                    "code": "USD",
                    "minBuyAmount": 21.0,
                    "maxBuyAmount": 29849.0
                },
                "areFeesIncluded": false
            }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // Mock context
        context = mockk(relaxed = true)

        // Mock RemoteApiSource
        remoteApiSource = mockk(relaxed = true) {
            coEvery { getRates() } returns dummyCurrencies
            coEvery { getMoonpayCurrencyLimit(any()) } returns dummyLimit
            coEvery { getBuyQuote(any()) } returns dummyQuote
            coEvery { getMoonpaySignedUrl(any()) } returns dummyGetMoonpaySignUrlResponse
        }

        // Mock CurrencyDataSource
        currencyDataSource = mockk {
            every { putCurrencies(any()) } just Runs
            every { getAllCurrencies(any()) } returns dummyCurrencies
        }

        // Mock SharedPreferences
        sharedPreferences = mockk(relaxed = true)

        // Mock static methods
        mockkStatic(FeeManager::class)
        every { FeeManager.updateFeePerKb(any()) } just Runs

        mockkStatic(BRSharedPrefs::class)
        every { BRSharedPrefs.getIsoSymbol(any()) } returns "USD"
        every { BRSharedPrefs.putIso(any(), any()) } just Runs
        every { BRSharedPrefs.putCurrencyListPosition(any(), any()) } just Runs

        mockkStatic(Utils::class)
        every { Utils.getAgentString(any(), any()) } returns "agent-string"

        // Initialize repository
        repository =
            LtcRepository.Impl(context, remoteApiSource, currencyDataSource, sharedPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
        preferenceStore.clear()
    }

    @Test
    fun `fetchRates should get rates from remote and save to local`() = runTest {
        // When
        val result = repository.fetchRates()

        // Then
        coVerify { remoteApiSource.getRates() }
        verify { FeeManager.updateFeePerKb(context) }
        verify { BRSharedPrefs.getIsoSymbol(context) }
        verify { currencyDataSource.putCurrencies(dummyCurrencies) }
        assertEquals(dummyCurrencies, result)
    }

    @Test
    fun `fetchRates should update BRSharedPrefs when matching ISO found`() = runTest {
        // Given
        every { BRSharedPrefs.getIsoSymbol(any()) } returns "EUR"

        // When
        repository.fetchRates()

        // Then
        verify { BRSharedPrefs.putIso(context, "EUR") }
        verify { BRSharedPrefs.putCurrencyListPosition(context, 0) } // Index is 1-1 for EUR
    }

    @Test
    fun `fetchRates should fall back to local data when remote fails`() = runTest {
        // Given
        coEvery { remoteApiSource.getRates() } throws Exception("Network error")

        // When
        val result = repository.fetchRates()

        // Then
        coVerify { remoteApiSource.getRates() }
        verify { currencyDataSource.getAllCurrencies(true) }
        assertEquals(dummyCurrencies, result)
    }

    @Test
    fun `fetchFeePerKb should return default fee`() = runTest {
        // When
        val result = repository.fetchFeePerKb()

        // Then
        assertEquals(Fee.Default, result)
    }

    @Test
    fun `fetchLimits should get limits from cache if available and not expired`() = runTest {
        // Given
        val currencyCode = "USD"
        val cacheKey = "${LtcRepository.PREF_KEY_BUY_LIMITS_PREFIX}$currencyCode"
        val cachedAtKey = "${LtcRepository.PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT}$currencyCode"

        preferenceStore[cacheKey] = dummyCachedJson
        preferenceStore[cachedAtKey] =
            (System.currentTimeMillis() - 1000).toString() // 1 second ago

        // When
        val result = repository.fetchLimits(currencyCode)

        // Then
        assertEquals("USD", result.data.baseCurrency.code)
        assertEquals(21.0F, result.data.baseCurrency.min, 0.01F)
        assertEquals(29849F, result.data.baseCurrency.max, 0.01F)
    }

    @Test
    fun `fetchLimits should get limits from remote when cache expired`() = runTest {
        // Given
        val currencyCode = "USD"
        val cacheKey = "${LtcRepository.PREF_KEY_BUY_LIMITS_PREFIX}$currencyCode"
        val cachedAtKey = "${LtcRepository.PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT}$currencyCode"

        // Store expired cache (more than 5 minutes old)
        val cachedJson =
            """{"code":"USD","minAmount":100.0,"maxAmount":10000.0,"supportedCountries":["US"]}"""
        preferenceStore[cacheKey] = cachedJson
        preferenceStore[cachedAtKey] =
            (System.currentTimeMillis() - 10 * 60 * 1000).toString() // 10 minutes ago

        // When
        val result = repository.fetchLimits(currencyCode)

        // Then
        coVerify { remoteApiSource.getMoonpayCurrencyLimit(currencyCode) }
        assertEquals(dummyLimit, result)
    }

    @Test
    fun `fetchBuyQuote should call remote API with parameters`() = runTest {
        // Given
        val params = mapOf(
            "baseCurrencyAmount" to "100",
            "baseCurrencyCode" to "USD"
        )

        // When
        val result = repository.fetchBuyQuote(params)

        // Then
        coVerify { remoteApiSource.getBuyQuote(params) }
        assertEquals(dummyQuote, result)
    }

    @Test
    fun `fetchMoonpaySignedUrl should call remote API and transform URL`() = runTest {
        // Given
        val params = mapOf(
            "baseCurrencyAmount" to "100",
            "baseCurrencyCode" to "USD"
        )

        val expectedParams = params + mapOf(
            "defaultCurrencyCode" to "ltc",
            "externalTransactionId" to "agent-string",
            "currencyCode" to "ltc",
            "themeId" to "main-v1.0.0"
        )

        val paramsCaptor = slot<Map<String, String>>()

        // Mock Uri builder properly
        mockkStatic(Uri::class)
        mockkStatic("androidx.core.net.UriKt")

        val mockUriBuilder: Uri.Builder = mockk(relaxed = true) {
            every { authority(any()) } returns this@mockk
            every { build() } returns mockk<Uri> {
                every { this@mockk.toString() } returns dummyGetMoonpaySignUrlResponse.signedUrl
            }
        }

        val mockUri = mockk<Uri> {
            every { buildUpon() } returns mockUriBuilder
        }

        every { Uri.parse(dummyGetMoonpaySignUrlResponse.signedUrl) } returns mockUri

        // When
        val result = repository.fetchMoonpaySignedUrl(params)

        // Then
        coVerify { remoteApiSource.getMoonpaySignedUrl(capture(paramsCaptor)) }
        assertEquals(expectedParams, paramsCaptor.captured)

        // Verify the result contains the expected URL
        assertEquals(dummyGetMoonpaySignUrlResponse.signedUrl, result)
    }
}