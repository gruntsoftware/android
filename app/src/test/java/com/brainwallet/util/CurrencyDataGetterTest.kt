package com.brainwallet.util

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.tools.sqlite.CurrencyDataSource
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CurrencyDataGetterTest {

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockCurrencyDataSource: CurrencyDataSource

    @MockK
    private lateinit var mockIsoSymbolGetter: (Context) -> String

    @MockK
    private lateinit var mockFormattedCurrencyStringGetter: (Context, String, BigDecimal) -> String?

    private lateinit var currencyDataGetter: CurrencyDataGetter

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        currencyDataGetter = CurrencyDataGetter(
            context = mockContext,
            currencyDataSource = mockCurrencyDataSource,
            isoSymbolGetter = mockIsoSymbolGetter,
            formattedCurrencyStringGetter = mockFormattedCurrencyStringGetter
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given valid context when getting iso symbol then returns expected symbol`() {
        val expectedIsoSymbol = "USD"
        every { mockIsoSymbolGetter.invoke(mockContext) } returns expectedIsoSymbol

        val actualIsoSymbol = currencyDataGetter.getIsoSymbol()
        assert(actualIsoSymbol == expectedIsoSymbol) {
            "Expected ISO symbol to be '$expectedIsoSymbol' but was '$actualIsoSymbol'"
        }
        verify(exactly = 1) { mockIsoSymbolGetter.invoke(mockContext) }
    }

    @Test
    fun `given valid iso code when getting currency by iso then returns currency entity`() {
        val isoCode = "EUR"
        val expectedCurrency = CurrencyEntity().apply {
            code = isoCode
            name = "Euro"
        }
        every { mockCurrencyDataSource.getCurrencyByIso(isoCode) } returns expectedCurrency

        val actualCurrency = currencyDataGetter.getCurrencyByIso(isoCode)

        assert(actualCurrency == expectedCurrency) {
            "Expected currency entity with code '$isoCode' but was '$actualCurrency'"
        }
        verify(exactly = 1) { mockCurrencyDataSource.getCurrencyByIso(isoCode) }
    }

    @Test
    fun `given invalid iso code when getting currency by iso then returns null`() {
        val invalidIsoCode = "INVALID"
        every { mockCurrencyDataSource.getCurrencyByIso(invalidIsoCode) } returns null

        val actualCurrency = currencyDataGetter.getCurrencyByIso(invalidIsoCode)

        assert(actualCurrency == null) {
            "Expected null for invalid ISO code '$invalidIsoCode' but was '$actualCurrency'"
        }
        verify(exactly = 1) { mockCurrencyDataSource.getCurrencyByIso(invalidIsoCode) }
    }

    @Test
    fun `given valid currency code and amount when getting formatted string then returns formatted currency`() {
        val currencyCode = "GBP"
        val amount = BigDecimal("123.45")
        val expectedFormattedString = "£123.45"
        every { 
            mockFormattedCurrencyStringGetter.invoke(mockContext, currencyCode, amount) 
        } returns expectedFormattedString

        val actualFormattedString = currencyDataGetter.getFormattedCurrencyString(currencyCode, amount)

        assert(actualFormattedString == expectedFormattedString) {
            "Expected formatted currency '$expectedFormattedString' but was '$actualFormattedString'"
        }
        verify(exactly = 1) { 
            mockFormattedCurrencyStringGetter.invoke(mockContext, currencyCode, amount) 
        }
    }

    @Test
    fun `given invalid currency code when getting formatted string then returns null`() {
        val invalidCurrencyCode = "INVALID"
        val amount = BigDecimal("100.00")
        every { 
            mockFormattedCurrencyStringGetter.invoke(mockContext, invalidCurrencyCode, amount) 
        } returns null
        val actualFormattedString = currencyDataGetter.getFormattedCurrencyString(invalidCurrencyCode, amount)

        assert(actualFormattedString == null) {
            "Expected null for invalid currency code '$invalidCurrencyCode' but was '$actualFormattedString'"
        }
        verify(exactly = 1) { 
            mockFormattedCurrencyStringGetter.invoke(mockContext, invalidCurrencyCode, amount) 
        }
    }
}
