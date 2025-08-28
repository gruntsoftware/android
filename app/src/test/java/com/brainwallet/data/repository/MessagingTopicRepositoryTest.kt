package com.brainwallet.data.repository

import com.brainwallet.data.model.Language
import com.brainwallet.data.source.MessagingTopicDataSource
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MessagingTopicRepositoryTest {

    @MockK
    private lateinit var mockSettingRepository: SettingRepository

    @MockK
    private lateinit var mockTopicDataSource: MessagingTopicDataSource

    private lateinit var repository: MessagingTopicRepository

    private lateinit var fakeIndonesianTopics: List<String>
    private lateinit var fakeEnglishTopics: List<String>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fakeIndonesianTopics = listOf("initial_in", "news_in", "promo_in", "warn_in")
        fakeEnglishTopics = listOf("initial_en", "news_en", "promo_en", "warn_en")
        repository = MessagingTopicRepository(mockSettingRepository, mockTopicDataSource)
    }

    @Test
    fun `given current language code is in when getCurrentTopics called then repository should return topics for Indonesian`() {
        every { mockSettingRepository.getCurrentLanguage().code } returns "in"
        every { mockTopicDataSource.getTopicsByLanguageCode("in") } returns fakeIndonesianTopics

        val result = repository.getCurrentTopics()

        assert(result == fakeIndonesianTopics) {
            "Expected repository to return topics for Indonesian but got $result"
        }
        verify(exactly = 1) { mockTopicDataSource.getTopicsByLanguageCode("in") }
    }

    @Test
    fun `given language object when getTopicsByLanguage called then repository should return topics for that language`() {
        val language = Language.INDONESIAN
        every { mockTopicDataSource.getTopicsByLanguageCode(language.code) } returns fakeIndonesianTopics

        val result = repository.getTopicsByLanguage(language)

        assert(result == fakeIndonesianTopics) {
            "Expected repository to return topics for Indonesian language but got $result"
        }
        verify(exactly = 1) { mockTopicDataSource.getTopicsByLanguageCode(language.code) }
    }

    @Test
    fun `given English language when getTopicsByLanguage called then repository should return English topics`() {
        val language = Language.ENGLISH
        every { mockTopicDataSource.getTopicsByLanguageCode(language.code) } returns fakeEnglishTopics

        val result = repository.getTopicsByLanguage(language)

        assert(result == fakeEnglishTopics) {
            "Expected repository to return topics for English language but got $result"
        }
        verify(exactly = 1) { mockTopicDataSource.getTopicsByLanguageCode(language.code) }
    }
}
