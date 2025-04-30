@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun BrainwalletScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = BrainwalletTheme.colors.surface,
        contentColor = BrainwalletTheme.colors.content,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
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

@Composable
fun BrainwalletBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        containerColor = BrainwalletTheme.colors.background,
        contentColor = BrainwalletTheme.colors.content,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = BrainwalletTheme.colors.content)
        },
        onDismissRequest = onDismissRequest,
        content = content
    )
}

@Composable
fun BrainwalletButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrainwalletTheme.colors.surface,
        contentColor = BrainwalletTheme.colors.content
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = colors,
        modifier = modifier
            .border(1.dp, BrainwalletTheme.colors.border, CircleShape)
            .height(50.dp),
        content = content
    )
}
