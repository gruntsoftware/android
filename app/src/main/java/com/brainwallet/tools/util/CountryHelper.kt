package com.brainwallet.tools.util

import com.brainwallet.data.model.Country
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
