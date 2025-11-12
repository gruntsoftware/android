package com.brainwallet.ui.bento

import android.content.Context
import com.brainwallet.data.repository.SyncAnalyticsRepository
import com.brainwallet.design.presentation.component.rail.BentoRailDataProvider
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.util.BRConstants
import org.koin.core.annotation.Single

@Single
class BentoRailDataProviderImpl(
    private val context: Context,
    private val syncAnalyticsRepository: SyncAnalyticsRepository
) : BentoRailDataProvider {

    override fun getShareAnalyticsEnabled(): Boolean {
        return BRSharedPrefs.getShareData(context)
    }

    override fun getSelectedLanguage(): String {
        return "English"
    }

    override fun getSelectedCurrency(): String {
        return "USD"
    }

    override fun getSelectedFeeType(): String {
        return "Regular"
    }

    override fun getSyncDescription(): String {
        return syncAnalyticsRepository.getLastSyncMetadata()?.let {
            SyncAnalyticsRepository.SyncMetadata.Formatter().format(it)
        } ?: "No sync metadata"
    }

    override fun getAppVersion(): String {
        return BRConstants.APP_VERSION_NAME_CODE
    }
}
