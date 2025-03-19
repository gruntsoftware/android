package com.brainwallet.ui.screens.home.composable

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.home.composable.settingsrows.CurrencyDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.GamesDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.LanguageDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.LitecoinBlockchainDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.LockSettingRowItem
import com.brainwallet.ui.screens.home.composable.settingsrows.SecurityDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.SettingRowItem
import com.brainwallet.ui.screens.home.composable.settingsrows.ThemeSettingRowItem
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import com.brainwallet.util.EventBus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.koin.compose.koinInject
import org.koin.java.KoinJavaComponent.inject

@Composable
fun HomeSettingDrawerSheet(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    /// Layout values
    val headerPadding = 56

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = headerPadding.dp)
                .wrapContentHeight(align = Alignment.Top)
        ) {
            item {
                SecurityDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    onEvent = {
                        viewModel.onEvent(it)
                    }
                )
            }
            item {
                LanguageDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    selectedLanguage = state.selectedLanguage,
                    onLanguageSelect = { language ->
                        viewModel.onEvent(
                            SettingsEvent.OnLanguageChange(
                                language
                            )
                        )
                    }
                )

            }
            item {
                CurrencyDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    selectedCurrency = state.selectedCurrency,
                    onFiatSelect = { currency ->
                        viewModel.onEvent(
                            SettingsEvent.OnFiatChange(
                                currency
                            )
                        )
                    }
                )
            }
            item {
                GamesDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight()
                )
            }
            item {
                LitecoinBlockchainDetail(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(),
                    onUserDidStartSync = {
                        //viewModel.onEvent(SettingsEvent.OnUserDidStartSync)
                    }
                )
            }
            item {
                SettingRowItem(
                    title = stringResource(R.string.settings_title_support),
                    description = "brainwallet.co"
                )
            }
            item {
                SettingRowItem(
                    title = stringResource(R.string.settings_title_social_media),
                    description = "linktr.ee/brainwallet"
                )
            }
            item {
                // Lock / Unlock
                LockSettingRowItem {
                    viewModel.onEvent(SettingsEvent.OnToggleLock)
                }
            }
            item {
                // Theme
                ThemeSettingRowItem(
                    darkMode = state.darkMode,
                    onToggledDarkMode = {
                        viewModel.onEvent(SettingsEvent.OnToggleDarkMode)
                    }
                )
            }

            item {
                SettingRowItem(
                    title = stringResource(R.string.settings_title_app_version),
                    description = BRConstants.APP_VERSION_NAME_CODE
                )
            }
        }
    }
}

/**
 * for backward compat with XML, for now we are using XML
 * will used by [activity_bread.xml]
 */

class HomeSettingDrawerComposeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbstractComposeView(context, attrs) {

    private val settingsViewModel: SettingsViewModel by inject(SettingsViewModel::class.java)

    @Composable
    override fun Content() {
        val appSetting by settingsViewModel.appSetting.collectAsState(
            AppSetting()
        )
        BrainwalletAppTheme(appSetting = appSetting) {
            HomeSettingDrawerSheet(viewModel = settingsViewModel)
        }
    }

    fun observeBus(
        onEach: (EventBus.Event.Message) -> Unit
    ) {
        EventBus.events
            .filter { it is EventBus.Event.Message }
            .map { it as EventBus.Event.Message }
            .onEach { onEach.invoke(it) }
            .launchIn(findViewTreeLifecycleOwner()!!.lifecycle.coroutineScope)
    }
}