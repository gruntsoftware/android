package com.brainwallet.data.model

import java.util.Locale

/**
 * Language
 * This enum is not meant to be exhaustive nor alphabetical but
 * subjectively ordered to reflect current usage patterns and current
 * Brainwallet localizations. Will be updated from time to time
 * based on business objectives
 */
enum class Language (
    val code: String,
    val title: String,
    val desc: String,
) {
    ENGLISH("en", "English", "Select language"),
    SPANISH("es", "Español", "Seleccione el idioma"),
    INDONESIAN("in", "Indonesia", "Pilih bahasa"),
    ARABIC("ar", "عربي", "اختر اللغة"),
    UKRAINIAN("uk", "Yкраїнський", "Оберіть мову"),
    RUSSIAN("ru", "Pусский", "Выберите язык"),
    PORTUGUESE("pt", "Português", "Selecione o idioma"),
    HINDI("hi", "हिंदी", "भाषा चुने"),
    GERMAN("de", "Deutsch", "Sprache auswählen"),
    KOREAN("ko", "한국어", "언어 선택"),
    FRENCH("fr", "Français", "Sélectionner la langue"),
    CHINESE_TRADITIONAL("zh-TW", "繁體字", "選擇語言"),
    TURKISH("tr", "Türkçe", "Dil Seçin"),
    JAPANESE("ja", "日本語", "言語を選択する"),
    CHINESE_SIMPLIFIED("zh-CN", "简化字", "选择语言"),
    ITALIAN("it", "Italiano", "Seleziona la lingua"),
    ;

    companion object {
        fun find(code: String?): Language = entries.find { it.code == code } ?: ENGLISH
    }

    fun toLocale(): Locale {
        val codes = code.split("-")
        return if (codes.size == 2) {
            Locale(codes[0], codes[1])
        } else {
            Locale(codes[0])
        }
    }
}

