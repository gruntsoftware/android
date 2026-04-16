package com.brainwallet.util

import com.brainwallet.BuildConfig
import org.koin.core.annotation.Factory

@Factory
class VersionCodeProvider(
    private val versionCodeGetter: () -> Int = { BuildConfig.VERSION_CODE },
    private val versionNameGetter: () -> String = { BuildConfig.VERSION_NAME }
) {
    fun getVersionCode(): Int {
        return versionCodeGetter()
    }

    fun getVersionName(): String {
        return versionNameGetter()
    }

    fun getFormatted(): String {
        return "${getVersionName()} (${getVersionCode()})"
    }

    companion object {
        fun getVersionLabel() {
            TODO("Not yet implemented")
        }
    }
}
