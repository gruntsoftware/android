package com.brainwallet.ui.screens.home.receive

import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

//todo: wip
class ReceiveDialogViewModel : BrainwalletViewModel<ReceiveDialogEvent>() {

    private val _state = MutableStateFlow(ReceiveDialogState())
    val state: StateFlow<ReceiveDialogState> = _state.asStateFlow()


    override fun onEvent(event: ReceiveDialogEvent) {
        when (event) {
            is ReceiveDialogEvent.OnLoad -> _state.update {
                val address = BRSharedPrefs.getReceiveAddress(event.context)
                it.copy(
                    address = address,
                    qrBitmap = QRUtils.generateQR(event.context, "litecoin:${address}")
                )
            }

            is ReceiveDialogEvent.OnCopyClick -> BRClipboardManager.putClipboard(
                event.context,
                state.value.address
            )
        }
    }
}