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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.DesignTheme

@Composable
fun BrainwalletScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = DesignTheme.colors.surface,
        contentColor = DesignTheme.colors.content,
        topBar = topBar,
        bottomBar = bottomBar,
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
            containerColor = DesignTheme.colors.surface,
            navigationIconContentColor = DesignTheme.colors.content
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
        containerColor = DesignTheme.colors.background,
        contentColor = DesignTheme.colors.content,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = DesignTheme.colors.content)
        },
        onDismissRequest = onDismissRequest,
        content = content
    )
}

@Composable
fun BrainwalletButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CircleShape,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = DesignTheme.colors.surface,
        contentColor = DesignTheme.colors.content
    ),
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = colors,
        modifier = modifier
            .border(1.dp, DesignTheme.colors.border, shape)
            .height(50.dp),
        content = content
    )
}
