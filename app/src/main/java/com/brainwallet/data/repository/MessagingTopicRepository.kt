package com.brainwallet.data.repository

import com.brainwallet.data.model.Language
import com.brainwallet.data.source.MessagingTopicDataSource
import org.koin.core.annotation.Single

@Single
class MessagingTopicRepository(
    private val settingRepository: SettingRepository,
    private val topicDataSource: MessagingTopicDataSource
) {
    fun getCurrentTopics(): List<String> {
        val currentLanguage = settingRepository.getCurrentLanguage()
        return topicDataSource.getTopicsByLanguageCode(currentLanguage.code)
    }

    fun getTopicsByLanguage(language: Language): List<String> {
        return topicDataSource.getTopicsByLanguageCode(language.code)
    }
}
