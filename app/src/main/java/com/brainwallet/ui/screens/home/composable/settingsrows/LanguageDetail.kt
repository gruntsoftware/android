package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun LanguageDetail(
    selectedLanguage: Language,
    onDismissRequest: () -> Unit,
    onLanguageSelect: (Language) -> Unit
) {
    Column() {

        Spacer(modifier = Modifier.weight(0.1f))

        LazyColumn {
            val languages = Language.entries.toTypedArray()
            val lastLanguageItem = languages[languages.size - 1]
            val lastLanguageBottomPad = 16
            lateinit var settingRepository: SettingRepository
            val userPreferredLanguage = settingRepository.getCurrentLanguage()

            items(
                items = languages
            ) { language ->
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = BrainwalletTheme.colors.background,
                        headlineColor = BrainwalletTheme.colors.content,
                    ),
                    modifier = Modifier.clickable {
                        if (language.code.isNotBlank()) {
                            onLanguageSelect.invoke(language)
                        }
                    }
                        .padding(bottom = if (language == lastLanguageItem) lastLanguageBottomPad.dp else 0.dp),
                    headlineContent = {
                        Text(language.title)
                    },
                    trailingContent = {
                        if (userPreferredLanguage.code == language.code) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrainwalletTheme.colors.affirm
                            )
                        }
                    }
                )
            }
        }
    }
}