package com.brainwallet.domain

import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.MessagingTopicRepository
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.core.annotation.Single

@Single
class MessagingTopicUseCase(
    private val messagingTopicRepository: MessagingTopicRepository,
    private val firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {
    fun initialize() {
        val topics = messagingTopicRepository.getCurrentTopics()
        subscribeToTopics(topics)
    }

    fun subscribeByLanguage(newLanguage: Language) {
        val currentTopics = messagingTopicRepository.getCurrentTopics()
        unsubscribeFromTopics(currentTopics)
        val newTopics = messagingTopicRepository.getTopicsByLanguage(newLanguage)
        subscribeToTopics(newTopics)
    }

    private fun subscribeToTopics(topics: List<String>) {
        topics.forEach { topic ->
            firebaseMessaging.subscribeToTopic(topic)
        }
    }

    private fun unsubscribeFromTopics(topics: List<String>) {
        topics.forEach { topic ->
            firebaseMessaging.unsubscribeFromTopic(topic)
        }
    }
}
