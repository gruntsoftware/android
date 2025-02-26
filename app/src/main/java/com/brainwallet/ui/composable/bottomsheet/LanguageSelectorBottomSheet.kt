package com.brainwallet.ui.composable.bottomsheet

import androidx.compose.foundation.clickable
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
import com.brainwallet.data.model.Language
import com.brainwallet.ui.composable.BrainwalletBottomSheet
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun LanguageSelectorBottomSheet(
    selectedLanguage: Language,
    onDismissRequest: () -> Unit,
    onLanguageSelect: (Language) -> Unit
) {
    BrainwalletBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn {
            val languages = Language.entries.toTypedArray()
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
                    },
                    headlineContent = {
                        Text(language.title)
                    },
                    trailingContent = {
                        if (selectedLanguage.code == language.code) {
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