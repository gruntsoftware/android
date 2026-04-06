package com.brainwallet.ui.screens.send

sealed class SendEvent {

    data object OnLoad : SendEvent()
    data class OnConfirmSend(val sendAmount: Float) : SendEvent()
    data object OnTapPasteLTCAddress : SendEvent()
    data object OnTapShowCameraForQRLTCAddress : SendEvent()
    data object OnToggleFiatOrLTC : SendEvent()

    data object OnCheckIfSendIsReady : SendEvent()
}
