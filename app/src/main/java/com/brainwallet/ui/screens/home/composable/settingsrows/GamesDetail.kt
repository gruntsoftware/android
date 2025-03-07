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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R

@Composable
fun GamesDetail(
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    /// Layout values
    val closedHeight = 60
    val expandedHeight = 100
    val dividerThickness = 1
    val horizontalPadding = 14

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
                Text(text = stringResource(R.string.settings_title_games),
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
            },

        )

        AnimatedVisibility(visible = expanded) {
            Row(modifier = Modifier
                .height(expandedHeight.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text("TODO CONTENT HERE")
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