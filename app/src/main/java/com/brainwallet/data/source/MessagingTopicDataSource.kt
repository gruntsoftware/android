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

    /**
     * Note: Indonesian language code is normalized from "in" to "id" to maintain
     * consistency with backend batch messaging server which uses ISO 639-1 standard.
     * Supports both "in" and "id" as input for Indonesian language.
     */
    private fun getBaseLanguageCode(languageCode: String): String {
        val normalizedLanguageCode = languageCode.split("_", "-").first().lowercase()
        
        // Handle reverse mapping: if "id" is passed, treat it as Indonesian
        val searchCode = when (normalizedLanguageCode) {
            "id" -> "in"
            else -> normalizedLanguageCode
        }
        
        val targetLanguage = supportedLanguages.find { 
            it.code.split("-").first().lowercase() == searchCode 
        } ?: defaultLanguage
        
        val baseCode = targetLanguage.code.split("-").first().lowercase()
        
        return when (baseCode) {
            "in" -> "id"
            else -> baseCode
        }
    }
}
