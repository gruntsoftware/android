@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.settingsrows.LanguageDetail
import com.brainwallet.ui.screens.welcome.WelcomeEvent
import com.brainwallet.ui.screens.welcome.WelcomeViewModel

@Composable
fun SettingsScreen(
    shouldShowSettingsScreen: Boolean,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val configuration = LocalConfiguration.current

    ///Language
    var shouldShowLanguageDetail by remember { mutableStateOf(false) }


    /// Layout values
    val leadingCopyPadding = 16

    val horizontalVerticalSpacing = 8
    val spacerHeight = 90

    val activeRowHeight = 70


    LazyColumn {

            // Security Row >
            item {
                Text(text = "First item")
            }
            // Language Row >
            item {
                LanguageDetail(
                    selectedLanguage = state.selectedLanguage,
                    onDismissRequest = { shouldShowLanguageDetail = false },
                    onLanguageSelect = { language ->
                        viewModel.onEvent(
                            SettingsEvent.OnLanguageChange(
                            language
                            )
                        )
                    }
                )
            }
            // Currency Dropdown Row
            item {
                Text(text = "First item")
            }
            // Games >
            item {
                Text(text = "First item")
            }
            // Blockchain: Litecoin >
            item {
                Text(text = "First item")
            }
            // Support
            item {
                Text(text = "First item")
            }
            // Social
            item {
                Text(text = "First item")
            }

            // Lock / Unlock
            item {
                Text(text = "First item")
            }


        }
}
