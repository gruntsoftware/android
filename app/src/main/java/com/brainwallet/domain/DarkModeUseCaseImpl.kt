package com.brainwallet.domain

import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.design.domain.DarkModeUseCase
import org.koin.core.annotation.Single

@Single
class DarkModeUseCaseImpl(
    private val settingRepository: SettingRepository
) : DarkModeUseCase {
    override fun isDarkMode(): Boolean {
        return settingRepository.isDarkMode()
    }

    override fun toggleDarkMode(isDarkMode: Boolean) {
        settingRepository.toggleDarkMode(isDarkMode)
    }
}
