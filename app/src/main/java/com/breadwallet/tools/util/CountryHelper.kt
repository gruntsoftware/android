package com.breadwallet.tools.util

import com.breadwallet.entities.Country
import java.util.Locale

object CountryHelper {
    val countries: List<Country> =
        Locale.getISOCountries().map {
            with(Locale("", it)) {
                Country(displayCountry, it)
            }
        }.sortedWith(compareBy { it.name }).toList()

    val usaCountry = Country("United States", "US")
}
