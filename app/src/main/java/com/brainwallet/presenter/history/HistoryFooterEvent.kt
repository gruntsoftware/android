package com.brainwallet.presenter.history

sealed class HistoryFooterEvent {
    /** The export button was tapped, or the export sheet was dismissed. */
    data object OnToggleExportSheet : HistoryFooterEvent()
}
