package com.brainwallet.data.source

import org.junit.Before
import org.junit.Test

class MessagingTopicDataSourceTest {
    private lateinit var dataSource: MessagingTopicDataSource

    @Before
    fun setUp() {
        dataSource = MessagingTopicDataSource()
    }

    @Test
    fun `given locale with underscore when getTopicsByLanguageCode called then it should extract base language only`() {
        val result = dataSource.getTopicsByLanguageCode("fr_FR")

        val expectedTopics = listOf("initial_fr", "news_fr", "promo_fr", "warn_fr")
        assert(result == expectedTopics) {
            "Expected topics for French language from 'fr_FR' but got $result"
        }
    }

    @Test
    fun `given locale with hyphen when getTopicsByLanguageCode called then it should extract base language only`() {
        val result = dataSource.getTopicsByLanguageCode("zh-CN")

        val expectedTopics = listOf("initial_zh", "news_zh", "promo_zh", "warn_zh")
        assert(result == expectedTopics) {
            "Expected topics for Chinese language from 'zh-CN' but got $result"
        }
    }

    @Test
    fun `given uppercase language code when getTopicsByLanguageCode called then it should normalize to lowercase`() {
        val result = dataSource.getTopicsByLanguageCode("FR")

        val expectedTopics = listOf("initial_fr", "news_fr", "promo_fr", "warn_fr")
        assert(result == expectedTopics) {
            "Expected topics for French language from uppercase 'FR' but got $result"
        }
    }

    @Test
    fun `given mixed case locale when getTopicsByLanguageCode called then it should normalize to lowercase base`() {
        val result = dataSource.getTopicsByLanguageCode("Es_ES")

        val expectedTopics = listOf("initial_es", "news_es", "promo_es", "warn_es")
        assert(result == expectedTopics) {
            "Expected topics for Spanish language from 'Es_ES' but got $result"
        }
    }

    @Test
    fun `given empty string when getTopicsByLanguageCode called then it should default to English`() {
        val result = dataSource.getTopicsByLanguageCode("")

        val expectedTopics = listOf("initial_en", "news_en", "promo_en", "warn_en")
        assert(result == expectedTopics) {
            "Expected topics to default to English for empty string but got $result"
        }
    }

    @Test
    fun `given unsupported language code when getTopicsByLanguageCode called then it should default to English`() {
        val result = dataSource.getTopicsByLanguageCode("unsupported")

        val expectedTopics = listOf("initial_en", "news_en", "promo_en", "warn_en")
        assert(result == expectedTopics) {
            "Expected topics to default to English for unsupported language but got $result"
        }
    }

    @Test
    fun `given Indonesian language code when getTopicsByLanguageCode called then it should normalize to id for backend consistency`() {
        val result = dataSource.getTopicsByLanguageCode("in")

        val expectedTopics = listOf("initial_id", "news_id", "promo_id", "warn_id")
        assert(result == expectedTopics) {
            "Expected Indonesian language code 'in' to be normalized to 'id' for backend consistency but got $result"
        }
    }

    @Test
    fun `given Indonesian locale with region when getTopicsByLanguageCode called then it should normalize to id`() {
        val result = dataSource.getTopicsByLanguageCode("in_ID")

        val expectedTopics = listOf("initial_id", "news_id", "promo_id", "warn_id")
        assert(result == expectedTopics) {
            "Expected Indonesian locale 'in_ID' to be normalized to 'id' for backend consistency but got $result"
        }
    }

    @Test
    fun `given id language code when getTopicsByLanguageCode called then it should be recognized as Indonesian`() {
        val result = dataSource.getTopicsByLanguageCode("id")

        val expectedTopics = listOf("initial_id", "news_id", "promo_id", "warn_id")
        assert(result == expectedTopics) {
            "Expected 'id' language code to be recognized as Indonesian and return Indonesian topics but got $result"
        }
    }

    @Test
    fun `given all supported languages when getTopicsByLanguageCode called then it should return correct base language topics`() {
        val testCases = mapOf(
            "en" to "en",
            "es" to "es",
            // Check All possible indonesian combination
            "in" to "id",
            "in_ID" to "id",
            "id" to "id",
            "ar" to "ar",
            "uk" to "uk",
            "ru" to "ru",
            "pt" to "pt",
            "hi" to "hi",
            "de" to "de",
            "fa" to "fa",
            "pa" to "pa",
            "pl" to "pl",
            "ko" to "ko",
            "fr" to "fr",
            "zh-TW" to "zh",
            "zh-CN" to "zh",
            "tr" to "tr",
            "ja" to "ja",
            "it" to "it",
            "sv" to "sv"
        )

        testCases.forEach { (input, expectedBase) ->
            val result = dataSource.getTopicsByLanguageCode(input)
            val expectedTopics = listOf(
                "initial_$expectedBase",
                "news_$expectedBase",
                "promo_$expectedBase",
                "warn_$expectedBase"
            )
            assert(result == expectedTopics) {
                "Expected topics for '$input' to have base '$expectedBase' but got $result"
            }
        }
    }
}
