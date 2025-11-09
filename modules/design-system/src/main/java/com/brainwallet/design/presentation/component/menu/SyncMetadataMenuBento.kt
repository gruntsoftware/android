package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Sync metadata menu item for bento rail.
 * Displays synchronization status and metadata information.
 */
@Composable
fun SyncMetadataMenuBento(
    modifier: Modifier = Modifier,
    syncDescription: String = "No sync metadata",
    onClick: (() -> Unit)? = null
) {
    BentoMenuBase(
        title = "Sync Metadata",
        description = syncDescription,
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun SyncMetadataMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        SyncMetadataMenuBento(
            syncDescription = "Last sync: 2 hours ago"
        )
    }
}
