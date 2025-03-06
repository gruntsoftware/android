package com.brainwallet.ui.screens.home.composable

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.unit.dp
import com.brainwallet.tools.util.BRConstants
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

    /// Layout values
    val headerPadding = 16

    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(),
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {
        Column {
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
                            .wrapContentHeight()
                    )
                }
                item {
                    LanguageDetail(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight()
                    )
                }
                item {
                    CurrencyDetail(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentHeight()
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
                            .wrapContentHeight()
                    )
                }
                item {
                    SettingsSimpleRowItem(
                        modifier = Modifier,
                        mainLabel = "Support",
                        detailLabel = "Support URL",
                    )
                }
                item {
                    SettingsSimpleRowItem(
                        modifier = Modifier,
                        mainLabel = "Social Media",
                        detailLabel = "linktr.ee/brainwallet",
                    )
                }
                item {
                    // Lock / Unlock
                    SettingsSimpleRowItem(
                        modifier = Modifier,
                        mainLabel = "Unlock",
                        detailLabel = "",
                        actionType = RowActionType.LOCK_TOGGLE,
                    )
                }
                item {
                    // Theme
                    SettingsSimpleRowItem(
                        modifier = Modifier,
                        mainLabel = "Theme",
                        detailLabel = "",
                        actionType = RowActionType.THEME_TOGGLE,
                    )
                }

                item {
                    SettingsSimpleRowItem(
                        modifier = Modifier,
                        mainLabel = "App version:",
                        detailLabel = BRConstants.APP_VERSION_NAME_CODE,
                    )
                }
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
    @Composable
    override fun Content() {
        BrainwalletAppTheme { HomeSettingDrawerSheet() }
    }
}


enum class RowActionType (
    val code: String,
) {
    SLIDER(code = "SLIDER"),
    THEME_TOGGLE(code = "THEME_TOGGLE"),
    LOCK_TOGGLE(code = "LOCK_TOGGLE")
}