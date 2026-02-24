package com.brainwallet.ui.screens.send

import android.content.Context

sealed class SendEvent {
    data class OnLoad(val context: Context) : SendEvent()
}
