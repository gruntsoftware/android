package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Lock/unlock menu item for bento rail.
 * Provides access to app lock and security features.
 */
@Composable
fun LockMenuBento(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = stringResource(R.string.design_menu_lock_title),
        description = stringResource(R.string.design_menu_lock_description),
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun LockMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        LockMenuBento()
    }
}
