package com.brainwallet.ui.screens.home.receive

import android.content.Context

sealed class ReceiveDialogEvent {
    data class OnLoad(val context: Context) : ReceiveDialogEvent()
    data class OnCopyClick(val context: Context) : ReceiveDialogEvent()
}