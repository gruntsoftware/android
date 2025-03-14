package com.brainwallet.ui.composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun DarkModeToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconButtonSize = 32

    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .width(iconButtonSize.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(
                    1.dp,
                    if (checked) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                    CircleShape
                )
                .background(if (checked) BrainwalletTheme.colors.surface else BrainwalletTheme.colors.content)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(iconButtonSize.dp)
                    .aspectRatio(1f),
                tint = if (checked) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                painter = painterResource(if (checked) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                contentDescription = stringResource(R.string.toggle_dark_mode),
            )
        }
    }
}