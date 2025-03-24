package com.brainwallet.ui.screens.ready

import android.content.Context

sealed class ReadyEvent {
    data class OnLoad(val context: Context) : ReadyEvent()
}