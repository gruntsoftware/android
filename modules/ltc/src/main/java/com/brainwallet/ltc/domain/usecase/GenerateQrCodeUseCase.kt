package com.brainwallet.ltc.domain.usecase

import android.graphics.Bitmap

interface GenerateQrCodeUseCase {
    fun generateQrCode(content: String): Bitmap?
}
