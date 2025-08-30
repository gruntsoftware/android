package com.brainwallet.domain

import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.MessagingTopicRepository
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class MessagingTopicUseCaseTest {

    private lateinit var useCase: MessagingTopicUseCase

    @MockK
    private lateinit var mockRepository: MessagingTopicRepository

    @RelaxedMockK
    private lateinit var mockFirebaseMessaging: FirebaseMessaging
    private lateinit var currentTopics: List<String>
    private lateinit var newTopics: List<String>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        currentTopics = listOf("initial_en", "news_en", "promo_en", "warn_en")
        newTopics = listOf("initial_fr", "news_fr", "promo_fr", "warn_fr")

        useCase = MessagingTopicUseCase(mockRepository, mockFirebaseMessaging)
    }

    @Test
    fun `given current topics when initialize called then it should subscribe to all current topics`() {
        every { mockRepository.getCurrentTopics() } returns currentTopics

        useCase.initialize()

        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("initial_en") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("news_en") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("promo_en") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("warn_en") }
    }

    @Test
    fun `given current topics and new language when subscribeByLanguage called then it should unsubscribe old and subscribe new`() {
        every { mockRepository.getCurrentTopics() } returns currentTopics
        every { mockRepository.getTopicsByLanguage(any()) } returns newTopics

        val newLanguage = Language.FRENCH

        useCase.subscribeByLanguage(newLanguage)

        verify(exactly = 1) { mockFirebaseMessaging.unsubscribeFromTopic("initial_en") }
        verify(exactly = 1) { mockFirebaseMessaging.unsubscribeFromTopic("news_en") }
        verify(exactly = 1) { mockFirebaseMessaging.unsubscribeFromTopic("promo_en") }
        verify(exactly = 1) { mockFirebaseMessaging.unsubscribeFromTopic("warn_en") }

        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("initial_fr") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("news_fr") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("promo_fr") }
        verify(exactly = 1) { mockFirebaseMessaging.subscribeToTopic("warn_fr") }
    }
}
