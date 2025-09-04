package com.brainwallet.domain

import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
import org.koin.core.annotation.Single

@Single
class LanguageSwitcherUseCase(
    private val settingRepository: SettingRepository,
    private val messagingTopicUseCase: MessagingTopicUseCase
) {
    fun switchLanguage(newLanguage: Language) {
        messagingTopicUseCase.subscribeByLanguage(newLanguage)
        settingRepository.updateCurrentLanguage(newLanguage.code)
    }
}
