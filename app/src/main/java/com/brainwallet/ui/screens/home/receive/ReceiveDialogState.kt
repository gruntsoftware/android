package com.brainwallet.ui.screens.home.receive

import android.graphics.Bitmap

data class ReceiveDialogState(
    val address: String = "",
    val qrBitmap: Bitmap? = null
)
