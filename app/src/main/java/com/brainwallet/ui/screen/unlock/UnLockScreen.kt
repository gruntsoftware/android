@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.brainwallet.ui.screen.unlock

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.screen.unlock.composable.CircleButton
import com.brainwallet.ui.screen.unlock.composable.PinDotItem

//TODO: WIP here
@Composable
fun UnLockScreen(
    onEvent: (UnLockEvent) -> Unit,
    viewModel: UnLockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.pinDigits.all { it > -1 }) {
        //
    }

    /// Layout values
    val columnPadding = 18
    val horizontalVerticalSpacing = 8
    val spacerHeight = 48
    val maxItemsPerRow = 3

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(
                    onClick = { onEvent.invoke(UnLockEvent.OnBackClick) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                    )
                }
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(columnPadding.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {

            Image(
                painter = painterResource(R.drawable.brainwallet_logotype_white),
                contentDescription = "logo"
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White)
            ) {
                //todo
                Text("todo")
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.pinDigits.forEach { digit ->
                    PinDotItem(checked = digit > -1)
                }
            }

            Spacer(Modifier.height(16.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                maxItemsInEachRow = maxItemsPerRow
            ) {
                //pin number button

                repeat(9) { index ->
                    val number = index + 1
                    CircleButton(
                        onClick = {
                            viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = number))
                        },
                    ) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                }

                // Bottom row with biometric, 0, and backspace
                if (state.biometricEnabled) {
                    CircleButton(
                        onClick = {
                            //
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Transparent
                        ),
                    ) {
                        Icon(
                            Icons.Default.Face,
                            contentDescription = "Biometric",
                            tint = Color.White
                        )
                    }
                } else {
                    CircleButton(
                        onClick = { },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            disabledContainerColor = Color.Transparent
                        ),
                        enabled = false,
                    ) {
                        Spacer(modifier = Modifier)
                    }
                }

                CircleButton(
                    onClick = {
                        viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 0))
                    },
                ) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }

                CircleButton(
                    onClick = {
                        viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Transparent
                    ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //app version
            Text(
                text = BRConstants.APP_VERSION_NAME_CODE,
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )
        }
    }
}