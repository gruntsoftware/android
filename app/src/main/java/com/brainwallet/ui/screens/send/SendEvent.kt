package com.brainwallet.ui.screens.send

import java.math.BigDecimal

sealed class SendEvent {

    data object OnLoad : SendEvent()
    data class OnSend(val sendAmount: BigDecimal) : SendEvent()
    data object OnTapPasteLTCAddress : SendEvent()
    data object OnTapShowCameraForQRLTCAddress : SendEvent()
    data object OnToggleFiatOrLTC : SendEvent()
    data class OnRecipientAddressChanged(val address: String) : SendEvent()
    data class OnAmountChanged(val amountInLTCString: String) : SendEvent()
    data class OnUserMemorandumChanged(val memo: String) : SendEvent()

    data object OnFieldFocused : SendEvent()
}
