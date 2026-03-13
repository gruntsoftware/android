package com.brainwallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.brainwallet.R
import org.koin.compose.koinInject

val BoldenVan = FontFamily(
    Font(R.font.bolden_van, FontWeight.Normal)
)

val IBMPlexSans = FontFamily(
    Font(R.font.ibm_plex_sans_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_sans_medium, FontWeight.Medium),
    Font(R.font.ibm_plex_sans_semi_bold, FontWeight.SemiBold),
    Font(R.font.ibm_plex_sans_bold, FontWeight.Bold),
    Font(R.font.ibm_plex_sans_extra_light, FontWeight.ExtraLight),
    Font(R.font.ibm_plex_sans_light, FontWeight.Light),
    Font(R.font.ibm_plex_sans_thin, FontWeight.Thin),
    Font(R.font.ibm_plex_sans_italic, FontWeight.Normal, FontStyle.Italic)
)

@Composable
fun BrainwalletAppTheme(
    appSetting: AppSetting = koinInject<SettingRepository>().settings.collectAsStateWithLifecycle(
        AppSetting()
    ).value,
    content:
    @Composable()
    () -> Unit
) {
    BrainwalletTheme(
        darkTheme = appSetting.isDarkMode,
        languageCode = appSetting.languageCode,
        content = content
    )
}

fun ComposeView.setContentWithTheme(content: @Composable () -> Unit) {
    setContent {
        BrainwalletAppTheme { content.invoke() }
    }
}
