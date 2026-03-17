package com.brainwallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import org.koin.compose.koinInject

@Composable
fun BrainwalletAppTheme(
    appSetting: AppSetting = koinInject<SettingRepository>().settings.collectAsStateWithLifecycle(
        AppSetting()
    ).value,
    content:
    @Composable()
    () -> Unit
) {
    DesignTheme(
        isDarkMode = appSetting.isDarkMode,
        languageCode = appSetting.languageCode,
        content = content
    )
}
fun ComposeView.setContentWithTheme(content: @Composable () -> Unit) {
    setContent {
        BrainwalletAppTheme { content.invoke() }
    }
}
