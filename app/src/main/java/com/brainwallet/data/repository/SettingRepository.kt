package com.brainwallet.data.repository

import com.brainwallet.data.model.AppSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SettingRepository {

    val settings: Flow<AppSetting>

    suspend fun save(setting: AppSetting)

    class Impl : SettingRepository {

        //todo: please persist using sharedpref or datastore
        private val _state = MutableStateFlow(AppSetting())
        val state: StateFlow<AppSetting> = _state.asStateFlow()

        override val settings: Flow<AppSetting>
            get() = state

        override suspend fun save(setting: AppSetting) {
            _state.update { setting }
        }
    }
}