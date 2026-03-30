package com.brainwallet.ui.screens.restore

import android.content.Context
import com.brainwallet.navigation.Route

sealed class RestoreEvent {
    data class OnLoad(
        val source: Route.Restore.Source? = null
    ) : RestoreEvent()

    data class OnSeedWordItemChange(
        val index: Int,
        val text: String
    ) : RestoreEvent()

    object OnClearSeedWords : RestoreEvent()

    data class OnRestoreClick(
        val context: Context
    ) : RestoreEvent()
}
