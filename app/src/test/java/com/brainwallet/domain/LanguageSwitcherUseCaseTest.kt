package com.brainwallet.domain

import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.verifyOrder
import org.junit.Before
import org.junit.Test

class LanguageSwitcherUseCaseTest {

    private lateinit var useCase: LanguageSwitcherUseCase

    @MockK
    private lateinit var mockSettingRepository: SettingRepository

    @MockK
    private lateinit var mockMessagingTopicUseCase: MessagingTopicUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        useCase = LanguageSwitcherUseCase(mockSettingRepository, mockMessagingTopicUseCase)
    }

    @Test
    fun `given new language when switchLanguage called then it should subscribe by language and update current language`() {
        val newLanguage = Language.FRENCH

        useCase.switchLanguage(newLanguage)

        // Order is important to make old subscribed language still valid
        verifyOrder {
            mockMessagingTopicUseCase.subscribeByLanguage(newLanguage)
            mockSettingRepository.updateCurrentLanguage(newLanguage.code)
        }
    }
}
