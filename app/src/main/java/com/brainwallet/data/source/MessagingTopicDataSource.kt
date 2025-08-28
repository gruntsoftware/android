package com.brainwallet.data.source

import com.brainwallet.data.model.Language
import org.koin.core.annotation.Single

@Single
class MessagingTopicDataSource(
    private val supportedLanguages: List<Language> = Language.entries,
    private val defaultLanguage: Language = Language.ENGLISH
) {
    fun getTopicsByLanguageCode(languageCode: String): List<String> {
        val baseLanguageCode = getBaseLanguageCode(languageCode)
        return listOf(
            "initial_$baseLanguageCode",
            "news_$baseLanguageCode",
            "promo_$baseLanguageCode",
            "warn_$baseLanguageCode"
        )
    }

    private fun getBaseLanguageCode(languageCode: String): String {
        val normalizedLanguageCode = languageCode.split("_", "-").first().lowercase()
        val targetLanguage = supportedLanguages.find { 
            it.code.split("-").first().lowercase() == normalizedLanguageCode 
        } ?: defaultLanguage
        
        return targetLanguage.code.split("-").first().lowercase()
    }
}
