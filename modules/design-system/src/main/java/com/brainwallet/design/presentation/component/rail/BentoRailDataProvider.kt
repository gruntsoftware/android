package com.brainwallet.design.presentation.component.rail

interface BentoRailDataProvider {
    fun getShareAnalyticsEnabled(): Boolean
    fun getSelectedLanguage(): String
    fun getSelectedCurrency(): String
    fun getSelectedFeeType(): String
    fun getSyncDescription(): String
    fun getAppVersion(): String
}
