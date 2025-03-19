package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R

@Composable
fun GamesDetail(
    modifier: Modifier = Modifier
) {
    /// Layout values
    val contentHeight = 60
    val horizontalPadding = 14

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_games)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.game_title, 1))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                }) {
                    Text(stringResource(R.string.game_buy_price_1, "TBD"))
                }
            }

            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.game_title, 2))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                }) {
                    Text(stringResource(R.string.game_buy_price_2, "TBD"))
                }
            }

            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.game_title, 3))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                }) {
                    Text(stringResource(R.string.game_buy_price_3, "TBD"))
                }
            }
            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.game_title, 4))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                }) {
                    Text(stringResource(R.string.game_buy_price_4, "TBD"))
                }
            }

        }
    }

}