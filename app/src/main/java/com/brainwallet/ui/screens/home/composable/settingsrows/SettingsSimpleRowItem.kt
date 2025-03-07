package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.theme.BrainwalletTheme

//TODO
@Composable
fun SettingsSimpleRowItem(
    modifier: Modifier = Modifier,
    mainLabel: String,
    detailLabel: String,
    isDetailAURL: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    /// Layout values
    val contentHeight = 60
    val horizontalPadding = 14
    val verticalPadding = 4
    val dividerThickness = 1

Column(
        modifier = modifier
    ) {
        HorizontalDivider(thickness = dividerThickness.dp, color = BrainwalletTheme.colors.content)

        Row (
                modifier = Modifier
                    .height(contentHeight.dp)
                    .padding(horizontal = horizontalPadding.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
            Column {
                Text(modifier = Modifier
                    .padding(vertical = verticalPadding.dp),
                    text = mainLabel,
                    style = MaterialTheme.typography.labelLarge
                        .copy(textAlign = TextAlign.Left)
                )
                Text(modifier = Modifier
                    .padding(vertical = verticalPadding.dp),
                    text = detailLabel,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(textAlign = TextAlign.Right)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}