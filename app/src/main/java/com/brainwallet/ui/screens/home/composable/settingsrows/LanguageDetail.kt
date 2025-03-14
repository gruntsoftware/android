package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.Language
import com.brainwallet.ui.theme.BrainwalletTheme

//TODO
@Composable
fun LanguageDetail(
    modifier: Modifier = Modifier,
    selectedLanguage: Language,
    onLanguageSelect: (Language) -> Unit
) {
    val languages = Language.entries.toTypedArray()

    /// Layout values
    val closedHeight = 60
    val expandedHeight = 150
    val dividerThickness = 1
    val unselectedCircleSize = 20


    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_language)
    ) {
        LazyColumn(modifier = Modifier.height(expandedHeight.dp)) {
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
                        Text(
                            modifier = Modifier,
                            text = language.title,
                            style = MaterialTheme.typography.bodyMedium
                                .copy(textAlign = TextAlign.Left)
                        )
                    },
                    trailingContent = {
                        if (selectedLanguage.code == language.code) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrainwalletTheme.colors.affirm
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(unselectedCircleSize.dp)
                                    .alpha(0.1f)
                                    .clip(CircleShape)
                                    .background(BrainwalletTheme.colors.content)
                            )
                        }
                    }
                )
            }
        }
    }
}