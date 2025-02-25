package com.brainwallet.data.model

import java.util.Locale

enum class Language(
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
    KOREAN("ko", "한국어", "언어 선택"),
    FRENCH("fr", "Français", "Sélectionner la langue"),
    CHINESE_TRADITIONAL("zh-TW", "繁體字", "選擇語言"),
    TURKISH("tr", "Türkçe", "Dil Seçin"),
    JAPANESE("ja", "日本語", "言語を選択する"),
    GERMAN("de", "Deutsch", "Sprache auswählen"),
    CHINESE_SIMPLIFIED("zh-CN", "简化字", "选择语言"),
    HINDI("hi", "हिंदी", "भाषा चुने"),
    ITALIAN("it", "Italiano", "Seleziona la lingua"),
    BLANK(" ", " ", " "),
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

