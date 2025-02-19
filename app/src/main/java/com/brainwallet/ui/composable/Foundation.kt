@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun BrainwalletScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = BrainwalletTheme.colors.surface,
        contentColor = BrainwalletTheme.colors.content,
        topBar = topBar,
        content = content
    )
}

@Composable
fun BrainwalletTopAppBar(
    title: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
) {
    TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BrainwalletTheme.colors.surface,
            navigationIconContentColor = BrainwalletTheme.colors.content
        ),
        title = title,
        navigationIcon = navigationIcon
    )
}