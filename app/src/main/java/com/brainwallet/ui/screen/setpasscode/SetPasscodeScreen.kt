@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)

package com.brainwallet.ui.screen.setpasscode

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.screen.yourseedproveit.YourSeedProveItEvent

//OnSetBiometrics



@Composable
fun SetPasscodeScreen (
    onEvent: (SetPasscodeScreenEvent) -> Unit = {},
    viewModel: SetPasscodeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    /// Layout values
    val columnPadding = 18
    val tinyPadding = 4

    val smallPadding = 8
    val textPadding = 16

    val horizontalVerticalSpacing = 8
    val spacerHeight = 90
    val imageMedium = 90

    val detailPartOne = R.string.ready_setup_instructions_1
    val detailPartTwo = R.string.ready_setup_instructions_2
    val detailCombined = detailPartOne + detailPartTwo

    LaunchedEffect(Unit) {
        //viewModel.onEvent(SetPasscodeScreenEvent.OnLoad(seedWords))
    }

    LaunchedEffect(Unit) {
        ///
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(
                    onClick = { onEvent.invoke(SetPasscodeScreenEvent.OnBackClick) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            })
        }

    ) { paddingValues ->

        Spacer(modifier = Modifier.height(spacerHeight.dp))
        Column(
            modifier = Modifier
                .background(
                    color = Color(0xFF2C2C2C)
                )
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .background(
                        color = Color(0xFF2C2C2C),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                    .padding(horizontal = textPadding.dp),
            ) {

                Image(
                    painterResource(R.drawable.ic_down_left_white_arrow),
                    contentDescription = "down-left-arrow",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(imageMedium.dp)
                        .padding(columnPadding.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
            }


//            Text(
//                text = stringResource(R.string.ready_setup),
//                style = MaterialTheme.typography.headlineLarge.copy(color = Color.White),
//                textAlign = TextAlign.Left,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = textPadding.dp, vertical = smallPadding.dp)
//            )
//
//            Text(
//                text = stringResource(detailCombined),
//                style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
//                textAlign = TextAlign.Left,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = textPadding.dp)
//            )

            LargeButton(
                onClick = {
                    onEvent.invoke(SetPasscodeScreenEvent.OnSetPasscode)
                },
            ) {
                Text(
                    text = stringResource(R.string.setup_app_passcode),
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White),
                    modifier = Modifier.padding()
                )
            }
        }
    }
}

@Preview
@Composable
fun SetPasscodeScreenPreview() {
    SetPasscodeScreen()
}



