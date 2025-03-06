package com.brainwallet.ui.screens.home.composable

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.screens.home.composable.settingsrows.CurrencyDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.GamesDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.LanguageDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.LitecoinBlockchainDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.SecurityDetail
import com.brainwallet.ui.screens.home.composable.settingsrows.SettingsSimpleRowItem
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun HomeSettingDrawerSheet(
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(),
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(),
            userScrollEnabled = true
        ) {
            item {
                SecurityDetail(
                    modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight(),)
            }
            item {
                LanguageDetail()
            }
            item {
                CurrencyDetail()
            }
            item {
                GamesDetail()
            }
            item {
                LitecoinBlockchainDetail()
            }
            item {
                SettingsSimpleRowItem(
                    modifier = Modifier,
                    mainLabel = "Support",
                    detailLabel = "Support URL",
                    url = ""
                )
            }
            item {
                SettingsSimpleRowItem(
                    modifier = Modifier,
                    mainLabel = "Social Media",
                    detailLabel = "linktr.ee/brainwallet",
                    url = ""
                )
            }
            item {
//                // Lock / Unlock
//                SettingsActionRowItem(
//                    modifier = Modifier,
//                    mainLabel = "Unlock",
//                    actionType = TODO(),
//                )
            }
        }
    }

    Spacer(modifier = Modifier.height(40.dp))
}

/**
 * for backward compat with XML, for now we are using XML
 * will used by [activity_bread.xml]
 */

class HomeSettingDrawerComposeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbstractComposeView(context, attrs) {
    @Composable
    override fun Content() {
        BrainwalletAppTheme { HomeSettingDrawerSheet() }
    }

}


//     SecurityRowItem()


//                LanguageDetail(
//                    selectedLanguage = state.selectedLanguage,
//                    onDismissRequest = { shouldShowLanguageDetail = false },
//                    onLanguageSelect = { language ->
//                        viewModel.onEvent(
//                            SettingsEvent.OnLanguageChange(
//                                language
//                            )
//                        )
//                    }
//                )
// Currency Dropdown Row