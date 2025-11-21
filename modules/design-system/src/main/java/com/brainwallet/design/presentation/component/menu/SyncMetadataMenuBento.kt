package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Sync metadata menu item for bento rail.
 * Displays synchronization status and metadata information.
 */
@Composable
fun SyncMetadataMenuBento(
    modifier: Modifier = Modifier,
    syncDescription: String = stringResource(R.string.design_menu_sync_metadata_no_data),
    onClick: (() -> Unit)? = null
) {
    BentoMenuBase(
        title = stringResource(R.string.design_menu_sync_metadata_title),
        description = syncDescription,
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun SyncMetadataMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        SyncMetadataMenuBento()
    }
}
