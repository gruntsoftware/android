package com.brainwallet.ui.screens.main.history

import android.content.Context

sealed class HistoryEvent {
    data class OnLoad(val context: Context) : HistoryEvent()
}
