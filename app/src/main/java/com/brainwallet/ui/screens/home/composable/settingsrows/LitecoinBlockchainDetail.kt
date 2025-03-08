package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brainwallet.ui.theme.BrainwalletTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.data.model.Language
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.home.composable.RowActionType

//TODO
@Composable
fun LitecoinBlockchainDetail(
    modifier: Modifier = Modifier,
    onUserDidStartSync: (Boolean) -> Unit,
    viewModel: SettingsViewModel = viewModel()

) {
    var expanded by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()

    /// Layout values
    val closedHeight = 60
    val expandedHeight = 100
    val toggleButtonSize = 40
    val dividerThickness = 1
    val iconButtonSize = 22

    Column(
        modifier = modifier
            .background (if (expanded) BrainwalletTheme.colors.background else BrainwalletTheme.colors.surface)
    ) {
        HorizontalDivider(thickness = dividerThickness.dp, color = BrainwalletTheme.colors.content)
        DropdownMenuItem(
            modifier = Modifier
                .height(closedHeight.dp),
            colors = MenuDefaults.itemColors(
                textColor = BrainwalletTheme.colors.content,
                trailingIconColor = BrainwalletTheme.colors.content,
            ),
            text = {
                Text(
                    text = stringResource(R.string.blockchain_litecoin),
                    style = MaterialTheme.typography.labelLarge
                        .copy(textAlign = TextAlign.Left)
                )
            },
            onClick = {
                expanded = expanded.not()
            },
            trailingIcon = {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
        )

        AnimatedVisibility(visible = expanded) {
            Row(
                modifier = Modifier
                    .height(expandedHeight.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))

                DarkModeToggleButton(
                    modifier = Modifier
                        .width(toggleButtonSize.dp)
                        .aspectRatio(1f),
                    checked = state.darkMode,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.OnUserDidStartSync)
                    }
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .width(iconButtonSize.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .border(
                                1.dp,
                                if (state.darkMode) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                                CircleShape
                            )
                            .background(if (state.darkMode) BrainwalletTheme.colors.surface else BrainwalletTheme.colors.content)
                    ) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(iconButtonSize.dp)
                                .aspectRatio(1f),
                            tint = if (state.darkMode) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                            painter = painterResource(if (state.darkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                            contentDescription = stringResource(R.string.toggle_dark_mode),
                        )
                    }

                }
            }
        }
    }
}

//
//@Composable
//fun LanguageDetail(
//    selectedLanguage: Language,
//    onDismissRequest: () -> Unit,
//    onLanguageSelect: (Language) -> Unit
//) {
//    Column() {
//
//        Spacer(modifier = Modifier.weight(0.1f))
//
//        LazyColumn {
//            val languages = Language.entries.toTypedArray()
//            val lastLanguageItem = languages[languages.size - 1]
//            val lastLanguageBottomPad = 16
//            lateinit var settingRepository: SettingRepository
//            val userPreferredLanguage = settingRepository.getCurrentLanguage()
//
//            items(
//                items = languages
//            ) { language ->
//                ListItem(
//                    colors = ListItemDefaults.colors(
//                        containerColor = BrainwalletTheme.colors.background,
//                        headlineColor = BrainwalletTheme.colors.content,
//                    ),
//                    modifier = Modifier.clickable {
//                        if (language.code.isNotBlank()) {
//                            onLanguageSelect.invoke(language)
//                        }
//                    }
//                        .padding(bottom = if (language == lastLanguageItem) lastLanguageBottomPad.dp else 0.dp),
//                    headlineContent = {
//                        Text(language.title)
//                    },
//                    trailingContent = {
//                        if (userPreferredLanguage.code == language.code) {
//                            Icon(
//                                Icons.Default.CheckCircle,
//                                contentDescription = null,
//                                tint = BrainwalletTheme.colors.affirm
//                            )
//                        }
//                    }
//                )
//            }
//        }
//    }
//}