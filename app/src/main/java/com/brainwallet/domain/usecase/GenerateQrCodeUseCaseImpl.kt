package com.brainwallet.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import com.brainwallet.ltc.domain.usecase.GenerateQrCodeUseCase
import com.brainwallet.tools.qrcode.QRUtils
import org.koin.core.annotation.Single

@Single
class GenerateQrCodeUseCaseImpl(
    private val context: Context
) : GenerateQrCodeUseCase {
    override fun generateQrCode(content: String): Bitmap? {
        return QRUtils.generateQR(context, content)
    }
}
