package com.brainwallet.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object EventBus {

    private val _events = MutableSharedFlow<Event>(
        replay = 0, // No replays, emit only to active collectors
        extraBufferCapacity = 1, // Buffer size of 1 to prevent backpressure issues
        onBufferOverflow = BufferOverflow.DROP_OLDEST // Drop oldest when buffer overflows
    )

    val events: SharedFlow<Event> = _events.asSharedFlow()

    suspend fun emit(event: Event) {
        _events.emit(event)
    }

    @Deprecated(
        "External litecoin: deep links (device camera/another app scanning a QR code) now " +
            "route through com.brainwallet.navigation.DeepLink - Compose Navigation via " +
            "Route.Main's pendingSendAddress, which opens MainScreen's Send modal - not " +
            "EventBus. This remains only as the bridge for the Send screen's own in-app " +
            "\"Scan\" button (BRActivity.onActivityResult's SCANNER_REQUEST case, posting " +
            "into an already-open Compose screen from a legacy Java Activity callback), not " +
            "for new deep-link-shaped navigation."
    )
    @Suppress("DEPRECATION")
    fun postQRCodeScanned(url: String?) {
        _events.tryEmit(Event.QRCodeScanned(url))
    }

    sealed class Event {
        data class Message(
            val message: String,
            val address: String?,
        ) : Event()

        // provide this for old flow
        data class LegacyPasscodeVerified(
            val passcode: List<Int>,
        ) : Event()

        data class LegacyUnLock(
            val passcode: List<Int>,
        ) : Event()

        @Deprecated(
            "See EventBus#postQRCodeScanned's deprecation note - only the in-app Send screen " +
                "\"Scan\" button still posts this."
        )
        data class QRCodeScanned(val url: String?) : Event()
    }
}
