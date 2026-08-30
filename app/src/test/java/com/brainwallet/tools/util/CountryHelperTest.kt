package com.brainwallet.tools.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryHelperTest {

    @Test
    fun `given ISO countries, when countries is built, then it is not empty`() {
        assertTrue(CountryHelper.countries.isNotEmpty())
    }

    @Test
    fun `given ISO countries, when countries is built, then every entry has a non-blank name and code`() {
        CountryHelper.countries.forEach { country ->
            assertTrue("expected a non-blank name for code ${country.code}", country.name.isNotBlank())
            assertTrue("expected a non-blank code for name ${country.name}", country.code.isNotBlank())
        }
    }

    @Test
    fun `given ISO countries, when countries is built, then it is sorted by display name`() {
        val names = CountryHelper.countries.map { it.name }

        assertEquals(names.sorted(), names)
    }

    @Test
    fun `given ISO countries, when countries is built, then it contains the United States`() {
        assertTrue(CountryHelper.countries.any { it.code == "US" })
    }

    @Test
    fun `given usaCountry, then it is the United States with code US`() {
        assertEquals("United States", CountryHelper.usaCountry.name)
        assertEquals("US", CountryHelper.usaCountry.code)
    }
}
