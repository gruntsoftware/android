package com.brainwallet.data.model

import java.util.Currency
import java.util.Locale

object CurrencyUtils {
    /**
     * Finds the currency symbol for a given ISO 4217 currency code.
     * This mimics the Swift implementation by searching through available locales.
     */
    fun getSymbolForCurrencyCode(code: String): String? {
        return try {
            val currency = Currency.getInstance(code)
            // In Android/Java, Currency.getSymbol() returns the symbol for the default locale.
            // If you specifically want to find a locale that matches the currency like the Swift code:
            Locale.getAvailableLocales().firstOrNull { locale ->
                try {
                    Currency.getInstance(locale).currencyCode == code
                } catch (e: Exception) {
                    false
                }
            }?.let { currency.getSymbol(it) } ?: currency.symbol
        } catch (e: Exception) {
            null
        }
    }
}

enum class GlobalCurrency(
    val id: Int,
    val code: String,
    val fullCurrencyName: String,
    val symbol: String,
    val countryFlag: String
) {
    USD(0, "USD", "US Dollar", "$", "🇺🇸"),
    EUR(1, "EUR", "Euro", "€", "🇪🇺"),
    AED(2, "AED", "UAE Dirham", "د.إ", "🇦🇪"),
    AFN(3, "AFN", "Afghan Afghani", "؋", "🇦🇫"),
    ALL(4, "ALL", "Albanian Lek", "L", "🇦🇱"),
    AMD(5, "AMD", "Armenian Dram", "֏", "🇦🇲"),
    ARS(6, "ARS", "Argentine Peso", "$", "🇦🇷"),
    AUD(7, "AUD", "Australian Dollar", "A$", "🇦🇺"),
    AZN(8, "AZN", "Azerbaijani Manat", "₼", "🇦🇿"),
    BAM(9, "BAM", "Bosnia and Herzegovina Convertible Mark", "КМ", "🇧🇦"),
    BBD(10, "BBD", "Barbadian Dollar", "Bds$", "🇧🇧"),
    BDT(11, "BDT", "Bangladeshi Taka", "৳", "🇧🇩"),
    BGN(12, "BGN", "Bulgarian Lev", "лв", "🇧🇬"),
    BHD(13, "BHD", "Bahraini Dinar", ".د.ب", "🇧🇭"),
    BND(14, "BND", "Brunei Dollar", "B$", "🇧🇳"),
    BOB(15, "BOB", "Bolivian Boliviano", "Bs.", "🇧🇴"),
    BRL(16, "BRL", "Brazilian Real", "R$", "🇧🇷"),
    BTN(17, "BTN", "Bhutanese Ngultrum", "Nu.", "🇧🇹"),
    BYN(18, "BYN", "Belarusian Ruble", "Br", "🇧🇾"),
    CAD(19, "CAD", "Canadian Dollar", "C$", "🇨🇦"),
    CHF(20, "CHF", "Swiss Franc", "CHF", "🇨🇭"),
    CLP(21, "CLP", "Chilean Peso", "$", "🇨🇱"),
    CNY(22, "CNY", "Chinese Yuan", "¥", "🇨🇳"),
    COP(23, "COP", "Colombian Peso", "$", "🇨🇴"),
    CRC(24, "CRC", "Costa Rican Colón", "₡", "🇨🇷"),
    CZK(25, "CZK", "Czech Koruna", "Kč", "🇨🇿"),
    DKK(26, "DKK", "Danish Krone", "kr", "🇩🇰"),
    DOP(27, "DOP", "Dominican Peso", "RD$", "🇩🇴"),
    DZD(28, "DZD", "Algerian Dinar", "د.ج", "🇩🇿"),
    EGP(29, "EGP", "Egyptian Pound", "£", "🇪🇬"),
    FJD(30, "FJD", "Fijian Dollar", "FJ$", "🇫🇯"),
    GBP(31, "GBP", "British Pound Sterling", "£", "🇬🇧"),
    GEL(32, "GEL", "Georgian Lari", "₾", "🇬🇪"),
    GHS(33, "GHS", "Ghanaian Cedi", "₵", "🇬🇭"),
    GTQ(34, "GTQ", "Guatemalan Quetzal", "Q", "🇬🇹"),
    HKD(35, "HKD", "Hong Kong Dollar", "HK$", "🇭🇰"),
    HNL(36, "HNL", "Honduran Lempira", "L", "🇭🇳"),
    HRK(37, "HRK", "Croatian Kuna", "kn", "🇭🇷"),
    HUF(38, "HUF", "Hungarian Forint", "Ft", "🇭🇺"),
    IDR(39, "IDR", "Indonesian Rupiah", "Rp", "🇮🇩"),
    ILS(40, "ILS", "Israeli Shekel", "₪", "🇮🇱"),
    INR(41, "INR", "Indian Rupee", "₹", "🇮🇳"),
    ISK(42, "ISK", "Icelandic Krona", "kr", "🇮🇸"),
    JMD(43, "JMD", "Jamaican Dollar", "J$", "🇯🇲"),
    JOD(44, "JOD", "Jordanian Dinar", "د.ا", "🇯🇴"),
    JPY(45, "JPY", "Japanese Yen", "¥", "🇯🇵"),
    KES(46, "KES", "Kenyan Shilling", "Sh", "🇰🇪"),
    KGS(47, "KGS", "Kyrgyzstani Som", "лв", "🇰🇬"),
    KHR(48, "KHR", "Cambodian Riel", "៛", "🇰🇭"),
    KRW(49, "KRW", "South Korean Won", "₩", "🇰🇷"),
    KWD(50, "KWD", "Kuwaiti Dinar", "د.ك", "🇰🇼"),
    KZT(51, "KZT", "Kazakhstani Tenge", "₸", "🇰🇿"),
    LAK(52, "LAK", "Lao Kip", "₭", "🇱🇦"),
    LKR(53, "LKR", "Sri Lankan Rupee", "Rs", "🇱🇰"),
    MAD(54, "MAD", "Moroccan Dirham", "د.م.", "🇲🇦"),
    MDL(55, "MDL", "Moldovan Leu", "L", "🇲🇩"),
    MKD(56, "MKD", "Macedonian Denar", "ден", "🇲🇰"),
    MMK(57, "MMK", "Myanmar Kyat", "Ks", "🇲🇲"),
    MNT(58, "MNT", "Mongolian Tugrik", "₮", "🇲🇳"),
    MXN(59, "MXN", "Mexican Peso", "$", "🇲🇽"),
    MYR(60, "MYR", "Malaysian Ringgit", "RM", "🇲🇾"),
    NGN(61, "NGN", "Nigerian Naira", "₦", "🇳🇬"),
    NIO(62, "NIO", "Nicaraguan Córdoba", "C$", "🇳🇮"),
    NOK(63, "NOK", "Norwegian Krone", "kr", "🇳🇴"),
    NPR(64, "NPR", "Nepalese Rupee", "Rs", "🇳🇵"),
    NZD(65, "NZD", "New Zealand Dollar", "NZ$", "🇳🇿"),
    OMR(66, "OMR", "Omani Rial", "﷼", "🇴🇲"),
    PAB(67, "PAB", "Panamanian Balboa", "B/.", "🇵🇦"),
    PEN(68, "PEN", "Peruvian Sol", "S/", "🇵🇪"),
    PGK(69, "PGK", "Papua New Guinea Kina", "K", "🇵🇬"),
    PHP(70, "PHP", "Philippine Peso", "₱", "🇵🇭"),
    PKR(71, "PKR", "Pakistani Rupee", "₨", "🇵🇰"),
    PLN(72, "PLN", "Polish Zloty", "zł", "🇵🇱"),
    PYG(73, "PYG", "Paraguayan Guarani", "₲", "🇵🇾"),
    QAR(74, "QAR", "Qatari Riyal", "﷼", "🇶🇦"),
    RON(75, "RON", "Romanian Leu", "lei", "🇷🇴"),
    RSD(76, "RSD", "Serbian Dinar", "дин", "🇷🇸"),
    RUB(77, "RUB", "Russian Ruble", "₽", "🇷🇺"),
    SAR(78, "SAR", "Saudi Riyal", "﷼", "🇸🇦"),
    SBD(79, "SBD", "Solomon Islands Dollar", "SI$", "🇸🇧"),
    SEK(80, "SEK", "Swedish Krona", "kr", "🇸🇪"),
    SGD(81, "SGD", "Singapore Dollar", "S$", "🇸🇬"),
    THB(82, "THB", "Thai Baht", "฿", "🇹🇭"),
    TJS(83, "TJS", "Tajikistani Somoni", "ЅМ", "🇹🇯"),
    TMT(84, "TMT", "Turkmenistani Manat", "m", "🇹🇲"),
    TND(85, "TND", "Tunisian Dinar", "د.ت", "🇹🇳"),
    TOP(86, "TOP", "Tongan Pa'anga", "T$", "🇹🇴"),
    TRY(87, "TRY", "Turkish Lira", "₺", "🇹🇷"),
    TTD(88, "TTD", "Trinidad and Tobago Dollar", "TT$", "🇹🇹"),
    TWD(89, "TWD", "Taiwan Dollar", "NT$", "🇹🇼"),
    UAH(90, "UAH", "Ukrainian Hryvnia", "₴", "🇺🇦"),
    UYU(91, "UYU", "Uruguayan Peso", "$", "🇺🇾"),
    UZS(92, "UZS", "Uzbekistani Som", "лв", "🇺🇿"),
    VES(93, "VES", "Venezuelan Bolívar", "Bs.S", "🇻🇪"),
    VND(94, "VND", "Vietnamese Dong", "₫", "🇻🇳"),
    VUV(95, "VUV", "Vanuatu Vatu", "Vt", "🇻🇺"),
    WST(96, "WST", "Samoan Tala", "WS$", "🇼🇸"),
    XCD(97, "XCD", "East Caribbean Dollar", "$", "🇦🇬"),
    ZAR(98, "ZAR", "South African Rand", "R", "🇿🇦");

    companion object {
        fun from(code: String): GlobalCurrency? {
            return entries.find { it.code == code }
        }
    }
}
